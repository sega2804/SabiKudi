package com.crypticsamsara.sabikudi.core.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.crypticsamsara.sabikudi.core.data.entities.Group
import java.lang.reflect.Member

@Dao
interface GroupDao {
    @Insert
    suspend fun insertGroup(group: Group): Long

    @Update
    suspend fun updateGroup(group: Group)

    @Delete
    suspend fun deleteGroup(group: Group)

    @Insert
    suspend fun addMember(member: GroupMember): Long

    @Delete
    suspend fun removeMember(member: GroupMember)

    @Query("SELECT * FROM groups ORDER BY name ASC")
    fun observeAll() : Flow<List<Group>>

    @Query(
        """
            SELECT p.* FROM people p
            INNER JOIN group_members gm ON gm.person_id = p.id
            WHERE gm.group_id = :groupId AND p.status = 'ACTIVE'
            ORDER BY p.name ASC
        """
    )
    fun observeMembers(groupId: Long) : Flow<List<Person>>
}