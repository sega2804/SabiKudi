package com.crypticsamsara.sabikudi.core.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.crypticsamsara.sabikudi.core.data.entities.Person
import com.crypticsamsara.sabikudi.core.data.entities.PersonStatus
import kotlinx.coroutines.flow.Flow


@Dao
interface PersonDao {
    @Insert
    suspend fun insert(person: Person): Long

    @Update
    suspend fun update(person: Person)

    @Delete
    suspend fun delete(person: Person)

    @Query("UPDATE people SET status = :status WHERE id = :id")
    suspend fun setStatus(id: Long, status: PersonStatus)

    @Query("SELECT * FROM people WHERE status = 'ACTIVE' ORDER BY name ASC")
    fun observeActive(): Flow<List<Person>>

    @Query("SELECT * FROM people WHERE id = :id")
    suspend fun getById(id: Long): Person?
}