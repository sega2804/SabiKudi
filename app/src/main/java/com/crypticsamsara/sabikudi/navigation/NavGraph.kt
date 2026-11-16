package com.crypticsamsara.sabikudi.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.crypticsamsara.sabikudi.data.preferences.AppMode
import com.crypticsamsara.sabikudi.ui.MainViewModel
import com.crypticsamsara.sabikudi.ui.screens.main.MainShellScreen
import com.crypticsamsara.sabikudi.ui.screens.mgmt.PeopleGroupsScreen
import com.crypticsamsara.sabikudi.ui.screens.mgmt.StaffDevicesScreen
import com.crypticsamsara.sabikudi.ui.screens.onboarding.AuthScreen
import com.crypticsamsara.sabikudi.ui.screens.onboarding.ModeSelectionScreen
import com.crypticsamsara.sabikudi.ui.screens.onboarding.RoleSelectionScreen
import com.crypticsamsara.sabikudi.ui.screens.onboarding.StaffOnboardingScreen
import com.crypticsamsara.sabikudi.ui.screens.receipt.*
import com.crypticsamsara.sabikudi.ui.screens.security.AppLockScreen
import com.crypticsamsara.sabikudi.ui.screens.splash.SplashScreen
import com.crypticsamsara.sabikudi.ui.screens.tools.*

object Destinations {
    const val SPLASH = "splash"
    const val AUTH = "auth"
    const val MODE_SELECTION = "mode_selection"
    const val ROLE_SELECTION = "role_selection/{mode}"
    const val MAIN_SHELL = "main_shell"
    const val STAFF_ONBOARDING = "staff_onboarding"
    const val STAFF_DEVICES = "staff_devices"

    const val MANUAL_RECEIPT = "manual_receipt"
    const val CAMERA_SCAN = "camera_scan"
    const val RECEIPT_REVIEW = "receipt_review"
    const val ADD_STOCK = "add_stock/{itemIndex}"
    const val STOCK_DETAIL = "stock_detail/{itemId}"
    const val CHOOSE_GROUP = "choose_group"
    const val SPLIT_ASSIGNMENT = "split_assignment"
    const val SPLIT_REVIEW = "split_review"
    const val RECEIPT_DETAIL = "receipt_detail/{receiptId}"
    const val PEOPLE_GROUPS = "people_groups"

    const val TOOLS_HUB = "tools_hub"
    const val SETTLEMENT_HISTORY = "settlement_history"
    const val RECURRING_EXPENSES = "recurring_expenses"
    const val RESTOCK_EXPIRY = "restock_expiry"
    const val SPENDING = "spending"
    const val AUDIT_TRAIL = "audit_trail"
    const val STATEMENT = "statement"
    const val BACKUP_RESTORE = "backup_restore"
    const val APP_LOCK = "app_lock"

    fun roleSelectionRoute(mode: AppMode) = "role_selection/${mode.name}"
    fun addStockRoute(itemIndex: Int) = "add_stock/$itemIndex"
    fun stockDetailRoute(itemId: Long) = "stock_detail/$itemId"
    fun receiptDetailRoute(receiptId: Long) = "receipt_detail/$receiptId"
}

