package com.sabikudi.auth

import com.sabikudi.plugins.ValidationError
import java.util.UUID

fun parseUuid(value: String): UUID =
    parseUuidOrNull(value) ?: throw ValidationError("Invalid identifier", "invalid_id")

fun parseUuidOrNull(value: String): UUID? =
    try {
        UUID.fromString(value)
    } catch (e: IllegalArgumentException) {
        null
    }

/** The signed-in caller, set by the JWT plugin after the device is checked against the database. */
data class AuthPrincipal(val userId: UUID, val deviceId: UUID, val installationId: UUID)

object Phone {
    private val e164 = Regex("""^\+[1-9][0-9]{7,14}$""")

    /** Accepts 0803..., 234803..., +234803... and +234 0803...; returns E.164. */
    fun normalize(raw: String): String {
        val cleaned = raw.trim().replace(Regex("""[\s\-().]"""), "")
        val candidate = when {
            cleaned.startsWith("+2340") -> "+234" + cleaned.drop(5)
            cleaned.startsWith("+") -> cleaned
            cleaned.startsWith("00") -> "+" + cleaned.drop(2)
            cleaned.startsWith("234") && cleaned.length == 13 -> "+$cleaned"
            cleaned.startsWith("0") && cleaned.length == 11 -> "+234" + cleaned.drop(1)
            else -> throw ValidationError("Enter your phone number with its country code", "invalid_phone")
        }
        if (!e164.matches(candidate)) {
            throw ValidationError("Enter a valid phone number", "invalid_phone")
        }
        return candidate
    }
}

fun normalizeEmail(raw: String): String {
    val email = raw.trim().lowercase()
    if (email.length > 254 || !Regex("""^[^@\s]+@[^@\s]+\.[^@\s]+$""").matches(email)) {
        throw ValidationError("Enter a valid email address", "invalid_email")
    }
    return email
}

fun maskEmail(email: String): String {
    val (name, domain) = email.split("@", limit = 2)
    return "${name.first()}***@$domain"
}
