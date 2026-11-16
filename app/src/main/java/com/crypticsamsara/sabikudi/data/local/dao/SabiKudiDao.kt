package com.crypticsamsara.sabikudi.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.crypticsamsara.sabikudi.data.local.entities.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PeopleDao {
    @Query("SELECT * FROM people ORDER BY name ASC")
    fun getAllPeople(): Flow<List<PersonEntity>>

    @Query("SELECT * FROM people WHERE isArchived = 0 ORDER BY name ASC")
    fun getActivePeople(): Flow<List<PersonEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPerson(person: PersonEntity): Long

    @Update
    suspend fun updatePerson(person: PersonEntity)
}

@Dao
interface GroupsDao {
    @Query("SELECT * FROM groups ORDER BY name ASC")
    fun getAllGroups(): Flow<List<GroupEntity>>

    @Query("SELECT * FROM groups WHERE id = :groupId")
    suspend fun getGroupById(groupId: Long): GroupEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: GroupEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemberCrossRef(crossRef: GroupMemberCrossRef)

    @Query("DELETE FROM group_members WHERE groupId = :groupId")
    suspend fun deleteMembersForGroup(groupId: Long)

    @Query("SELECT p.* FROM people p INNER JOIN group_members gm ON p.id = gm.personId WHERE gm.groupId = :groupId")
    fun getMembersForGroup(groupId: Long): Flow<List<PersonEntity>>
}

@Dao
interface ReceiptsDao {
    @Query("SELECT * FROM receipts ORDER BY id DESC")
    fun getAllReceipts(): Flow<List<ReceiptEntity>>

    @Query("SELECT * FROM receipts WHERE mode = :mode ORDER BY id DESC")
    fun getReceiptsByMode(mode: String): Flow<List<ReceiptEntity>>

    @Query("SELECT * FROM receipts WHERE status = :status ORDER BY id DESC")
    fun getReceiptsByStatus(status: String): Flow<List<ReceiptEntity>>

    @Query("SELECT * FROM receipts WHERE id = :id")
    suspend fun getReceiptById(id: Long): ReceiptEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReceipt(receipt: ReceiptEntity): Long

    @Update
    suspend fun updateReceipt(receipt: ReceiptEntity)

    @Query("UPDATE receipts SET status = :status, rejectionReason = :reason WHERE id = :id")
    suspend fun updateReceiptStatus(id: Long, status: String, reason: String? = null)

    @Query("SELECT * FROM receipt_items WHERE receiptId = :receiptId")
    fun getItemsForReceipt(receiptId: Long): Flow<List<ReceiptItemEntity>>

    @Query("SELECT * FROM receipt_items WHERE id = :itemId")
    suspend fun getReceiptItemById(itemId: Long): ReceiptItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReceiptItem(item: ReceiptItemEntity): Long

    @Update
    suspend fun updateReceiptItem(item: ReceiptItemEntity)

    @Query("DELETE FROM receipt_items WHERE id = :itemId")
    suspend fun deleteReceiptItem(itemId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSplitAssignment(assignment: ItemSplitAssignmentEntity)

    @Query("SELECT * FROM item_split_assignments WHERE receiptItemId = :receiptItemId")
    fun getSplitAssignmentsForItem(receiptItemId: Long): Flow<List<ItemSplitAssignmentEntity>>

    @Query("DELETE FROM item_split_assignments WHERE receiptItemId = :receiptItemId")
    suspend fun deleteSplitAssignmentsForItem(receiptItemId: Long)

    @Query("UPDATE receipt_items SET isStale = :isStale WHERE id = :receiptItemId")
    suspend fun markItemStale(receiptItemId: Long, isStale: Boolean)
}

@Dao
interface BalancesDao {
    @Query("SELECT * FROM balances WHERE mode = :mode")
    fun getBalancesByMode(mode: String): Flow<List<BalanceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBalance(balance: BalanceEntity): Long

    @Update
    suspend fun updateBalance(balance: BalanceEntity)

    @Query("DELETE FROM balances WHERE id = :id")
    suspend fun deleteBalance(id: Long)
}

@Dao
interface InventoryDao {
    @Query("SELECT * FROM inventory ORDER BY name ASC")
    fun getAllInventory(): Flow<List<InventoryEntity>>

    @Query("SELECT * FROM inventory WHERE id = :id")
    suspend fun getInventoryById(id: Long): InventoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInventoryItem(item: InventoryEntity): Long

    @Update
    suspend fun updateInventoryItem(item: InventoryEntity)
}

@Dao
interface StaffDevicesDao {
    @Query("SELECT * FROM staff_devices ORDER BY staffName ASC")
    fun getAllStaffDevices(): Flow<List<StaffDeviceEntity>>

    @Query("SELECT * FROM staff_devices WHERE id = :id")
    suspend fun getStaffDeviceById(id: Long): StaffDeviceEntity?

    @Query("SELECT * FROM staff_devices WHERE inviteCode = :code")
    suspend fun getStaffDeviceByInviteCode(code: String): StaffDeviceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStaffDevice(device: StaffDeviceEntity): Long

    @Update
    suspend fun updateStaffDevice(device: StaffDeviceEntity)

    @Query("DELETE FROM staff_devices WHERE id = :id")
    suspend fun deleteStaffDevice(id: Long)
}

@Dao
interface AuditDao {
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<AuditLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: AuditLogEntity)
}

@Dao
interface SettlementHistoryDao {
    @Query("SELECT * FROM settlement_history ORDER BY timestamp DESC")
    fun getAllSettlements(): Flow<List<SettlementHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettlement(settlement: SettlementHistoryEntity): Long

    @Query("DELETE FROM settlement_history WHERE id = :id")
    suspend fun deleteSettlement(id: Long)
}

@Dao
interface RecurringExpenseDao {
    @Query("SELECT * FROM recurring_expenses ORDER BY id ASC")
    fun getAllRecurring(): Flow<List<RecurringExpenseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecurring(expense: RecurringExpenseEntity)
}

@Dao
interface GroupMemoryDao {
    @Query("SELECT * FROM group_memory WHERE merchantName = :merchantName LIMIT 1")
    suspend fun getGroupMemoryForMerchant(merchantName: String): GroupMemoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveGroupMemory(memory: GroupMemoryEntity)
}

@Dao
interface WorkspacesDao {
    @Query("SELECT * FROM workspaces ORDER BY id ASC")
    fun getAllWorkspaces(): Flow<List<WorkspaceEntity>>

    @Query("SELECT * FROM workspaces WHERE isCurrent = 1 LIMIT 1")
    fun getCurrentWorkspace(): Flow<WorkspaceEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkspace(workspace: WorkspaceEntity): Long

    @Query("UPDATE workspaces SET isCurrent = CASE WHEN id = :workspaceId THEN 1 ELSE 0 END")
    suspend fun setCurrentWorkspace(workspaceId: Long)

    @Query("SELECT * FROM workspace_memberships WHERE workspaceId = :workspaceId")
    fun getMembershipsForWorkspace(workspaceId: Long): Flow<List<WorkspaceMembershipEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMembership(membership: WorkspaceMembershipEntity)
}
