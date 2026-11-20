package com.sabikudi.auth

import com.sabikudi.config.AppConfig
import com.sabikudi.db.Db
import com.sabikudi.db.execute
import com.sabikudi.db.one
import com.sabikudi.db.uuid
import com.sabikudi.db.uuidOrNull
import com.sabikudi.plugins.UnauthorizedError
import java.sql.Connection
import java.util.UUID

class SessionTokens(val userId: UUID, val deviceId: UUID, val refreshToken: String)

class RefreshTokenRepo(private val db: Db, private val config: AppConfig) {

    /** First sign-in for a device: creates a new token family and returns the plain refresh token. */
    suspend fun issueNewFamily(userId: UUID, deviceId: UUID): String =
        db.tx { c -> issueInTx(c, userId, deviceId, familyId = UUID.randomUUID()) }

    /**
     * Rotates [rawToken]. If the token was already used, revokes the whole family (reuse detection)
     * and throws `session_revoked`.
     */
    suspend fun rotate(rawToken: String, installationId: UUID): SessionTokens {
        val hash = Crypto.sha256Hex(rawToken.trim())

        val outcome = db.tx { c ->
            val row = c.one(
                """
                SELECT r.id, r.user_id, r.device_id, r.family_id, r.used_at, r.revoked_at,
                       (r.expires_at < now()) AS expired, d.installation_id
                FROM refresh_tokens r
                JOIN devices d ON d.id = r.device_id
                WHERE r.token_hash = ? FOR UPDATE OF r
                """.trimIndent(),
                hash,
            ) { rs ->
                object {
                    val id = rs.uuid("id")
                    val userId = rs.uuid("user_id")
                    val deviceId = rs.uuid("device_id")
                    val familyId = rs.uuid("family_id")
                    val usedAt = rs.getObject("used_at")
                    val revokedAt = rs.getObject("revoked_at")
                    val expired = rs.getBoolean("expired")
                    val installationId = rs.uuid("installation_id")
                }
            }

            when {
                row == null || row.installationId != installationId || row.expired ->
                    RotateOutcome.Revoked

                row.usedAt != null -> {
                    // Reuse detected: revoke the whole family.
                    c.execute("UPDATE refresh_tokens SET revoked_at = now() WHERE family_id = ?", row.familyId)
                    RotateOutcome.ReuseDetected
                }

                row.revokedAt != null -> RotateOutcome.Revoked

                else -> {
                    c.execute("UPDATE refresh_tokens SET used_at = now() WHERE id = ?", row.id)
                    val newToken = issueInTx(c, row.userId, row.deviceId, row.familyId)
                    RotateOutcome.Ok(SessionTokens(row.userId, row.deviceId, newToken))
                }
            }
        }

        return when (outcome) {
            is RotateOutcome.Ok -> outcome.tokens
            RotateOutcome.ReuseDetected -> throw UnauthorizedError("Session revoked", "session_revoked")
            RotateOutcome.Revoked -> throw UnauthorizedError("Session expired", "session_expired")
        }
    }

    suspend fun revokeDevice(deviceId: UUID) {
        db.tx { c ->
            c.execute("UPDATE refresh_tokens SET revoked_at = now() WHERE device_id = ? AND revoked_at IS NULL", deviceId)
        }
    }

    suspend fun revokeUser(userId: UUID) {
        db.tx { c ->
            c.execute("UPDATE refresh_tokens SET revoked_at = now() WHERE user_id = ? AND revoked_at IS NULL", userId)
        }
    }

    private fun issueInTx(c: Connection, userId: UUID, deviceId: UUID, familyId: UUID): String {
        val raw = Crypto.randomToken()
        val hash = Crypto.sha256Hex(raw)
        c.execute(
            """
            INSERT INTO refresh_tokens (user_id, device_id, family_id, token_hash, expires_at)
            VALUES (?, ?, ?, ?, now() + make_interval(days => ?))
            """.trimIndent(),
            userId, deviceId, familyId, hash, config.refreshTokenDays.toInt(),
        )
        return raw
    }

    private sealed interface RotateOutcome {
        class Ok(val tokens: SessionTokens) : RotateOutcome
        data object ReuseDetected : RotateOutcome
        data object Revoked : RotateOutcome
    }
}
