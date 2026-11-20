package com.sabikudi.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.sabikudi.auth.AuthPrincipal
import com.sabikudi.auth.parseUuidOrNull
import com.sabikudi.config.AppConfig
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*

fun Application.configureSecurity(config: AppConfig) {
    install(Authentication) {
        jwt("auth-jwt") {
            realm = "sabikudi"
            verifier(
                JWT.require(Algorithm.HMAC256(config.jwtSecret))
                    .withIssuer(config.jwtIssuer)
                    .build(),
            )
            validate { credential ->
                val userId = credential.payload.subject?.let { parseUuidOrNull(it) }
                val deviceId = credential.payload.getClaim("did")?.asString()?.let { parseUuidOrNull(it) }
                val installationId = credential.payload.getClaim("iid")?.asString()?.let { parseUuidOrNull(it) }

                if (userId != null && deviceId != null && installationId != null) {
                    AuthPrincipal(userId, deviceId, installationId)
                } else {
                    null
                }
            }
        }
    }
}
