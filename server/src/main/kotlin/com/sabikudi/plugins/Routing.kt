package com.sabikudi.plugins

import com.sabikudi.db.Db
import com.sabikudi.shared.dto.HealthResponse
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Application.configureRouting() {
    val db by inject<Db>()

    routing {
        get("/health") {
            val ok = db.isHealthy()
            call.respond(HealthResponse(status = if (ok) "ok" else "degraded", db = ok))
        }
    }
}
