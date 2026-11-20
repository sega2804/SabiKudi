package com.sabikudi

import com.sabikudi.config.AppConfig
import com.sabikudi.config.AppEnv
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import org.testcontainers.containers.PostgreSQLContainer
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HealthTest {
    private val postgres = PostgreSQLContainer("postgres:16")
    private var dockerAvailable = false

    @BeforeTest
    fun start() {
        try {
            postgres.start()
            dockerAvailable = true
        } catch (e: Exception) {
            dockerAvailable = false
        }
    }

    @AfterTest
    fun stop() {
        if (dockerAvailable) {
            try {
                postgres.stop()
            } catch (_: Exception) {}
        }
    }

    @Test
    fun healthRouteReturnsOk() {
        if (!dockerAvailable) {
            println("Skipping HealthTest: Docker environment is not running locally.")
            return
        }

        testApplication {
            application {
                configureApp(
                    AppConfig(
                        env = AppEnv.DEV,
                        dbUrl = postgres.jdbcUrl,
                        dbUser = postgres.username,
                        dbPassword = postgres.password,
                        jwtSecret = "dev-only-change-me",
                        jwtIssuer = "sabikudi",
                    ),
                )
            }

            val response = client.get("/health")
            assertEquals(HttpStatusCode.OK, response.status)
            assertTrue(response.bodyAsText().contains("\"status\":\"ok\""))
        }
    }
}
