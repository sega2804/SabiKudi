package com.crypticsamsara.sabikudi.di

import android.content.Context
import com.crypticsamsara.sabikudi.data.local.SabiKudiDatabase
import com.crypticsamsara.sabikudi.data.local.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideApplicationScope(): CoroutineScope {
        return CoroutineScope(SupervisorJob())
    }

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
        scope: CoroutineScope
    ): SabiKudiDatabase {
        return SabiKudiDatabase.getDatabase(context, scope)
    }

    @Provides
    fun providePeopleDao(database: SabiKudiDatabase): PeopleDao = database.peopleDao()

    @Provides
    fun provideGroupsDao(database: SabiKudiDatabase): GroupsDao = database.groupsDao()

    @Provides
    fun provideReceiptsDao(database: SabiKudiDatabase): ReceiptsDao = database.receiptsDao()

    @Provides
    fun provideBalancesDao(database: SabiKudiDatabase): BalancesDao = database.balancesDao()

    @Provides
    fun provideInventoryDao(database: SabiKudiDatabase): InventoryDao = database.inventoryDao()

    @Provides
    fun provideStaffDevicesDao(database: SabiKudiDatabase): StaffDevicesDao = database.staffDevicesDao()

    @Provides
    fun provideAuditDao(database: SabiKudiDatabase): AuditDao = database.auditDao()

    @Provides
    fun provideSettlementHistoryDao(database: SabiKudiDatabase): SettlementHistoryDao = database.settlementHistoryDao()

    @Provides
    fun provideRecurringExpenseDao(database: SabiKudiDatabase): RecurringExpenseDao = database.recurringExpenseDao()

    @Provides
    fun provideGroupMemoryDao(database: SabiKudiDatabase): GroupMemoryDao = database.groupMemoryDao()

    @Provides
    fun provideWorkspacesDao(database: SabiKudiDatabase): WorkspacesDao = database.workspacesDao()
}
