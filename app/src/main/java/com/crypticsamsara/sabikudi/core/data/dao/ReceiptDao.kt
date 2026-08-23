package com.crypticsamsara.sabikudi.core.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.crypticsamsara.sabikudi.core.data.entities.Receipt
import kotlinx.coroutines.flow.Flow

@Dao
interface ReceiptDao {
    @Insert
    suspend fun insert(receipt: Receipt): Long

    @Update
    suspend fun update(receipt: Receipt)

    @Delete
    suspend fun delete(receipt: Receipt)

    @Query("SELECT * FROM receipts WHERE id = :id")
    suspend fun getById(id: Long): Receipt?

    @Query("SELECT * FROM receipts ORDER BY date DESC")
    fun observeAll(): Flow<List<Receipt>>

    @Query("SELECT * FROM receipts WHERE groupId = :groupId ORDER BY date DESC")
    fun observeByGroup(groupId: Long): Flow<List<Receipt>>


    @Query(
        """
        SELECT * FROM receipts
        WHERE imageRetentionOverride = 0 AND imageUri IS NOT NULL AND date < :cutoffMillis
        """
    )
    suspend fun getReceiptsEligibleForImagePurge(cutoffMillis: Long): List<Receipt>

    /** Nulls out imageUri after the worker has deleted the underlying file. */
    @Query("UPDATE receipts SET imageUri = NULL WHERE id = :id")
    suspend fun clearImage(id: Long)
}