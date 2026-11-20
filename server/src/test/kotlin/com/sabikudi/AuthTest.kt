package com.sabikudi

import com.sabikudi.auth.Crypto
import com.sabikudi.auth.Phone
import com.sabikudi.auth.maskEmail
import com.sabikudi.auth.normalizeEmail
import com.sabikudi.config.AppConfig
import com.sabikudi.config.AppEnv
import com.sabikudi.config.OtpProvider
import com.sabikudi.plugins.ValidationError
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AuthTest {

    @Test
    fun phoneNormalizationWorksForNigerianFormats() {
        assertEquals("+2348031234567", Phone.normalize("08031234567"))
        assertEquals("+2348031234567", Phone.normalize("2348031234567"))
        assertEquals("+2348031234567", Phone.normalize("+2348031234567"))
        assertEquals("+2348031234567", Phone.normalize("+234 0803 123 4567"))
    }

    @Test
    fun invalidPhoneFormatThrowsValidationError() {
        assertFailsWith<ValidationError> { Phone.normalize("123") }
        assertFailsWith<ValidationError> { Phone.normalize("abc") }
    }

    @Test
    fun emailNormalizationAndMaskingWork() {
        assertEquals("user@example.com", normalizeEmail("  User@Example.COM "))
        assertEquals("u***@example.com", maskEmail("user@example.com"))
    }

    @Test
    fun cryptoSixDigitCodeGeneratesSixDigits() {
        val code = Crypto.sixDigitCode()
        assertEquals(6, code.length)
        assertTrue(code.all { it.isDigit() })
    }

    @Test
    fun cryptoConstantTimeEqualsWorks() {
        assertTrue(Crypto.constantTimeEquals("secret123", "secret123"))
        assertFalse(Crypto.constantTimeEquals("secret123", "wrongcode"))
    }

    @Test
    fun appConfigValidatesProdSecrets() {
        assertFailsWith<IllegalArgumentException> {
            AppConfig(
                env = AppEnv.DEV,
                dbUrl = "jdbc:postgresql://localhost:5432/sabikudi",
                dbUser = "user",
                dbPassword = "password",
                jwtSecret = "short",
                jwtIssuer = "sabikudi",
                otpPepper = "012345678901234567890123456789012",
                otpProvider = OtpProvider.LOG,
            ).also {
                AppConfig(
                    env = AppEnv.PROD,
                    dbUrl = "jdbc:postgresql://localhost:5432/sabikudi",
                    dbUser = "user",
                    dbPassword = "password",
                    jwtSecret = "short",
                    jwtIssuer = "sabikudi",
                    otpPepper = "012345678901234567890123456789012",
                    otpProvider = OtpProvider.TERMII,
                    termiiApiKey = "test-key",
                )
            }
        }
    }
}
