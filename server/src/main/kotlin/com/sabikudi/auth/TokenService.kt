package com.sabikudi.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.sabikudi.config.AppConfig
import com.sabikudi.shared.dto.TokenPair
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.UUID

class TokenService(private val config: AppConfig) {
    private val algorithm = Algorithm.HMAC256(config.jwtSecret)

    fun issueAccessToken(userId: UUID, deviceId: UUID, installationId: UUID): String {
        val now = Instant.now()
        val expiresAt = now.plus(config.accessTokenMinutes, ChronoUnit.MINUTES)
        return JWT.create()
            .withIssuer(config.jwtIssuer)
            .withSubject(userId.toString())
            .withClaim("did", deviceId.toString())
            .withClaim("iid", installationId.toString())
            .withIssuedAt(Date.from(now))
            .withExpiresAt(Date.from(expiresAt))
            .sign(algorithm)
    }

    fun makeTokenPair(userId: UUID, deviceId: UUID, installationId: UUID, refreshToken: String): TokenPair =
        TokenPair(
            accessToken = issueAccessToken(userId, deviceId, installationId),
            refreshToken = refreshToken,
            expiresInSeconds = (config.accessTokenMinutes * 60).toInt(),
        )
}
