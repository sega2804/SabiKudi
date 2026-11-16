package com.crypticsamsara.sabikudi.data.repository

import com.crypticsamsara.sabikudi.data.local.dao.WorkspacesDao
import com.crypticsamsara.sabikudi.data.local.entities.WorkspaceEntity
import com.crypticsamsara.sabikudi.data.local.entities.WorkspaceMembershipEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkspaceRepository @Inject constructor(
    private val workspacesDao: WorkspacesDao
) {
    val allWorkspaces: Flow<List<WorkspaceEntity>> = workspacesDao.getAllWorkspaces()
    val currentWorkspace: Flow<WorkspaceEntity?> = workspacesDao.getCurrentWorkspace()

    suspend fun switchWorkspace(workspaceId: Long) {
        workspacesDao.setCurrentWorkspace(workspaceId)
    }

    suspend fun createWorkspace(name: String, mode: String, role: String, memberName: String): Long {
        val inviteCode = if (mode == "BUSINESS") "SBK-${(1000..9999).random()}" else "FLT-${(1000..9999).random()}"
        val workspaceId = workspacesDao.insertWorkspace(
            WorkspaceEntity(
                name = name,
                mode = mode,
                inviteCode = inviteCode,
                isCurrent = true
            )
        )
        workspacesDao.setCurrentWorkspace(workspaceId)
        workspacesDao.insertMembership(
            WorkspaceMembershipEntity(
                workspaceId = workspaceId,
                role = role,
                memberName = memberName
            )
        )
        return workspaceId
    }

    fun getMemberships(workspaceId: Long): Flow<List<WorkspaceMembershipEntity>> {
        return workspacesDao.getMembershipsForWorkspace(workspaceId)
    }
}
