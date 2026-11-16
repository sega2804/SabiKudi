package com.crypticsamsara.sabikudi.ui.screens.main

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.crypticsamsara.sabikudi.data.preferences.AppMode
import com.crypticsamsara.sabikudi.data.preferences.AppRole
import com.crypticsamsara.sabikudi.data.preferences.UserPreferences
import com.crypticsamsara.sabikudi.ui.screens.business.BusinessDashboardTab
import com.crypticsamsara.sabikudi.ui.screens.history.HistoryScreen
import com.crypticsamsara.sabikudi.ui.screens.shared.SharedHomeTab
import com.crypticsamsara.sabikudi.ui.theme.SabiKudiTheme

sealed class MainTab(
    val key: String,
    val title: String,
    val icon: ImageVector
) {
    object Home : MainTab("home", "Home", Icons.Default.Home)
    object Dashboard : MainTab("dashboard", "Dashboard", Icons.Default.GridView)
    object History : MainTab("history", "History", Icons.AutoMirrored.Filled.List)
    object Manage : MainTab("manage", "Manage", Icons.Default.People)
    object Settings : MainTab("settings", "Settings", Icons.Default.Settings)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainShellScreen(
    userPreferences: UserPreferences,
    onNavigateToNewReceipt: () -> Unit,
    onNavigateToReceiptDetail: (Long) -> Unit,
    onNavigateToPeopleGroups: () -> Unit,
    onNavigateToToolsHub: () -> Unit,
    onNavigateToStaffDevices: () -> Unit = {},
    onChangeModeOrRoleRequested: () -> Unit,
    modifier: Modifier = Modifier
) {
    val extendedColors = SabiKudiTheme.extendedColors
    val mode = userPreferences.mode ?: AppMode.SHARED
    val role = userPreferences.currentRoleForMode()

    var showInboxSheet by remember { mutableStateOf(false) }
    var showWorkspaceSheet by remember { mutableStateOf(false) }

    val tabs = remember(mode, role) {
        if (mode == AppMode.BUSINESS) {
            listOf(MainTab.Dashboard, MainTab.History, MainTab.Settings)
        } else {
            val list = mutableListOf(MainTab.Home, MainTab.History)
            if (role == AppRole.ORGANISER) {
                list.add(MainTab.Manage)
            }
            list.add(MainTab.Settings)
            list
        }
    }

    var selectedTabKey by remember(mode) {
        mutableStateOf(if (mode == AppMode.BUSINESS) MainTab.Dashboard.key else MainTab.Home.key)
    }

    val currentTab = tabs.find { it.key == selectedTabKey } ?: tabs.first()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth().padding(end = 8.dp)
                    ) {
                        Text(
                            text = currentTab.title,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = extendedColors.navy
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (mode == AppMode.BUSINESS) {
                                IconButton(onClick = { showInboxSheet = true }) {
                                    Icon(
                                        imageVector = Icons.Default.Notifications,
                                        contentDescription = "Inbox Notifications",
                                        tint = extendedColors.navy
                                    )
                                }
                            }

                            val modeLabel = if (mode == AppMode.BUSINESS) {
                                "Business · ${role.name.lowercase().replaceFirstChar { it.uppercase() }}"
                            } else {
                                "Shared · ${role.name.lowercase().replaceFirstChar { it.uppercase() }}"
                            }

                            val tagColor = if (mode == AppMode.BUSINESS) extendedColors.teal else extendedColors.terracotta
                            val tagBg = if (mode == AppMode.BUSINESS) extendedColors.tealBg else extendedColors.terracottaBg

                            Box(
                                modifier = Modifier
                                    .border(1.dp, tagColor, RoundedCornerShape(99.dp))
                                    .background(tagBg, RoundedCornerShape(99.dp))
                                    .clickable { showWorkspaceSheet = true }
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = modeLabel,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = tagColor
                                )
                            }
                        }
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp,
                modifier = Modifier.border(1.dp, extendedColors.line)
            ) {
                tabs.forEach { tab ->
                    val selected = tab.key == currentTab.key
                    NavigationBarItem(
                        selected = selected,
                        onClick = { selectedTabKey = tab.key },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontSize = 11.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = extendedColors.navy,
                            selectedTextColor = extendedColors.navy,
                            indicatorColor = extendedColors.surface2,
                            unselectedIconColor = extendedColors.muted,
                            unselectedTextColor = extendedColors.muted
                        )
                    )
                }
            }
        },
        floatingActionButton = {
            val canShowFab = if (mode == AppMode.SHARED && role == AppRole.MEMBER) {
                false
            } else {
                currentTab == MainTab.Home || currentTab == MainTab.Dashboard || currentTab == MainTab.History
            }

            if (canShowFab) {
                FloatingActionButton(
                    onClick = onNavigateToNewReceipt,
                    containerColor = extendedColors.amber,
                    contentColor = Color(0xFF1A1405),
                    shape = CircleShape,
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "New Receipt",
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                MainTab.Home -> SharedHomeTab(role = role, onNavigateToReceiptDetail = onNavigateToReceiptDetail)
                MainTab.Dashboard -> BusinessDashboardTab(role = role, onNavigateToReceiptDetail = onNavigateToReceiptDetail)
                MainTab.History -> HistoryScreen(role = role, onNavigateToReceiptDetail = onNavigateToReceiptDetail)
                MainTab.Manage -> ManageTabContent(onNavigateToPeopleGroups = onNavigateToPeopleGroups)
                MainTab.Settings -> SettingsTabContent(
                    userPreferences = userPreferences,
                    onNavigateToToolsHub = onNavigateToToolsHub,
                    onNavigateToStaffDevices = onNavigateToStaffDevices,
                    onChangeModeOrRoleRequested = onChangeModeOrRoleRequested
                )
            }
        }
    }

    // Inbox Bottom Sheet (Business Mode)
    if (showInboxSheet) {
        ModalBottomSheet(onDismissRequest = { showInboxSheet = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(text = "🔔 Business Inbox", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = extendedColors.navy)
                if (role == AppRole.OWNER) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = extendedColors.surface2),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().clickable {
                            showInboxSheet = false
                            onNavigateToReceiptDetail(6L)
                        }
                    ) {
                        Row(modifier = Modifier.padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text(text = "Receipts waiting approval (1)", fontWeight = FontWeight.Bold, color = extendedColors.navy)
                                Text(text = "From Tunde · Oyingbo Wholesale (₦10,300.00)", fontSize = 12.sp, color = extendedColors.muted)
                            }
                            Text(text = "Review ›", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = extendedColors.teal)
                        }
                    }
                } else {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = extendedColors.surface2),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(text = "My Receipts Status", fontWeight = FontWeight.Bold, color = extendedColors.navy)
                            Text(text = "Oyingbo Wholesale · Sent for approval", fontSize = 12.sp, color = extendedColors.muted)
                        }
                    }
                }

                Button(
                    onClick = { showInboxSheet = false },
                    colors = ButtonDefaults.buttonColors(containerColor = extendedColors.navy),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().height(44.dp)
                ) {
                    Text(text = "Close", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showWorkspaceSheet) {
        com.crypticsamsara.sabikudi.ui.components.WorkspaceSwitcherSheet(
            workspaces = emptyList(),
            currentWorkspaceId = null,
            onWorkspaceSelected = { _ -> },
            onJoinWorkspaceWithCode = { _ -> },
            onCreateWorkspaceRequested = onChangeModeOrRoleRequested,
            onDismissRequest = { showWorkspaceSheet = false }
        )
    }
}

