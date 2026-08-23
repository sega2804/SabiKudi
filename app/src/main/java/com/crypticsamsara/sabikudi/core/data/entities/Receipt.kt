package com.crypticsamsara.sabikudi.core.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey


enum class ReceiptSource { CAMERA, MANUAL}

@Entity(
    tableName = "receipts",
    foreignKeys = [
        ForeignKey(
            entity = Group::class,
            parentColumns = ["id"],
            childColumns = ["groupId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = Person::class,
            parentColumns = ["id"],
            childColumns = ["paidByPersonId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("groupId"), Index("paidByPersonId")]
)

data class Receipt(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val source: ReceiptSource,
    val merchantName: String?,
    val date: Long,
    val totalAmountMinor: Long,
    val imageUri: String?,
    val groupId: Long?,
    val paidByPersonId: Long? = null,
    val imageRetentionOverride: Boolean = false
)
