package com.sabikudi.plugins

import com.sabikudi.auth.OtpDeliveryException
import com.sabikudi.shared.dto.ErrorResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.ContentTransformationException
import io.ktor.server.response.*
import kotlinx.serialization.SerializationException
import kotlin.coroutines.cancellation.CancellationException

class ValidationError(message: String, val code: String = "validation_error") : RuntimeException(message)
class UnauthorizedError(message: String = "Sign in required", val code: String = "unauthorized") : RuntimeException(message)
class ForbiddenError(val code: String = "forbidden", message: String = "Forbidden") : RuntimeException(message)
class NotFoundError(message: String = "Not found") : RuntimeException(message)
class TooManyRequestsError(
    val retryAfterSeconds: Int,
    message: String,
    val code: String = "too_many_requests",
) : RuntimeException(message)

fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<ValidationError> { call, e ->
            call.respond(HttpStatusCode.BadRequest, ErrorResponse(e.code, e.message ?: "Invalid request"))
        }
        exception<UnauthorizedError> { call, e ->
            call.respond(HttpStatusCode.Unauthorized, ErrorResponse(e.code, e.message ?: "Sign in required"))
        }
        exception<ForbiddenError> { call, e ->
            call.respond(HttpStatusCode.Forbidden, ErrorResponse(e.code, e.message ?: "Forbidden"))
        }
        exception<NotFoundError> { call, e ->
            call.respond(HttpStatusCode.NotFound, ErrorResponse("not_found", e.message ?: "Not found"))
        }
        exception<TooManyRequestsError> { call, e ->
            call.response.header(HttpHeaders.RetryAfter, e.retryAfterSeconds.toString())
            call.respond(
                HttpStatusCode.TooManyRequests,
                ErrorResponse(e.code, e.message ?: "Too many requests", e.retryAfterSeconds),
            )
        }
        exception<OtpDeliveryException> { call, e ->
            call.application.environment.log.warn("OTP delivery failed: ${e.message}")
            call.respond(
                HttpStatusCode.BadGateway,
                ErrorResponse("otp_delivery_failed", "We couldn't send the code. Please try again."),
            )
        }
        exception<BadRequestException> { call, _ ->
            call.respond(HttpStatusCode.BadRequest, ErrorResponse("bad_request", "Invalid request"))
        }
        exception<ContentTransformationException> { call, _ ->
            call.respond(HttpStatusCode.BadRequest, ErrorResponse("bad_request", "Invalid request"))
        }
        exception<SerializationException> { call, _ ->
            call.respond(HttpStatusCode.BadRequest, ErrorResponse("bad_request", "Invalid request"))
        }
        exception<Throwable> { call, e ->
            if (e is CancellationException) throw e
            call.application.environment.log.error("Unhandled error on ${call.request.local.uri}", e)
            call.respond(HttpStatusCode.InternalServerError, ErrorResponse("internal_error", "Something went wrong"))
        }
    }
}
