package com.crypticsamsara.sabikudi.core.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey


enum class SplitAssignmentStatus { ACTIVE , STALE}

@Entity(
    tableName = "split_assignments",
    foreignKeys = [
        ForeignKey(
            entity = LineItem::class,
            parentColumns = ["id"],
            childColumns = ["lineItemId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Person::class,
            parentColumns = ["id"],
            childColumns = ["personId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("lineItemId"), Index("personId")]
)
data class SplitAssignment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val  lineItemId: Long,
    val personId: Long,
    val shareAmountMinor: Long,
    val status: SplitAssignmentStatus = SplitAssignmentStatus.ACTIVE,
    val settled: Boolean = false,
    val settledAt: Long? = null
)