@Composable
fun SabiKudiNavGraph(
    viewModel: MainViewModel,
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier
) {
    val userPreferences by viewModel.userPreferences.collectAsState()
    val newReceiptViewModel: NewReceiptViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = Destinations.SPLASH,
        modifier = modifier
    ) {
        composable(Destinations.SPLASH) {
            SplashScreen(
                onSplashFinished = {
                    navController.popBackStack()
                    navController.navigate(Destinations.AUTH) {
                        popUpTo(Destinations.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        composable(Destinations.AUTH) {
            AuthScreen(
                onAuthSuccess = {
                    if (userPreferences.mode == null) {
                        navController.navigate(Destinations.MODE_SELECTION)
                    } else {
                        navController.navigate(Destinations.MAIN_SHELL) {
                            popUpTo(Destinations.AUTH) { inclusive = true }
                        }
                    }
                },
                onJoinWorkspaceRequested = {
                    navController.navigate(Destinations.STAFF_ONBOARDING)
                }
            )
        }

        composable(Destinations.MODE_SELECTION) {
            ModeSelectionScreen(
                onModeSelected = { mode ->
                    viewModel.selectMode(mode)
                    navController.navigate(Destinations.roleSelectionRoute(mode))
                }
            )
        }

        composable(Destinations.ROLE_SELECTION) { backStackEntry ->
            val modeStr = backStackEntry.arguments?.getString("mode") ?: AppMode.SHARED.name
            val mode = runCatching { AppMode.valueOf(modeStr) }.getOrDefault(AppMode.SHARED)

            RoleSelectionScreen(
                mode = mode,
                onRoleSelected = { role ->
                    viewModel.selectRole(mode, role)
                    navController.navigate(Destinations.MAIN_SHELL) {
                        popUpTo(Destinations.MODE_SELECTION) { inclusive = true }
                    }
                },
                onChangeModeRequested = {
                    navController.popBackStack()
                }
            )
        }

        composable(Destinations.STAFF_ONBOARDING) {
            StaffOnboardingScreen(
                onOnboardingComplete = {
                    navController.navigate(Destinations.MAIN_SHELL) {
                        popUpTo(Destinations.STAFF_ONBOARDING) { inclusive = true }
                    }
                },
                onBackRequested = { navController.popBackStack() }
            )
        }

        composable(Destinations.STAFF_DEVICES) {
            StaffDevicesScreen(
                onBackRequested = { navController.popBackStack() }
            )
        }

        composable(Destinations.MAIN_SHELL) {
            MainShellScreen(
                userPreferences = userPreferences,
                onNavigateToNewReceipt = {
                    navController.navigate(Destinations.MANUAL_RECEIPT)
                },
                onNavigateToReceiptDetail = { receiptId ->
                    navController.navigate(Destinations.receiptDetailRoute(receiptId))
                },
                onNavigateToPeopleGroups = {
                    navController.navigate(Destinations.PEOPLE_GROUPS)
                },
                onNavigateToToolsHub = {
                    navController.navigate(Destinations.TOOLS_HUB)
                },
                onNavigateToStaffDevices = {
                    navController.navigate(Destinations.STAFF_DEVICES)
                },
                onChangeModeOrRoleRequested = {
                    viewModel.resetModeAndRole()
                    navController.navigate(Destinations.MODE_SELECTION) {
                        popUpTo(Destinations.MAIN_SHELL) { inclusive = true }
                    }
                }
            )
        }

        composable(Destinations.MANUAL_RECEIPT) {
            NewReceiptScreen(
                viewModel = newReceiptViewModel,
                onNavigateToCameraScan = { navController.navigate(Destinations.CAMERA_SCAN) },
                onNavigateToReview = { navController.navigate(Destinations.RECEIPT_REVIEW) },
                onBackRequested = { navController.popBackStack() }
            )
        }

        composable(Destinations.CAMERA_SCAN) {
            CameraScanScreen(
                onCaptureComplete = { navController.navigate(Destinations.RECEIPT_REVIEW) },
                onManualEntryRequested = { navController.popBackStack() }
            )
        }

        composable(Destinations.RECEIPT_REVIEW) {
            val mode = userPreferences.mode ?: AppMode.SHARED
            val role = userPreferences.currentRoleForMode()
            ReceiptReviewScreen(
                viewModel = newReceiptViewModel,
                isBusinessMode = mode == AppMode.BUSINESS,
                isStaffRole = role == com.crypticsamsara.sabikudi.data.preferences.AppRole.STAFF,
                onNavigateToStockConfig = { itemIndex ->
                    navController.navigate(Destinations.addStockRoute(itemIndex))
                },
                onNavigateToGroupSplit = {
                    navController.navigate(Destinations.CHOOSE_GROUP)
                },
                onReceiptSaved = { _ ->
                    navController.popBackStack(Destinations.MAIN_SHELL, false)
                }
            )
        }

        composable(
            route = Destinations.ADD_STOCK,
            arguments = listOf(navArgument("itemIndex") { type = NavType.IntType })
        ) { backStackEntry ->
            val itemIndex = backStackEntry.arguments?.getInt("itemIndex") ?: 0
            AddStockCategoryScreen(
                viewModel = newReceiptViewModel,
                itemIndex = itemIndex,
                onStockSaved = { navController.popBackStack() }
            )
        }

        composable(
            route = Destinations.STOCK_DETAIL,
            arguments = listOf(navArgument("itemId") { type = NavType.LongType })
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getLong("itemId") ?: 1L
            val role = userPreferences.currentRoleForMode()
            StockDetailScreen(
                itemId = itemId,
                role = role,
                onBackRequested = { navController.popBackStack() }
            )
        }

        composable(Destinations.CHOOSE_GROUP) {
            ChooseGroupScreen(
                onGroupSelected = { _ ->
                    navController.navigate(Destinations.SPLIT_ASSIGNMENT)
                },
                onNavigateToGroupsManager = {
                    navController.navigate(Destinations.PEOPLE_GROUPS)
                }
            )
        }

        composable(Destinations.SPLIT_ASSIGNMENT) {
            SplitAssignmentScreen(
                viewModel = newReceiptViewModel,
                initialMembers = listOf("Ada", "Chidi", "Ngozi"),
                onNavigateToSplitReview = {
                    navController.navigate(Destinations.SPLIT_REVIEW)
                }
            )
        }

        composable(Destinations.SPLIT_REVIEW) {
            SplitReviewScreen(
                viewModel = newReceiptViewModel,
                onSplitConfirmed = {
                    navController.popBackStack(Destinations.RECEIPT_REVIEW, false)
                }
            )
        }

        composable(
            route = Destinations.RECEIPT_DETAIL,
            arguments = listOf(navArgument("receiptId") { type = NavType.LongType })
        ) { backStackEntry ->
            val receiptId = backStackEntry.arguments?.getLong("receiptId") ?: 1L
            ReceiptDetailScreen(
                receiptId = receiptId,
                onBackRequested = { navController.popBackStack() }
            )
        }

        composable(Destinations.PEOPLE_GROUPS) {
            PeopleGroupsScreen(
                onBackRequested = { navController.popBackStack() }
            )
        }

        composable(Destinations.TOOLS_HUB) {
            val mode = userPreferences.mode ?: AppMode.SHARED
            ToolsHubScreen(
                mode = mode,
                onNavigateTo = { route -> navController.navigate(route) },
                onBackRequested = { navController.popBackStack() }
            )
        }

        composable(Destinations.SETTLEMENT_HISTORY) {
            SettlementHistoryScreen(onBackRequested = { navController.popBackStack() })
        }

        composable(Destinations.RECURRING_EXPENSES) {
            RecurringExpensesScreen(onBackRequested = { navController.popBackStack() })
        }

        composable(Destinations.RESTOCK_EXPIRY) {
            RestockExpiryScreen(onBackRequested = { navController.popBackStack() })
        }

        composable(Destinations.SPENDING) {
            SpendingScreen(onBackRequested = { navController.popBackStack() })
        }

        composable(Destinations.AUDIT_TRAIL) {
            AuditTrailScreen(onBackRequested = { navController.popBackStack() })
        }

        composable(Destinations.STATEMENT) {
            StatementScreen(onBackRequested = { navController.popBackStack() })
        }

        composable(Destinations.BACKUP_RESTORE) {
            BackupRestoreScreen(onBackRequested = { navController.popBackStack() })
        }

        composable(Destinations.APP_LOCK) {
            AppLockScreen(onUnlocked = { navController.popBackStack() })
        }
    }
}
