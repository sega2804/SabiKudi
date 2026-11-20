package com.sabikudi.auth

import com.sabikudi.plugins.ForbiddenError
import com.sabikudi.plugins.UnauthorizedError
import com.sabikudi.plugins.ValidationError
import com.sabikudi.shared.dto.AccessLevel
import com.sabikudi.shared.dto.AuthResponse
import com.sabikudi.shared.dto.AuthStatus
import com.sabikudi.shared.dto.CodeConfirmRequest
import com.sabikudi.shared.dto.MeResponse
import com.sabikudi.shared.dto.OtpRequest
import com.sabikudi.shared.dto.OtpRequestResponse
import com.sabikudi.shared.dto.OtpVerifyRequest
import com.sabikudi.shared.dto.RecoveryEmailRequest
import com.sabikudi.shared.dto.RefreshRequest
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import java.time.OffsetDateTime
import java.util.UUID

fun Application.configureAuthRoutes() {
    val otpService by inject<OtpService>()
    val emailSender by inject<EmailSender>()
    val pushSender by inject<PushSender>()
    val deviceService by inject<DeviceService>()
    val refreshTokenRepo by inject<RefreshTokenRepo>()
    val tokenService by inject<TokenService>()

    routing {
        post("/auth/otp/request") {
            val req = call.receive<OtpRequest>()
            val installationId = parseUuid(req.installationId)
            val res = otpService.requestSignIn(req.phone, installationId, req.channel)
            call.respond(HttpStatusCode.Accepted, res)
        }

        post("/auth/otp/verify") {
            val req = call.receive<OtpVerifyRequest>()
            val challengeId = parseUuid(req.challengeId)
            val installationId = parseUuid(req.installationId)

            val verified = otpService.consume(challengeId, req.code, installationId, ChallengePurpose.SIGN_IN)
            val user = deviceService.getOrCreateUser(verified.target)
            val isKnownDevice = deviceService.isKnownDevice(user.id, installationId)
            val isOwner = deviceService.isWorkspaceOwner(user.id)

            if (!isKnownDevice && isOwner) {
                if (user.hasVerifiedEmail) {
                    val recoveryEmail = requireNotNull(user.recoveryEmail)
                    val (emailChallengeId, emailCode) = otpService.issue(
                        purpose = ChallengePurpose.STEP_UP_EMAIL,
                        target = recoveryEmail,
                        channel = "EMAIL",
                        installationId = installationId,
                        userId = user.id,
                    )
                    emailSender.sendCode(recoveryEmail, emailCode)
                    call.respond(
                        HttpStatusCode.OK,
                        AuthResponse(
                            status = AuthStatus.EMAIL_CODE_REQUIRED,
                            emailChallengeId = emailChallengeId.toString(),
                            maskedEmail = maskEmail(recoveryEmail),
                        ),
                    )
                    return@post
                } else {
                    val fullAccessFrom = OffsetDateTime.now().plusHours(24)
                    val device = deviceService.registerDevice(user.id, installationId, req.deviceModel, req.fcmToken, fullAccessFrom)
                    val refreshToken = refreshTokenRepo.issueNewFamily(user.id, device.id)
                    val tokens = tokenService.makeTokenPair(user.id, device.id, installationId, refreshToken)

                    pushSender.alertOwnerNewDevice(user.id, device.id)

                    call.respond(
                        HttpStatusCode.OK,
                        AuthResponse(
                            status = AuthStatus.SIGNED_IN,
                            tokens = tokens,
                            accessLevel = AccessLevel.LIMITED,
                            fullAccessFrom = fullAccessFrom.toString(),
                        ),
                    )
                    return@post
                }
            }

            val device = deviceService.registerDevice(user.id, installationId, req.deviceModel, req.fcmToken)
            val refreshToken = refreshTokenRepo.issueNewFamily(user.id, device.id)
            val tokens = tokenService.makeTokenPair(user.id, device.id, installationId, refreshToken)

            call.respond(
                HttpStatusCode.OK,
                AuthResponse(
                    status = AuthStatus.SIGNED_IN,
                    tokens = tokens,
                    accessLevel = device.accessLevelAt(),
                ),
            )
        }

        post("/auth/email/verify") {
            val req = call.receive<OtpVerifyRequest>()
            val challengeId = parseUuid(req.challengeId)
            val installationId = parseUuid(req.installationId)

            val verified = otpService.consume(challengeId, req.code, installationId, ChallengePurpose.STEP_UP_EMAIL)
            val userId = requireNotNull(verified.userId) { "Invalid challenge" }

            val device = deviceService.registerDevice(userId, installationId, req.deviceModel, req.fcmToken)
            deviceService.promoteToFullAccess(device.id)
            deviceService.revokeOtherDevices(userId, device.id)

            val refreshToken = refreshTokenRepo.issueNewFamily(userId, device.id)
            val tokens = tokenService.makeTokenPair(userId, device.id, installationId, refreshToken)

            call.respond(
                HttpStatusCode.OK,
                AuthResponse(
                    status = AuthStatus.SIGNED_IN,
                    tokens = tokens,
                    accessLevel = AccessLevel.FULL,
                ),
            )
        }

        post("/auth/refresh") {
            val req = call.receive<RefreshRequest>()
            val installationId = parseUuid(req.installationId)
            val session = refreshTokenRepo.rotate(req.refreshToken, installationId)
            val tokens = tokenService.makeTokenPair(session.userId, session.deviceId, installationId, session.refreshToken)
            call.respond(HttpStatusCode.OK, tokens)
        }

        authenticate("auth-jwt") {
            post("/auth/logout") {
                val principal = call.principal<AuthPrincipal>() ?: throw UnauthorizedError()
                refreshTokenRepo.revokeDevice(principal.deviceId)
                call.respond(HttpStatusCode.NoContent)
            }

            get("/me") {
                val principal = call.principal<AuthPrincipal>() ?: throw UnauthorizedError()
                val user = deviceService.findUserById(principal.userId) ?: throw UnauthorizedError()
                val device = deviceService.findDevice(principal.userId, principal.installationId) ?: throw UnauthorizedError()

                call.respond(
                    HttpStatusCode.OK,
                    MeResponse(
                        userId = user.id.toString(),
                        phone = user.phone,
                        recoveryEmailVerified = user.hasVerifiedEmail,
                        accessLevel = device.accessLevelAt(),
                        fullAccessFrom = if (device.accessLevelAt() == AccessLevel.LIMITED) device.fullAccessFrom.toString() else null,
                    ),
                )
            }

            put("/me/recovery-email") {
                val principal = call.principal<AuthPrincipal>() ?: throw UnauthorizedError()
                val device = deviceService.findDevice(principal.userId, principal.installationId) ?: throw UnauthorizedError()
                if (device.accessLevelAt() == AccessLevel.LIMITED) {
                    throw ForbiddenError("device_limited", "A 24-hour limited session cannot change the recovery email")
                }

                val req = call.receive<RecoveryEmailRequest>()
                val email = normalizeEmail(req.email)

                deviceService.setRecoveryEmail(principal.userId, email)
                val (challengeId, code) = otpService.issue(
                    purpose = ChallengePurpose.VERIFY_EMAIL,
                    target = email,
                    channel = "EMAIL",
                    installationId = principal.installationId,
                    userId = principal.userId,
                )
                emailSender.sendCode(email, code)

                call.respond(
                    HttpStatusCode.Accepted,
                    OtpRequestResponse(challengeId.toString(), OtpService.RESEND_COOLDOWN_SECONDS, OtpService.CODE_TTL_SECONDS),
                )
            }

            post("/me/recovery-email/verify") {
                val principal = call.principal<AuthPrincipal>() ?: throw UnauthorizedError()
                val req = call.receive<CodeConfirmRequest>()
                val challengeId = parseUuid(req.challengeId)

                val verified = otpService.consume(challengeId, req.code, principal.installationId, ChallengePurpose.VERIFY_EMAIL)
                deviceService.confirmRecoveryEmail(principal.userId, verified.target)

                call.respond(HttpStatusCode.NoContent)
            }
        }
    }
}
