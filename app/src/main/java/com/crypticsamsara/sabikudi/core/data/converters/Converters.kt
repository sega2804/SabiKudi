package com.crypticsamsara.sabikudi.core.data.converters

import androidx.room.TypeConverter
import com.crypticsamsara.sabikudi.core.data.entities.LineItemDestination
import com.crypticsamsara.sabikudi.core.data.entities.PersonStatus
import com.crypticsamsara.sabikudi.core.data.entities.ReceiptSource
import com.crypticsamsara.sabikudi.core.data.entities.SplitAssignmentStatus

class Converters {
    @TypeConverter
    fun fromReceiptSource(value: ReceiptSource): String = value.name
    @TypeConverter
    fun toReceiptSource(value: String): ReceiptSource = ReceiptSource.valueOf(value)

    @TypeConverter
    fun fromLineItemDestination(value: LineItemDestination): String = value.name
    @TypeConverter
    fun toLineItemDestination(value: String): LineItemDestination = LineItemDestination.valueOf(value)

    @TypeConverter
    fun fromSplitAssignmentStatus(value: SplitAssignmentStatus): String = value.name
    @TypeConverter
    fun toSplitAssignmentStatus(value: String): SplitAssignmentStatus = SplitAssignmentStatus.valueOf(value)

    @TypeConverter
    fun fromPersonStatus(value: PersonStatus): String = value.name
    @TypeConverter
    fun toPersonStatus(value: String): PersonStatus = PersonStatus.valueOf(value)
}