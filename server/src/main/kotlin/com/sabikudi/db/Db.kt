package com.sabikudi.db

import com.sabikudi.config.AppConfig
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.flywaydb.core.Flyway
import java.sql.Connection
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction

class Db(config: AppConfig) : AutoCloseable {

    private val dataSource = HikariDataSource(HikariConfig().apply {
        jdbcUrl = config.dbUrl
        username = config.dbUser
        password = config.dbPassword
        maximumPoolSize = 10
        isAutoCommit = false
        validate()
    })

    private val database: Database

    init {
        Flyway.configure()
            .dataSource(dataSource)
            .locations("classpath:db/migration")
            .load()
            .migrate()

        database = Database.connect(dataSource)
    }

    suspend fun <T> query(block: () -> T): T =
        withContext(Dispatchers.IO) { transaction(database) { block() } }

    /**
     * Raw JDBC transaction on the IO dispatcher: commits when [block] returns, rolls back when it throws.
     */
    suspend fun <T> tx(block: (Connection) -> T): T = withContext(Dispatchers.IO) {
        dataSource.connection.use { connection ->
            try {
                val result = block(connection)
                connection.commit()
                result
            } catch (e: Throwable) {
                connection.rollback()
                throw e
            }
        }
    }

    suspend fun isHealthy(): Boolean = withContext(Dispatchers.IO) {
        try {
            dataSource.connection.use { it.isValid(2) }
        } catch (e: Exception) {
            false
        }
    }

    override fun close() = dataSource.close()
}
