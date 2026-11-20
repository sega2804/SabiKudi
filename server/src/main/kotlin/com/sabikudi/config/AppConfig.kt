package com.sabikudi.config

enum class AppEnv { DEV, PROD }

enum class OtpProvider { LOG, TERMII }

data class AppConfig(
    val env: AppEnv,
    val dbUrl: String,
    val dbUser: String,
    val dbPassword: String,
    val jwtSecret: String,
    val jwtIssuer: String,
    val otpPepper: String = "dev-only-otp-pepper",
    val otpProvider: OtpProvider = OtpProvider.LOG,
    val termiiApiKey: String? = null,
    val termiiSenderId: String = "Sabikudi",
    val termiiBaseUrl: String = "https://api.ng.termii.com",
    val whatsappOtpEnabled: Boolean = false,
    val accessTokenMinutes: Long = 15,
    val refreshTokenDays: Long = 60,
) {
    init {
        if (env == AppEnv.PROD) {
            require(jwtSecret.length >= 32) { "JWT_SECRET must be at least 32 characters in PROD" }
            require(otpPepper.length >= 32) { "OTP_PEPPER must be at least 32 characters in PROD" }
            require(otpProvider != OtpProvider.LOG) { "OTP_PROVIDER=LOG prints codes to the log and is not allowed in PROD" }
        }
        if (otpProvider == OtpProvider.TERMII) {
            require(!termiiApiKey.isNullOrBlank()) { "TERMII_API_KEY is required when OTP_PROVIDER=TERMII" }
        }
    }

    companion object {
        fun fromEnv(): AppConfig {
            val env = if (System.getenv("APP_ENV")?.uppercase() == "PROD") AppEnv.PROD else AppEnv.DEV

            fun value(name: String, devDefault: String): String =
                System.getenv(name)
                    ?: if (env == AppEnv.DEV) devDefault
                    else error("Missing required environment variable $name (APP_ENV=PROD)")

            return AppConfig(
                env = env,
                dbUrl = value("DB_URL", "jdbc:postgresql://localhost:5432/sabikudi"),
                dbUser = value("DB_USER", "sabikudi"),
                dbPassword = value("DB_PASSWORD", "sabikudi_dev"),
                jwtSecret = value("JWT_SECRET", "dev-only-change-me"),
                jwtIssuer = value("JWT_ISSUER", "sabikudi"),
                otpPepper = value("OTP_PEPPER", "dev-only-otp-pepper"),
                otpProvider = OtpProvider.valueOf(value("OTP_PROVIDER", "LOG").uppercase()),
                termiiApiKey = System.getenv("TERMII_API_KEY"),
                termiiSenderId = System.getenv("TERMII_SENDER_ID") ?: "Sabikudi",
                termiiBaseUrl = System.getenv("TERMII_BASE_URL") ?: "https://api.ng.termii.com",
                whatsappOtpEnabled = System.getenv("WHATSAPP_OTP_ENABLED") == "true",
            )
        }
    }
}
