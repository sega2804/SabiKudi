package com.crypticsamsara.sabikudi.ui.screens.tools

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.crypticsamsara.sabikudi.data.preferences.AppMode
import com.crypticsamsara.sabikudi.ui.theme.NavyPrimary
import com.crypticsamsara.sabikudi.ui.theme.SabiKudiTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolsHubScreen(
    mode: AppMode,
    onNavigateTo: (String) -> Unit,
    onBackRequested: () -> Unit,
    modifier: Modifier = Modifier
) {
    val extendedColors = SabiKudiTheme.extendedColors
    val scrollState = rememberScrollState()
    val isBusiness = mode == AppMode.BUSINESS

    var appLockEnabled by remember { mutableStateOf(false) }

    val toolsList = mutableListOf<Pair<String, String>>()
    if (!isBusiness) {
        toolsList.add(Pair("settlement_history", "Settlement history"))
    }
    toolsList.add(Pair("recurring_expenses", "Recurring expenses"))
    if (isBusiness) {
        toolsList.add(Pair("restock_expiry", "Restock & expiry"))
    }
    toolsList.addAll(
        listOf(
            Pair("spending", if (isBusiness) "Spending by category" else "Spending by payer"),
            Pair("audit_trail", "Audit trail"),
            Pair("statement", "Statement (PDF)"),
            Pair("backup_restore", "Backup & restore")
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Tools & data",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = NavyPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackRequested) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            toolsList.forEach { (route, label) ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, extendedColors.line, RoundedCornerShape(12.dp))
                        .clickable { onNavigateTo(route) }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = label, fontWeight = FontWeight.Bold, color = extendedColors.navy)
                        Text(text = "›", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = extendedColors.muted)
                    }
                }
            }

            // App Lock row
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, extendedColors.line, RoundedCornerShape(12.dp))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "App lock", fontWeight = FontWeight.Bold, color = extendedColors.navy)
                        Text(text = "Demo PIN: 1234", fontSize = 12.sp, color = extendedColors.muted)
                    }
                    Switch(
                        checked = appLockEnabled,
                        onCheckedChange = { appLockEnabled = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = androidx.compose.ui.graphics.Color.White, checkedTrackColor = extendedColors.teal)
                    )
                }
            }

            if (appLockEnabled) {
                OutlinedButton(
                    onClick = { onNavigateTo("app_lock") },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text(text = "Lock now", fontWeight = FontWeight.Bold, color = extendedColors.navy)
                }
            }
        }
    }
}
