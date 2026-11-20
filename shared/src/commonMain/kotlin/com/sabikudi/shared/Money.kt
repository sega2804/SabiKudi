package com.sabikudi.shared

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.math.abs

@Serializable
@JvmInline
value class Kobo(val value: Long) {
    operator fun plus(other: Kobo) = Kobo(value + other.value)
    operator fun minus(other: Kobo) = Kobo(value - other.value)

    fun formatNaira(): String {
        val sign = if (value < 0) "-" else ""
        val absolute = abs(value)
        val naira = absolute / 100
        val kobo = (absolute % 100).toString().padStart(2, '0')
        return "$sign₦$naira.$kobo"
    }
}

/** Splits [total] so the shares always sum exactly to [total]. Earlier shares absorb the remainder. */
fun splitEvenly(total: Kobo, parts: Int): List<Kobo> {
    require(parts > 0) { "parts must be positive" }
    require(total.value >= 0) { "total must not be negative" }
    val base = total.value / parts
    val remainder = (total.value % parts).toInt()
    return List(parts) { i -> Kobo(base + if (i < remainder) 1 else 0) }
}
