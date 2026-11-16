package com.crypticsamsara.sabikudi.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.crypticsamsara.sabikudi.data.local.dao.*
import com.crypticsamsara.sabikudi.data.local.entities.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        PersonEntity::class,
        GroupEntity::class,
        GroupMemberCrossRef::class,
        ReceiptEntity::class,
        ReceiptItemEntity::class,
        ItemSplitAssignmentEntity::class,
        BalanceEntity::class,
        InventoryEntity::class,
        StaffDeviceEntity::class,
        AuditLogEntity::class,
        SettlementHistoryEntity::class,
        RecurringExpenseEntity::class,
        GroupMemoryEntity::class,
        WorkspaceEntity::class,
        WorkspaceMembershipEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class SabiKudiDatabase : RoomDatabase() {

    abstract fun peopleDao(): PeopleDao
    abstract fun groupsDao(): GroupsDao
    abstract fun receiptsDao(): ReceiptsDao
    abstract fun balancesDao(): BalancesDao
    abstract fun inventoryDao(): InventoryDao
    abstract fun staffDevicesDao(): StaffDevicesDao
    abstract fun auditDao(): AuditDao
    abstract fun settlementHistoryDao(): SettlementHistoryDao
    abstract fun recurringExpenseDao(): RecurringExpenseDao
    abstract fun groupMemoryDao(): GroupMemoryDao
    abstract fun workspacesDao(): WorkspacesDao

    companion object {
        @Volatile
        private var INSTANCE: SabiKudiDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): SabiKudiDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SabiKudiDatabase::class.java,
                    "sabikudi_db"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateDatabase(database)
                }
            }
        }

        suspend fun populateDatabase(db: SabiKudiDatabase) {
            // Seed People
            val adaId = db.peopleDao().insertPerson(PersonEntity(name = "Ada", isArchived = false))
            val chidiId = db.peopleDao().insertPerson(PersonEntity(name = "Chidi", isArchived = false))
            val ngoziId = db.peopleDao().insertPerson(PersonEntity(name = "Ngozi", isArchived = false))
            val tundeId = db.peopleDao().insertPerson(PersonEntity(name = "Tunde", isArchived = false))
            db.peopleDao().insertPerson(PersonEntity(name = "Emeka", isArchived = true))

            // Seed Groups
            val flatmatesId = db.groupsDao().insertGroup(GroupEntity(name = "Flatmates"))
            db.groupsDao().insertMemberCrossRef(GroupMemberCrossRef(flatmatesId, adaId))
            db.groupsDao().insertMemberCrossRef(GroupMemberCrossRef(flatmatesId, chidiId))
            db.groupsDao().insertMemberCrossRef(GroupMemberCrossRef(flatmatesId, ngoziId))

            val shopStaffId = db.groupsDao().insertGroup(GroupEntity(name = "Shop staff"))
            db.groupsDao().insertMemberCrossRef(GroupMemberCrossRef(shopStaffId, tundeId))
            db.groupsDao().insertMemberCrossRef(GroupMemberCrossRef(shopStaffId, adaId))

            // Seed Workspaces
            val w1Id = db.workspacesDao().insertWorkspace(WorkspaceEntity(name = "Main Shop", mode = "BUSINESS", inviteCode = "SBK-1001", isCurrent = true))
            val w2Id = db.workspacesDao().insertWorkspace(WorkspaceEntity(name = "Flatmates Expenses", mode = "SHARED", inviteCode = "FLT-2002", isCurrent = false))
            db.workspacesDao().insertMembership(WorkspaceMembershipEntity(workspaceId = w1Id, role = "OWNER", memberName = "Ada"))
            db.workspacesDao().insertMembership(WorkspaceMembershipEntity(workspaceId = w1Id, role = "STAFF", memberName = "Tunde"))
            db.workspacesDao().insertMembership(WorkspaceMembershipEntity(workspaceId = w2Id, role = "ORGANISER", memberName = "Chidi"))
            db.workspacesDao().insertMembership(WorkspaceMembershipEntity(workspaceId = w2Id, role = "MEMBER", memberName = "Ada"))
            db.workspacesDao().insertMembership(WorkspaceMembershipEntity(workspaceId = w2Id, role = "MEMBER", memberName = "Ngozi"))

            // Seed Group Memory (Remembered Group for Shoprite Ikeja)
            db.groupMemoryDao().saveGroupMemory(GroupMemoryEntity(merchantName = "Shoprite Ikeja", groupId = flatmatesId))

            // Seed Staff Devices
            db.staffDevicesDao().insertStaffDevice(StaffDeviceEntity(staffName = "Tunde", inviteCode = "SBK-8821", status = "active", offlineLimitDays = 7))
            db.staffDevicesDao().insertStaffDevice(StaffDeviceEntity(staffName = "Ngozi", inviteCode = "SBK-9412", status = "pending", offlineLimitDays = 7))

            // Seed Inventory
            db.inventoryDao().insertInventoryItem(InventoryEntity(name = "Indomie carton", category = "Groceries", quantity = 3, threshold = 5, expiryDate = "2026-11-25"))
            db.inventoryDao().insertInventoryItem(InventoryEntity(name = "Nylon bags", category = "Packaging", quantity = 40, threshold = 20))
            db.inventoryDao().insertInventoryItem(InventoryEntity(name = "Soap 500ml", category = "Toiletries", quantity = 2, threshold = 6, expiryDate = "2027-03-01"))
            db.inventoryDao().insertInventoryItem(InventoryEntity(name = "Bottled water", category = "Beverages", quantity = 48, threshold = 24))
            db.inventoryDao().insertInventoryItem(InventoryEntity(name = "Receipt rolls", category = "Supplies", quantity = 4, threshold = 10))
            db.inventoryDao().insertInventoryItem(InventoryEntity(name = "Garri 5kg", category = "Groceries", quantity = 12, threshold = 5))

            // Seed Balances (SHARED)
            db.balancesDao().insertBalance(BalanceEntity(mode = "SHARED", debtorName = "Ada", creditorName = "Chidi", amountKobo = 200000, itemCount = 2))
            db.balancesDao().insertBalance(BalanceEntity(mode = "SHARED", debtorName = "Ngozi", creditorName = "Chidi", amountKobo = 125000, itemCount = 1, isStale = true))
            db.balancesDao().insertBalance(BalanceEntity(mode = "SHARED", debtorName = "Tunde", creditorName = "Ada", amountKobo = 310000, itemCount = 2))

            // Seed Balances (BUSINESS)
            db.balancesDao().insertBalance(BalanceEntity(mode = "BUSINESS", debtorName = "Tunde", creditorName = "Ada", amountKobo = 450000, itemCount = 3))
            db.balancesDao().insertBalance(BalanceEntity(mode = "BUSINESS", debtorName = "Ada", creditorName = "Tunde", amountKobo = 180000, itemCount = 1))

            // Seed Recurring
            db.recurringExpenseDao().insertRecurring(RecurringExpenseEntity(name = "Rent share", amountKobo = 15000000, groupName = "Flatmates"))
            db.recurringExpenseDao().insertRecurring(RecurringExpenseEntity(name = "Internet", amountKobo = 1500000, groupName = "Flatmates"))

            // Seed Receipts
            val r1Id = db.receiptsDao().insertReceipt(
                ReceiptEntity(merchant = "Shoprite Lekki", displayDate = "Nov 12", isoDate = "2026-11-12", mode = "SHARED", paidByPersonName = "Chidi", isPending = false, createdByRole = "organiser")
            )
            db.receiptsDao().insertReceiptItem(ReceiptItemEntity(receiptId = r1Id, name = "Rice 5kg", priceAmountKobo = 850000, destination = "S", category = "Groceries", assignedPeopleOrGroup = "Ada, Chidi"))
            db.receiptsDao().insertReceiptItem(ReceiptItemEntity(receiptId = r1Id, name = "Chicken", priceAmountKobo = 1240000, destination = "S", category = "Groceries", assignedPeopleOrGroup = "Flatmates"))

            val r2Id = db.receiptsDao().insertReceipt(
                ReceiptEntity(merchant = "Mile 12 Market", displayDate = "Nov 10", isoDate = "2026-11-10", mode = "BUSINESS", paidByPersonName = "Ada", isPending = false, createdByRole = "owner")
            )
            db.receiptsDao().insertReceiptItem(ReceiptItemEntity(receiptId = r2Id, name = "Indomie carton", priceAmountKobo = 1800000, destination = "K", category = "Groceries"))
            db.receiptsDao().insertReceiptItem(ReceiptItemEntity(receiptId = r2Id, name = "Nylon bags", priceAmountKobo = 320000, destination = "K", category = "Packaging"))
            db.receiptsDao().insertReceiptItem(ReceiptItemEntity(receiptId = r2Id, name = "Cleaning help", priceAmountKobo = 450000, destination = "S", category = "Supplies", assignedPeopleOrGroup = "Tunde"))

            // Seed Audit Log
            db.auditDao().insertLog(AuditLogEntity(logMessage = "Nov 12 · Receipt \"Shoprite Lekki\" added to balances"))
        }
    }
}
