package com.sabikudi.auth

import com.sabikudi.config.AppConfig
import com.sabikudi.config.OtpProvider
import com.sabikudi.shared.dto.OtpChannel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.util.UUID

class OtpDeliveryException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)

interface OtpSender {
    suspend fun send(phoneE164: String, code: String, channel: OtpChannel)
}

interface EmailSender {
    suspend fun sendCode(to: String, code: String)
}

interface PushSender {
    suspend fun alertOwnerNewDevice(userId: UUID, newDeviceId: UUID)
}

class LoggingOtpSender : OtpSender {
    private val log = LoggerFactory.getLogger(LoggingOtpSender::class.java)
    override suspend fun send(phoneE164: String, code: String, channel: OtpChannel) {
        log.info("DEV OTP via {} to {}: {}", channel, phoneE164, code)
    }
}

class LoggingEmailSender : EmailSender {
    private val log = LoggerFactory.getLogger(LoggingEmailSender::class.java)
    override suspend fun sendCode(to: String, code: String) {
        log.info("DEV email code to {}: {}", to, code)
    }
}

class LoggingPushSender : PushSender {
    private val log = LoggerFactory.getLogger(LoggingPushSender::class.java)
    override suspend fun alertOwnerNewDevice(userId: UUID, newDeviceId: UUID) {
        log.info("DEV push: owner {} signed in on new device {}", userId, newDeviceId)
    }
}

class TermiiOtpSender(
    private val apiKey: String,
    private val senderId: String,
    baseUrl: String,
) : OtpSender {

    private val endpoint = URI.create(baseUrl.trimEnd('/') + "/api/sms/send")
    private val http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build()
    private val json = Json { encodeDefaults = true }

    @Serializable
    private data class SendBody(
        val to: String,
        val from: String,
        val sms: String,
        val type: String = "plain",
        val channel: String,
        @SerialName("api_key") val apiKey: String,
    )

    override suspend fun send(phoneE164: String, code: String, channel: OtpChannel) {
        val body = SendBody(
            to = phoneE164.removePrefix("+"),
            from = senderId,
            sms = "Your Sabikudi code is $code. It expires in 5 minutes. Never share it.",
            channel = when (channel) {
                OtpChannel.SMS -> "dnd"
                OtpChannel.WHATSAPP -> "whatsapp"
            },
            apiKey = apiKey,
        )
        val request = HttpRequest.newBuilder(endpoint)
            .timeout(Duration.ofSeconds(10))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(json.encodeToString(body)))
            .build()

        val status = try {
            withContext(Dispatchers.IO) {
                http.send(request, HttpResponse.BodyHandlers.discarding()).statusCode()
            }
        } catch (e: Exception) {
            throw OtpDeliveryException("Termii request failed", e)
        }
        if (status !in 200..299) throw OtpDeliveryException("Termii returned HTTP $status")
    }
}

fun buildOtpSender(config: AppConfig): OtpSender = when (config.otpProvider) {
    OtpProvider.LOG -> LoggingOtpSender()
    OtpProvider.TERMII -> TermiiOtpSender(
        apiKey = requireNotNull(config.termiiApiKey),
        senderId = config.termiiSenderId,
        baseUrl = config.termiiBaseUrl,
    )
}