@Composable
fun ManageTabContent(
    onNavigateToPeopleGroups: () -> Unit
) {
    val extendedColors = SabiKudiTheme.extendedColors
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, extendedColors.line, RoundedCornerShape(12.dp))
                .clickable { onNavigateToPeopleGroups() }
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "People & Groups", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = extendedColors.navy)
                    Text(text = "Manage active members and expense groups", fontSize = 12.sp, color = extendedColors.muted)
                }
                Text(text = "›", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = extendedColors.muted)
            }
        }
    }
}

@Composable
fun SettingsTabContent(
    userPreferences: UserPreferences,
    onNavigateToToolsHub: () -> Unit,
    onNavigateToStaffDevices: () -> Unit,
    onChangeModeOrRoleRequested: () -> Unit
) {
    val extendedColors = SabiKudiTheme.extendedColors
    val scrollState = rememberScrollState()
    val mode = userPreferences.mode ?: AppMode.SHARED
    val role = userPreferences.currentRoleForMode()

    var autoPurge by remember { mutableStateOf(userPreferences.autoPurgeImages) }
    var retentionDays by remember { mutableIntStateOf(userPreferences.retentionDays) }
    var showWipeDialog by remember { mutableStateOf(false) }
    var showWipeToast by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Role Permission Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().border(1.dp, extendedColors.line, RoundedCornerShape(12.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = "Role & Permissions", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = extendedColors.navy)
                Text(text = "Active: ${mode.name} · ${role.name}", fontSize = 13.sp, color = extendedColors.teal, fontWeight = FontWeight.Bold)

                HorizontalDivider(color = extendedColors.line)

                val perms = when (role) {
                    AppRole.OWNER -> listOf("✓ Add receipts", "✓ Equal & Percent splits", "✓ Costs & profit margins", "✓ Settle & Nudge", "✓ Export CSV & Statement", "✓ Manage staff & devices")
                    AppRole.STAFF -> listOf("✓ Add receipts (sent for approval)", "✕ Split items", "✕ See costs & margins", "✕ Settle balances", "✓ Camera scan")
                    AppRole.ORGANISER -> listOf("✓ Add receipts", "✓ Equal, Percent, Exact, Shares splits", "✓ Balances & settling", "✓ PDF statement", "✓ Manage people & groups")
                    AppRole.MEMBER -> listOf("✓ Add receipts", "✓ Equal split", "✓ Own balances (\"I've paid\")", "✕ Settle or Nudge", "✕ PDF statement")
                }

                perms.forEach { perm ->
                    Text(text = perm, fontSize = 12.sp, color = if (perm.startsWith("✓")) extendedColors.navy else extendedColors.muted)
                }
            }
        }

        // Workspace Section (Business Owner)
        if (mode == AppMode.BUSINESS && role == AppRole.OWNER) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, extendedColors.line, RoundedCornerShape(12.dp))
                    .clickable { onNavigateToStaffDevices() }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Staff & devices", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = extendedColors.navy)
                        Text(text = "Invite staff, set permissions & 7-day offline limits", fontSize = 12.sp, color = extendedColors.muted)
                    }
                    Text(text = "›", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = extendedColors.muted)
                }
            }
        }

        // Tools & Data Hub Row
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, extendedColors.line, RoundedCornerShape(12.dp))
                .clickable { onNavigateToToolsHub() }
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Tools & data", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = extendedColors.navy)
                    Text(text = "Settlement history, recurring, spending & backup", fontSize = 12.sp, color = extendedColors.muted)
                }
                Text(text = "›", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = extendedColors.muted)
            }
        }

        // Receipt Image Retention Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().border(1.dp, extendedColors.line, RoundedCornerShape(12.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Receipt image auto-purge", fontWeight = FontWeight.Bold, color = extendedColors.navy)
                        Text(text = "Auto delete receipt photos after retention period", fontSize = 12.sp, color = extendedColors.muted)
                    }
                    Switch(checked = autoPurge, onCheckedChange = { autoPurge = it }, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = extendedColors.teal))
                }

                if (autoPurge) {
                    Text(text = "Retention days:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = extendedColors.muted)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(30, 60, 90).forEach { days ->
                            val selected = retentionDays == days
                            Box(
                                modifier = Modifier
                                    .border(1.dp, if (selected) extendedColors.teal else extendedColors.line, RoundedCornerShape(99.dp))
                                    .background(if (selected) extendedColors.tealBg else extendedColors.surface2, RoundedCornerShape(99.dp))
                                    .clickable { retentionDays = days }
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text(text = "$days days", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (selected) extendedColors.teal else extendedColors.muted)
                            }
                        }
                    }
                }
            }
        }

        // Mode & Role Switch Button Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().border(1.dp, extendedColors.line, RoundedCornerShape(12.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(text = "Mode & Role Settings", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = extendedColors.navy)
                Text(text = "Switch between Business and Shared Expenses mode or change active role.", fontSize = 13.sp, color = extendedColors.muted)
                Button(
                    onClick = onChangeModeOrRoleRequested,
                    colors = ButtonDefaults.buttonColors(containerColor = extendedColors.navy),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().height(44.dp)
                ) {
                    Text(text = "Change Mode or Role", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Full Data Wipe Button (Outline Danger)
        OutlinedButton(
            onClick = { showWipeDialog = true },
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Text(text = "Full data wipe", fontWeight = FontWeight.Bold, color = extendedColors.alert)
        }
    }

    // Full Data Wipe Confirmation Dialog
    if (showWipeDialog) {
        AlertDialog(
            onDismissRequest = { showWipeDialog = false },
            title = { Text(text = "Full data wipe?", fontWeight = FontWeight.Bold, color = extendedColors.alert) },
            text = { Text(text = "This will erase all receipts, balances, people, groups, and stored images from this phone. This action cannot be undone.", fontSize = 13.sp, color = extendedColors.navy) },
            confirmButton = {
                Button(
                    onClick = {
                        showWipeDialog = false
                        showWipeToast = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = extendedColors.alert)
                ) {
                    Text(text = "Wipe Everything", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showWipeDialog = false }) {
                    Text(text = "Cancel")
                }
            }
        )
    }

    if (showWipeToast) {
        LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(3000)
            showWipeToast = false
        }
        Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.BottomCenter) {
            Surface(color = extendedColors.alert, shape = RoundedCornerShape(12.dp), shadowElevation = 8.dp) {
                Text(text = "✓ All app data wiped successfully", color = Color.White, fontWeight = FontWeight.Medium, modifier = Modifier.padding(14.dp))
            }
        }
    }
}
