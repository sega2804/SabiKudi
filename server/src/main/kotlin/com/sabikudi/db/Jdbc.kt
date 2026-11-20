package com.sabikudi.db

import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.time.OffsetDateTime
import java.util.UUID

/** Runs a query and maps the first row, or returns null when there is none. */
fun <T> Connection.one(sql: String, vararg params: Any?, map: (ResultSet) -> T): T? =
    prepareStatement(sql).use { ps ->
        ps.bind(params)
        ps.executeQuery().use { rs -> if (rs.next()) map(rs) else null }
    }

/** Runs an INSERT, UPDATE or DELETE and returns the number of rows changed. */
fun Connection.execute(sql: String, vararg params: Any?): Int =
    prepareStatement(sql).use { ps ->
        ps.bind(params)
        ps.executeUpdate()
    }

private fun PreparedStatement.bind(params: Array<out Any?>) {
    params.forEachIndexed { index, value -> setObject(index + 1, value) }
}

fun ResultSet.uuid(column: String): UUID = getObject(column, UUID::class.java)
fun ResultSet.uuidOrNull(column: String): UUID? = getObject(column, UUID::class.java)
fun ResultSet.intOrNull(column: String): Int? = getInt(column).takeUnless { wasNull() }
fun ResultSet.offsetDateTime(column: String): OffsetDateTime = getObject(column, OffsetDateTime::class.java)
