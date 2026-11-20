package com.sabikudi

import com.sabikudi.auth.DeviceService
import com.sabikudi.auth.EmailSender
import com.sabikudi.auth.LoggingEmailSender
import com.sabikudi.auth.LoggingPushSender
import com.sabikudi.auth.OtpService
import com.sabikudi.auth.PushSender
import com.sabikudi.auth.RefreshTokenRepo
import com.sabikudi.auth.TokenService
import com.sabikudi.auth.buildOtpSender
import com.sabikudi.auth.configureAuthRoutes
import com.sabikudi.config.AppConfig
import com.sabikudi.db.Db
import com.sabikudi.plugins.configureRouting
import com.sabikudi.plugins.configureSecurity
import com.sabikudi.plugins.configureSerialization
import com.sabikudi.plugins.configureStatusPages
import io.ktor.server.application.*
import io.ktor.server.plugins.calllogging.*
import org.koin.dsl.module as koinModule
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun Application.module() {
    configureApp(AppConfig.fromEnv())
}

fun Application.configureApp(config: AppConfig) {
    val db = Db(config)
    monitor.subscribe(ApplicationStopped) { db.close() }

    val otpSender = buildOtpSender(config)
    val emailSender: EmailSender = LoggingEmailSender()
    val pushSender: PushSender = LoggingPushSender()

    install(Koin) {
        slf4jLogger()
        modules(koinModule {
            single { config }
            single { db }
            single { otpSender }
            single { emailSender }
            single { pushSender }
            single { OtpService(get(), get(), get()) }
            single { TokenService(get()) }
            single { RefreshTokenRepo(get(), get()) }
            single { DeviceService(get()) }
        })
    }
    install(CallLogging)

    configureSecurity(config)
    configureSerialization()
    configureStatusPages()
    configureRouting()
    configureAuthRoutes()
}
