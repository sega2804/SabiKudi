package com.crypticsamsara.sabikudi.data.repository

import com.crypticsamsara.sabikudi.data.local.dao.GroupMemoryDao
import com.crypticsamsara.sabikudi.data.local.entities.GroupMemoryEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GroupMemoryRepository @Inject constructor(
    private val groupMemoryDao: GroupMemoryDao
) {
    suspend fun getSuggestedGroupForMerchant(merchantName: String): Long? {
        return groupMemoryDao.getGroupMemoryForMerchant(merchantName.trim())?.groupId
    }

    suspend fun saveGroupMemory(merchantName: String, groupId: Long) {
        if (merchantName.isNotBlank()) {
            groupMemoryDao.saveGroupMemory(
                GroupMemoryEntity(
                    merchantName = merchantName.trim(),
                    groupId = groupId,
                    lastUsedTimestamp = System.currentTimeMillis()
                )
            )
        }
    }
}
