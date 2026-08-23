package com.crypticsamsara.sabikudi.core.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "inventory_entries",
    foreignKeys = [
        ForeignKey(
            entity = LineItem::class,
            parentColumns = ["id"],
            childColumns = ["lineItemId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("lineItemId")]
)
data class InventoryEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val lineItemId: Long,
    val category: String,
    val costBasisMinor: Long,
    val quantityOnHand: Double,
    val lowStockThreshold: Double
)
