package com.sabikudi.auth

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object Crypto {
    private val random = SecureRandom()

    fun sixDigitCode(): String = random.nextInt(1_000_000).toString().padStart(6, '0')

    fun randomToken(bytes: Int = 32): String {
        val buffer = ByteArray(bytes).also { random.nextBytes(it) }
        return Base64.getUrlEncoder().withoutPadding().encodeToString(buffer)
    }

    fun sha256Hex(value: String): String =
        MessageDigest.getInstance("SHA-256").digest(value.toByteArray()).toHex()

    /** Keyed hash. Six-digit codes are guessable offline, so a plain hash is not enough. */
    fun hmacHex(key: String, message: String): String {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(key.toByteArray(), "HmacSHA256"))
        return mac.doFinal(message.toByteArray()).toHex()
    }

    fun constantTimeEquals(a: String, b: String): Boolean =
        MessageDigest.isEqual(a.toByteArray(), b.toByteArray())

    private fun ByteArray.toHex() = joinToString("") { "%02x".format(it) }
}
