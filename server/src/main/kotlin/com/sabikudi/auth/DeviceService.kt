package com.sabikudi.auth

import com.sabikudi.db.Db
import com.sabikudi.db.execute
import com.sabikudi.db.offsetDateTime
import com.sabikudi.db.one
import com.sabikudi.db.uuid
import com.sabikudi.db.uuidOrNull
import com.sabikudi.shared.dto.AccessLevel
import java.time.OffsetDateTime
import java.util.UUID

class DeviceRecord(
    val id: UUID,
    val userId: UUID,
    val installationId: UUID,
    val fullAccessFrom: OffsetDateTime,
) {
    fun accessLevelAt(now: OffsetDateTime = OffsetDateTime.now()): AccessLevel =
        if (now.isAfter(fullAccessFrom) || now.isEqual(fullAccessFrom)) AccessLevel.FULL else AccessLevel.LIMITED
}

class UserSummary(val id: UUID, val phone: String, val recoveryEmail: String?, val recoveryEmailVerifiedAt: OffsetDateTime?) {
    val hasVerifiedEmail: Boolean get() = recoveryEmailVerifiedAt != null
}

class DeviceService(private val db: Db) {

    /** Upserts the user by phone number and returns their record. */
    suspend fun getOrCreateUser(phone: String): UserSummary = db.tx { c ->
        c.execute("INSERT INTO users (phone) VALUES (?) ON CONFLICT (phone) DO NOTHING", phone)
        requireNotNull(
            c.one(
                "SELECT id, phone, recovery_email, recovery_email_verified_at FROM users WHERE phone = ?",
                phone,
            ) { rs ->
                UserSummary(
                    id = rs.uuid("id"),
                    phone = rs.getString("phone"),
                    recoveryEmail = rs.getString("recovery_email"),
                    recoveryEmailVerifiedAt = rs.getString("recovery_email_verified_at")?.let { rs.offsetDateTime("recovery_email_verified_at") },
                )
            },
        )
    }

    suspend fun findUserById(userId: UUID): UserSummary? = db.tx { c ->
        c.one(
            "SELECT id, phone, recovery_email, recovery_email_verified_at FROM users WHERE id = ?",
            userId,
        ) { rs ->
            UserSummary(
                id = rs.uuid("id"),
                phone = rs.getString("phone"),
                recoveryEmail = rs.getString("recovery_email"),
                recoveryEmailVerifiedAt = rs.getString("recovery_email_verified_at")?.let { rs.offsetDateTime("recovery_email_verified_at") },
            )
        }
    }

    /** Upserts a device row and updates its `last_seen_at`. */
    suspend fun registerDevice(
        userId: UUID,
        installationId: UUID,
        model: String?,
        fcmToken: String?,
        fullAccessFrom: OffsetDateTime = OffsetDateTime.now(),
    ): DeviceRecord = db.tx { c ->
        c.execute(
            """
            INSERT INTO devices (user_id, installation_id, model, fcm_token, full_access_from)
            VALUES (?, ?, ?, ?, ?)
            ON CONFLICT (user_id, installation_id) DO UPDATE SET
                model = COALESCE(EXCLUDED.model, devices.model),
                fcm_token = COALESCE(EXCLUDED.fcm_token, devices.fcm_token),
                last_seen_at = now()
            """.trimIndent(),
            userId, installationId, model, fcmToken, fullAccessFrom,
        )
        requireNotNull(
            c.one(
                "SELECT id, user_id, installation_id, full_access_from FROM devices WHERE user_id = ? AND installation_id = ?",
                userId, installationId,
            ) { rs ->
                DeviceRecord(
                    id = rs.uuid("id"),
                    userId = rs.uuid("user_id"),
                    installationId = rs.uuid("installation_id"),
                    fullAccessFrom = rs.offsetDateTime("full_access_from"),
                )
            },
        )
    }

    /** Returns true if this installation id is already known for this user. */
    suspend fun isKnownDevice(userId: UUID, installationId: UUID): Boolean = db.tx { c ->
        c.one(
            "SELECT 1 FROM devices WHERE user_id = ? AND installation_id = ?",
            userId, installationId,
        ) { 1 } != null
    }

    /** Returns true if the user owns any active workspace. */
    suspend fun isWorkspaceOwner(userId: UUID): Boolean = db.tx { c ->
        c.one(
            "SELECT 1 FROM memberships WHERE user_id = ? AND role = 'OWNER' AND status != 'REMOVED' LIMIT 1",
            userId,
        ) { 1 } != null
    }

    suspend fun findDevice(userId: UUID, installationId: UUID): DeviceRecord? = db.tx { c ->
        c.one(
            "SELECT id, user_id, installation_id, full_access_from FROM devices WHERE user_id = ? AND installation_id = ?",
            userId, installationId,
        ) { rs ->
            DeviceRecord(
                id = rs.uuid("id"),
                userId = rs.uuid("user_id"),
                installationId = rs.uuid("installation_id"),
                fullAccessFrom = rs.offsetDateTime("full_access_from"),
            )
        }
    }

    /** Promotes a device to FULL access immediately (used when an owner verifies an email code). */
    suspend fun promoteToFullAccess(deviceId: UUID) {
        db.tx { c ->
            c.execute("UPDATE devices SET full_access_from = now() WHERE id = ?", deviceId)
        }
    }

    /** Revokes all other devices for a user except [keepDeviceId]. */
    suspend fun revokeOtherDevices(userId: UUID, keepDeviceId: UUID) {
        db.tx { c ->
            c.execute(
                "UPDATE refresh_tokens SET revoked_at = now() WHERE user_id = ? AND device_id != ? AND revoked_at IS NULL",
                userId, keepDeviceId,
            )
        }
    }

    suspend fun setRecoveryEmail(userId: UUID, email: String) {
        db.tx { c ->
            c.execute("UPDATE users SET recovery_email = ?, recovery_email_verified_at = NULL WHERE id = ?", email, userId)
        }
    }

    suspend fun confirmRecoveryEmail(userId: UUID, email: String) {
        db.tx { c ->
            c.execute(
                "UPDATE users SET recovery_email = ?, recovery_email_verified_at = now() WHERE id = ?",
                email, userId,
            )
        }
    }
}
