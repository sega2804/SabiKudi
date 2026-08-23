package com.crypticsamsara.sabikudi.core.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.crypticsamsara.sabikudi.core.data.entities.InventoryEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryDao {
    @Insert
    suspend fun insert(entry: InventoryEntry)

    @Update
    suspend fun update(entry: InventoryEntry)

    @@Query("SELECT * FROM inventory_entries ORDER BY category ASC")
    fun observeAll(): Flow<List<InventoryEntry>>

    @Query("SELECT * FROM inventory_entries WHERE quantityOnHand <= lowStockThreshold")
    fun observeLowStock(): Flow<List<InventoryEntry>>
}