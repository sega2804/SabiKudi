package com.crypticsamsara.sabikudi.core.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.crypticsamsara.sabikudi.core.data.entities.LineItem
import com.crypticsamsara.sabikudi.core.data.entities.LineItemDestination
import kotlinx.coroutines.flow.Flow


@Dao
interface LineItemDao {
    @Insert
    suspend fun insert(lineItem: LineItem): Long

    @Insert
    suspend fun insertAll(lineItems: List<LineItem>): List<Long>

    @Update
    suspend fun update(lineItem: LineItem)

    @Query("SELECT * FROM line_items WHERE receiptId = :receiptId")
    fun observeByReceipt(receiptId: Long): Flow<List<LineItem>>

    @Query("SELECT * FROM line_items WHERE id = :id")
    suspend fun getById(id: Long): LineItem?

    @Query("UPDATE line_items SET destination = :destination WHERE id = :id")
    suspend fun setDestination(id: Long, destination: LineItemDestination)
}
