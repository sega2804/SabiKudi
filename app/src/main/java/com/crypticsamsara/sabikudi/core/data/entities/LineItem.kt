package com.crypticsamsara.sabikudi.core.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey


enum class LineItemDestination { SPLIT, STOCK, UNASSIGNED}

@Entity(
    tableName = "line_items",
    foreignKeys = [
        ForeignKey(
            entity = Receipt::class,
            parentColumns = ["id"],
            childColumns = ["receiptId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("receiptId")]
)
data class LineItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val receiptId: Long,
    val rawOcrText: String?,
    val name: String,
    val quantity: Double,
    val priceMinor: Long,
    val destination: LineItemDestination = LineItemDestination.UNASSIGNED
)
