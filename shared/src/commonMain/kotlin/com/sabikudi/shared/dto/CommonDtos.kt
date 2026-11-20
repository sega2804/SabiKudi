package com.sabikudi.shared.dto

import kotlinx.serialization.Serializable

@Serializable
data class HealthResponse(val status: String, val db: Boolean)

@Serializable
data class ErrorResponse(
    val code: String,
    val message: String,
    val retryAfterSeconds: Int? = null,
)
