package com.crypticsamsara.sabikudi.ui.screens.tools

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.crypticsamsara.sabikudi.ui.theme.NavyPrimary
import com.crypticsamsara.sabikudi.ui.theme.SabiKudiTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenericToolScaffold(
    title: String,
    onBackRequested: () -> Unit,
    content: @Composable (ColumnScope, (String) -> Unit) -> Unit
) {
    val extendedColors = SabiKudiTheme.extendedColors
    val scrollState = rememberScrollState()
    var toastMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = NavyPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBackRequested) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                content(this) { msg -> toastMessage = msg }
            }

            toastMessage?.let { msg ->
                LaunchedEffect(msg) {
                    kotlinx.coroutines.delay(3000)
                    toastMessage = null
                }
                Box(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Surface(
                        color = extendedColors.navy,
                        shape = RoundedCornerShape(12.dp),
                        shadowElevation = 8.dp
                    ) {
                        Text(
                            text = "✓ $msg",
                            color = Color.White,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SettlementHistoryScreen(onBackRequested: () -> Unit) {
    val extendedColors = SabiKudiTheme.extendedColors
    var settlements by remember {
        mutableStateOf(
            mutableListOf(
                Triple("Ada → Chidi", 200000L, "Settled"),
                Triple("Tunde → Ada", 150000L, "Part payment")
            )
        )
    }

    GenericToolScaffold(title = "Settlement history", onBackRequested = onBackRequested) { _, showToast ->
        if (settlements.isEmpty()) {
            Text(text = "No settlements yet.", fontSize = 14.sp, color = extendedColors.muted)
        } else {
            settlements.forEachIndexed { index, (pair, amt, status) ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().border(1.dp, extendedColors.line, RoundedCornerShape(12.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = pair, fontWeight = FontWeight.Bold, color = extendedColors.navy, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                            Text(text = status, fontSize = 12.sp, color = extendedColors.muted, maxLines = 1)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "₦${String.format("%,.2f", amt / 100.0)}", fontWeight = FontWeight.ExtraBold, color = extendedColors.navy, maxLines = 1)
                            TextButton(
                                onClick = {
                                    settlements.removeAt(index)
                                    showToast("Undone settlement")
                                },
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text(text = "Undo", fontSize = 12.sp, color = extendedColors.terracotta)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RecurringExpensesScreen(onBackRequested: () -> Unit) {
    val extendedColors = SabiKudiTheme.extendedColors
    var expenses by remember {
        mutableStateOf(
            mutableListOf(
                Pair("Rent share", 15000000L),
                Pair("Internet", 1500000L)
            )
        )
    }

    GenericToolScaffold(title = "Recurring expenses", onBackRequested = onBackRequested) { _, showToast ->
        expenses.forEach { (name, amt) ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().border(1.dp, extendedColors.line, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = name, fontWeight = FontWeight.Bold, color = extendedColors.navy, modifier = Modifier.weight(1f), maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "₦${String.format("%,.2f", amt / 100.0)}", fontWeight = FontWeight.ExtraBold, color = extendedColors.navy, maxLines = 1)
                    }
                    OutlinedButton(
                        onClick = { showToast("Successfully posted $name for this month") },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(text = "Post this month", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = extendedColors.navy)
                    }
                }
            }
        }
    }
}

@Composable
fun RestockExpiryScreen(onBackRequested: () -> Unit) {
    val extendedColors = SabiKudiTheme.extendedColors
    GenericToolScaffold(title = "Restock & expiry", onBackRequested = onBackRequested) { _, showToast ->
        Text(text = "Restock list", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = extendedColors.muted)
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().border(1.dp, extendedColors.line, RoundedCornerShape(12.dp))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Indomie carton", fontWeight = FontWeight.Bold, color = extendedColors.navy, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                    Text(text = "3 left · threshold 5", fontSize = 12.sp, color = extendedColors.muted, maxLines = 1)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { showToast("Indomie carton marked as restocked") },
                    colors = ButtonDefaults.buttonColors(containerColor = extendedColors.teal),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(text = "Restocked", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun SpendingScreen(onBackRequested: () -> Unit) {
    val extendedColors = SabiKudiTheme.extendedColors
    GenericToolScaffold(title = "Spending breakdown", onBackRequested = onBackRequested) { _, _ ->
        val categories = listOf(Pair("Groceries", 2850000L), Pair("Toiletries", 960000L), Pair("Beverages", 1450000L))
        val maxAmt = categories.maxOf { it.second }.toFloat()

        categories.forEach { (cat, amt) ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().border(1.dp, extendedColors.line, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = cat, fontWeight = FontWeight.Bold, color = extendedColors.navy, modifier = Modifier.weight(1f), maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "₦${String.format("%,.2f", amt / 100.0)}", fontWeight = FontWeight.ExtraBold, color = extendedColors.navy, maxLines = 1)
                    }
                    LinearProgressIndicator(
                        progress = { (amt / maxAmt).toFloat() },
                        modifier = Modifier.fillMaxWidth().height(8.dp),
                        color = extendedColors.teal,
                        trackColor = extendedColors.surface2
                    )
                }
            }
        }
    }
}

@Composable
fun AuditTrailScreen(onBackRequested: () -> Unit) {
    val extendedColors = SabiKudiTheme.extendedColors
    GenericToolScaffold(title = "Audit trail", onBackRequested = onBackRequested) { _, _ ->
        val logs = listOf("Nov 13 · Added receipt \"Shoprite Ikeja\"", "Nov 12 · Receipt \"Shoprite Lekki\" added to balances")
        logs.forEach { log ->
            Card(
                colors = CardDefaults.cardColors(containerColor = extendedColors.surface2),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = log, fontSize = 13.sp, color = extendedColors.muted, modifier = Modifier.padding(14.dp))
            }
        }
    }
}

@Composable
fun StatementScreen(onBackRequested: () -> Unit) {
    val extendedColors = SabiKudiTheme.extendedColors
    GenericToolScaffold(title = "Statement (PDF)", onBackRequested = onBackRequested) { _, showToast ->
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().border(1.dp, extendedColors.line, RoundedCornerShape(12.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = "Balance statement", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = extendedColors.navy)
                Text(text = "Shared expenses · as of Nov 13, 2026", fontSize = 12.sp, color = extendedColors.muted)
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "Ada → Chidi", color = extendedColors.navy)
                    Text(text = "₦2,000.00", fontWeight = FontWeight.Bold, color = extendedColors.navy)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "Total", fontWeight = FontWeight.Bold, color = extendedColors.navy)
                    Text(text = "₦6,350.00", fontWeight = FontWeight.ExtraBold, color = extendedColors.navy)
                }
            }
        }
        Button(
            onClick = { showToast("Statement PDF exported successfully") },
            colors = ButtonDefaults.buttonColors(containerColor = extendedColors.navy),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Text(text = "Export PDF", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun BackupRestoreScreen(onBackRequested: () -> Unit) {
    val extendedColors = SabiKudiTheme.extendedColors
    GenericToolScaffold(title = "Backup & restore", onBackRequested = onBackRequested) { _, showToast ->
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().border(1.dp, extendedColors.line, RoundedCornerShape(12.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = "Last backup", fontWeight = FontWeight.Bold, color = extendedColors.navy)
                Text(text = "Never. Your data lives only on this phone.", fontSize = 12.sp, color = extendedColors.muted)
            }
        }
        Button(
            onClick = { showToast("Backup data exported successfully") },
            colors = ButtonDefaults.buttonColors(containerColor = extendedColors.navy),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Text(text = "Export backup", fontWeight = FontWeight.Bold)
        }
        OutlinedButton(
            onClick = { showToast("Data restored from file successfully") },
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Text(text = "Restore from file", fontWeight = FontWeight.Bold, color = extendedColors.navy)
        }
    }
}
