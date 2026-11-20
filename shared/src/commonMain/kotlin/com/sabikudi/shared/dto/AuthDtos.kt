package com.sabikudi.shared.dto

import kotlinx.serialization.Serializable

@Serializable
enum class OtpChannel { SMS, WHATSAPP }

@Serializable
enum class AuthStatus { SIGNED_IN, EMAIL_CODE_REQUIRED }

@Serializable
enum class AccessLevel { FULL, LIMITED }

@Serializable
data class OtpRequest(
    val phone: String,
    val installationId: String,
    val channel: OtpChannel = OtpChannel.SMS,
)

/** Used for phone codes, email codes and recovery-email codes. */
@Serializable
data class OtpRequestResponse(
    val challengeId: String,
    val resendAfterSeconds: Int,
    val expiresInSeconds: Int,
)

/** Used by both /auth/otp/verify and /auth/email/verify. */
@Serializable
data class OtpVerifyRequest(
    val challengeId: String,
    val code: String,
    val installationId: String,
    val deviceModel: String? = null,
    val fcmToken: String? = null,
)

@Serializable
data class TokenPair(
    val accessToken: String,
    val refreshToken: String,
    val expiresInSeconds: Int,
)

@Serializable
data class AuthResponse(
    val status: AuthStatus,
    val tokens: TokenPair? = null,
    val accessLevel: AccessLevel? = null,
    val fullAccessFrom: String? = null,      // ISO-8601, set when accessLevel is LIMITED
    val emailChallengeId: String? = null,    // set when status is EMAIL_CODE_REQUIRED
    val maskedEmail: String? = null,
)

@Serializable
data class RefreshRequest(val refreshToken: String, val installationId: String)

@Serializable
data class RecoveryEmailRequest(val email: String)

@Serializable
data class CodeConfirmRequest(val challengeId: String, val code: String)

@Serializable
data class MeResponse(
    val userId: String,
    val phone: String,
    val recoveryEmailVerified: Boolean,
    val accessLevel: AccessLevel,
    val fullAccessFrom: String? = null,
)
