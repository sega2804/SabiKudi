package com.crypticsamsara.sabikudi.core.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.crypticsamsara.sabikudi.core.data.converters.Converters
import com.crypticsamsara.sabikudi.core.data.dao.GroupDao
import com.crypticsamsara.sabikudi.core.data.dao.InventoryDao
import com.crypticsamsara.sabikudi.core.data.dao.PersonDao
import com.crypticsamsara.sabikudi.core.data.dao.ReceiptDao
import com.crypticsamsara.sabikudi.core.data.entities.Group
import com.crypticsamsara.sabikudi.core.data.entities.GroupMember
import com.crypticsamsara.sabikudi.core.data.entities.InventoryEntry
import com.crypticsamsara.sabikudi.core.data.entities.LineItem
import com.crypticsamsara.sabikudi.core.data.entities.Person
import com.crypticsamsara.sabikudi.core.data.entities.Receipt
import com.crypticsamsara.sabikudi.core.data.entities.SplitAssignment

@Database(
    entities = [
        Receipt::class,
        LineItem::class,
        SplitAssignment::class,
        Person::class,
        Group::class,
        GroupMember::class,
        InventoryEntry::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class SabikudiDatabase : RoomDatabase() {
    abstract fun receiptDao(): ReceiptDao
    abstract fun lineItemDao(): LineItemDao
    abstract fun splitAssignmentDao(): SplitAssignmentDao
    abstract fun personDao(): PersonDao
    abstract fun groupDao(): GroupDao
    abstract fun inventoryDao(): InventoryDao
}
