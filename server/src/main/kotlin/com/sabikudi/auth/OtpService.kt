package com.sabikudi.auth

import com.sabikudi.config.AppConfig
import com.sabikudi.db.Db
import com.sabikudi.db.execute
import com.sabikudi.db.intOrNull
import com.sabikudi.db.one
import com.sabikudi.db.uuidOrNull
import com.sabikudi.plugins.TooManyRequestsError
import com.sabikudi.plugins.ValidationError
import com.sabikudi.shared.dto.OtpChannel
import com.sabikudi.shared.dto.OtpRequestResponse
import java.sql.Connection
import java.util.UUID
import kotlin.coroutines.cancellation.CancellationException

enum class ChallengePurpose { SIGN_IN, STEP_UP_EMAIL, VERIFY_EMAIL }

class VerifiedChallenge(val target: String, val userId: UUID?)

class OtpService(
    private val db: Db,
    private val sender: OtpSender,
    private val config: AppConfig,
) {
    companion object {
        const val CODE_TTL_SECONDS = 300
        const val RESEND_COOLDOWN_SECONDS = 60
        const val MAX_PER_HOUR = 5
        const val MAX_ATTEMPTS = 5
        const val LOCKOUT_AFTER_BURNED_CODES = 3
    }

    suspend fun requestSignIn(rawPhone: String, installationId: UUID, channel: OtpChannel): OtpRequestResponse {
        val phone = Phone.normalize(rawPhone)
        if (channel == OtpChannel.WHATSAPP && !config.whatsappOtpEnabled) {
            throw ValidationError("WhatsApp codes are not available yet", "channel_unavailable")
        }
        val (challengeId, code) = issue(ChallengePurpose.SIGN_IN, phone, channel.name, installationId, userId = null)
        try {
            sender.send(phone, code, channel)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            abandon(challengeId)
            throw e as? OtpDeliveryException ?: OtpDeliveryException("OTP send failed", e)
        }
        return OtpRequestResponse(challengeId.toString(), RESEND_COOLDOWN_SECONDS, CODE_TTL_SECONDS)
    }

    suspend fun issue(
        purpose: ChallengePurpose,
        target: String,
        channel: String,
        installationId: UUID,
        userId: UUID?,
    ): Pair<UUID, String> {
        val code = Crypto.sixDigitCode()
        val id = UUID.randomUUID()
        db.tx { c ->
            c.one("SELECT pg_advisory_xact_lock(hashtext(?))", "${purpose.name}:$target") { 1 }
            enforceLimits(c, purpose, target)
            c.execute(
                """
                INSERT INTO otp_challenges
                    (id, purpose, target, channel, code_hash, installation_id, user_id, max_attempts, expires_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, now() + make_interval(secs => ?))
                """.trimIndent(),
                id, purpose.name, target, channel, hash(id, code), installationId, userId, MAX_ATTEMPTS, CODE_TTL_SECONDS,
            )
        }
        return id to code
    }

    private fun enforceLimits(c: Connection, purpose: ChallengePurpose, target: String) {
        val burned = c.one(
            """
            SELECT count(*) AS n FROM otp_challenges
            WHERE purpose = ? AND target = ? AND consumed_at IS NULL AND attempts >= max_attempts
              AND created_at > now() - interval '30 minutes'
            """.trimIndent(),
            purpose.name, target,
        ) { it.getInt("n") } ?: 0
        if (burned >= LOCKOUT_AFTER_BURNED_CODES) {
            throw TooManyRequestsError(1800, "Too many tries. Try again in 30 minutes.", "locked_out")
        }

        val secondsSinceLast = c.one(
            "SELECT EXTRACT(EPOCH FROM (now() - max(created_at)))::int AS s FROM otp_challenges WHERE purpose = ? AND target = ?",
            purpose.name, target,
        ) { it.intOrNull("s") }
        if (secondsSinceLast != null && secondsSinceLast < RESEND_COOLDOWN_SECONDS) {
            throw TooManyRequestsError(RESEND_COOLDOWN_SECONDS - secondsSinceLast, "Wait before asking for another code.", "resend_cooldown")
        }

        val lastHour = c.one(
            "SELECT count(*) AS n FROM otp_challenges WHERE purpose = ? AND target = ? AND created_at > now() - interval '1 hour'",
            purpose.name, target,
        ) { it.getInt("n") } ?: 0
        if (lastHour >= MAX_PER_HOUR) {
            throw TooManyRequestsError(900, "Too many codes requested. Try again later.", "hourly_limit")
        }
    }

    private suspend fun abandon(challengeId: UUID) {
        db.tx { it.execute("UPDATE otp_challenges SET consumed_at = now() WHERE id = ?", challengeId) }
    }

    private class Row(
        val purpose: String,
        val target: String,
        val userId: UUID?,
        val codeHash: String,
        val installationId: UUID,
        val attempts: Int,
        val maxAttempts: Int,
        val expired: Boolean,
        val consumed: Boolean,
    )

    private sealed interface Outcome {
        class Ok(val result: VerifiedChallenge) : Outcome
        class Bad(val code: String, val message: String) : Outcome
    }

    suspend fun consume(
        challengeId: UUID,
        code: String,
        installationId: UUID,
        purpose: ChallengePurpose,
    ): VerifiedChallenge {
        val outcome = db.tx { c ->
            val row = c.one(
                """
                SELECT purpose, target, user_id, code_hash, installation_id, attempts, max_attempts,
                       (expires_at < now()) AS expired, (consumed_at IS NOT NULL) AS consumed
                FROM otp_challenges WHERE id = ? FOR UPDATE
                """.trimIndent(),
                challengeId,
            ) { rs ->
                Row(
                    purpose = rs.getString("purpose"),
                    target = rs.getString("target"),
                    userId = rs.uuidOrNull("user_id"),
                    codeHash = rs.getString("code_hash"),
                    installationId = rs.getObject("installation_id", UUID::class.java),
                    attempts = rs.getInt("attempts"),
                    maxAttempts = rs.getInt("max_attempts"),
                    expired = rs.getBoolean("expired"),
                    consumed = rs.getBoolean("consumed"),
                )
            }

            when {
                row == null || row.purpose != purpose.name || row.installationId != installationId ||
                    row.consumed || row.expired ->
                    Outcome.Bad("invalid_code", "That code is not valid. Request a new one.")

                row.attempts >= row.maxAttempts ->
                    Outcome.Bad("too_many_attempts", "Too many wrong codes. Request a new one.")

                !Crypto.constantTimeEquals(row.codeHash, hash(challengeId, code.trim())) -> {
                    c.execute("UPDATE otp_challenges SET attempts = attempts + 1 WHERE id = ?", challengeId)
                    val left = row.maxAttempts - (row.attempts + 1)
                    val msg = if (left > 0) "That code doesn't match. $left tries left." else "Too many wrong codes. Request a new one."
                    Outcome.Bad("wrong_code", msg)
                }

                else -> {
                    c.execute("UPDATE otp_challenges SET consumed_at = now() WHERE id = ?", challengeId)
                    Outcome.Ok(VerifiedChallenge(row.target, row.userId))
                }
            }
        }

        return when (outcome) {
            is Outcome.Ok -> outcome.result
            is Outcome.Bad -> throw ValidationError(outcome.message, outcome.code)
        }
    }

    private fun hash(challengeId: UUID, code: String): String =
        Crypto.hmacHex(config.otpPepper, "$challengeId:$code")
}
