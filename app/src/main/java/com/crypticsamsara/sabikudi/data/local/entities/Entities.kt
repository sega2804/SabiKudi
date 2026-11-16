package com.crypticsamsara.sabikudi.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "people")
data class PersonEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val isArchived: Boolean = false
)

@Entity(tableName = "groups")
data class GroupEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String
)

@Entity(
    tableName = "group_members",
    primaryKeys = ["groupId", "personId"],
    foreignKeys = [
        ForeignKey(
            entity = GroupEntity::class,
            parentColumns = ["id"],
            childColumns = ["groupId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = PersonEntity::class,
            parentColumns = ["id"],
            childColumns = ["personId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("groupId"), Index("personId")]
)
data class GroupMemberCrossRef(
    val groupId: Long,
    val personId: Long
)

@Entity(tableName = "receipts")
data class ReceiptEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val merchant: String,
    val displayDate: String,
    val isoDate: String,
    val mode: String, // BUSINESS or SHARED
    val paidByPersonName: String,
    val isPending: Boolean = true,
    val createdByRole: String, // owner, staff, organiser, member
    val isKeepPermanent: Boolean = false,
    val photoUri: String? = null,
    val staffPhotoPurgeAt: Long? = null,
    val status: String = "approved", // approved, pending, rejected
    val rejectionReason: String? = null
)

@Entity(
    tableName = "receipt_items",
    foreignKeys = [
        ForeignKey(
            entity = ReceiptEntity::class,
            parentColumns = ["id"],
            childColumns = ["receiptId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("receiptId")]
)
data class ReceiptItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val receiptId: Long,
    val name: String,
    val priceAmountKobo: Long,
    val destination: String, // U (Unassigned), K (Stock), S (Split)
    val category: String = "Other",
    val stkQuantity: Int = 10,
    val stkThreshold: Int = 3,
    val staleOriginalPriceKobo: Long? = null,
    val note: String? = null,
    val assignedPeopleOrGroup: String? = null,
    val splitType: String = "equal", // equal, percent, exact, shares
    val isStale: Boolean = false
)

@Entity(
    tableName = "item_split_assignments",
    foreignKeys = [
        ForeignKey(
            entity = ReceiptItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["receiptItemId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("receiptItemId")]
)
data class ItemSplitAssignmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val receiptItemId: Long,
    val personName: String,
    val amountKobo: Long
)

@Entity(tableName = "balances")
data class BalanceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val mode: String, // BUSINESS or SHARED
    val debtorName: String,
    val creditorName: String,
    val amountKobo: Long,
    val itemCount: Int,
    val isStale: Boolean = false,
    val isPaymentReported: Boolean = false
)

@Entity(tableName = "inventory")
data class InventoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String,
    val quantity: Int,
    val threshold: Int,
    val costBasisKobo: Long = 3500000,
    val expiryDate: String? = null,
    val marginPercent: Int = 30,
    val sellPriceKobo: Long? = null
)

@Entity(tableName = "staff_devices")
data class StaffDeviceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val staffName: String,
    val inviteCode: String,
    val status: String = "active", // pending, active, suspended, locked, removed
    val offlineLimitDays: Int = 7, // 3 to 14 days
    val lastSyncTimestamp: Long = System.currentTimeMillis(),
    val permissionsMask: Long = 0x3FL // bitwise permissions
)

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val logMessage: String
)

@Entity(tableName = "settlement_history")
data class SettlementHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val mode: String,
    val debtorName: String,
    val creditorName: String,
    val amountKobo: Long,
    val isFullSettlement: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "recurring_expenses")
data class RecurringExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val amountKobo: Long,
    val groupName: String = "Flatmates"
)

@Entity(tableName = "group_memory")
data class GroupMemoryEntity(
    @PrimaryKey val merchantName: String,
    val groupId: Long,
    val lastUsedTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "workspaces")
data class WorkspaceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val mode: String, // BUSINESS or SHARED
    val inviteCode: String,
    val isCurrent: Boolean = false
)

@Entity(tableName = "workspace_memberships")
data class WorkspaceMembershipEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val workspaceId: Long,
    val role: String, // OWNER, STAFF, ORGANISER, MEMBER
    val memberName: String
)
