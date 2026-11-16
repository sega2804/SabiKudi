package com.crypticsamsara.sabikudi.ui.screens.shared

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.crypticsamsara.sabikudi.data.preferences.AppRole
import com.crypticsamsara.sabikudi.ui.theme.SabiKudiTheme

data class BalanceItem(
    val debtor: String,
    val creditor: String,
    val amountKobo: Long,
    val itemCount: Int,
    val isStale: Boolean = false,
    val isPaymentReported: Boolean = false
)

data class SettledItem(
    val id: Long,
    val text: String,
    val amountKobo: Long
)

@Composable
fun SharedHomeTab(
    role: AppRole,
    onNavigateToReceiptDetail: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val extendedColors = SabiKudiTheme.extendedColors
    val scrollState = rememberScrollState()

    var balances by remember {
        mutableStateOf(
            listOf(
                BalanceItem("Ada", "Chidi", 200000, 2),
                BalanceItem("Ngozi", "Chidi", 125000, 1, isStale = true),
                BalanceItem("Tunde", "Ada", 310000, 2)
            )
        )
    }

    var settledItems by remember {
        mutableStateOf(
            mutableListOf(
                SettledItem(1L, "Ada settled with Chidi", 150000L)
            )
        )
    }

    var selectedBalanceForSettle by remember { mutableStateOf<BalanceItem?>(null) }
    var selectedBalanceForNudge by remember { mutableStateOf<BalanceItem?>(null) }
    var toastMessage by remember { mutableStateOf<String?>(null) }

    // Collapsible states
    var isSpendingOpen by remember { mutableStateOf(false) }
    var isSettledOpen by remember { mutableStateOf(false) }
    var isRecurringOpen by remember { mutableStateOf(false) }

    val totalOutstanding = balances.sumOf { it.amountKobo }
    val isMember = role == AppRole.MEMBER
    val myName = "Ada"

    val myOwes = balances.filter { it.debtor == myName }.sumOf { it.amountKobo }
    val myOwed = balances.filter { it.creditor == myName }.sumOf { it.amountKobo }
    val pendingReportCount = balances.count { it.isPaymentReported }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Payment Reports Banner (Organiser Only)
        if (!isMember && pendingReportCount > 0) {
            Surface(
                color = extendedColors.warning,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().border(1.dp, extendedColors.gold, RoundedCornerShape(12.dp))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🔔 $pendingReportCount payment report${if (pendingReportCount > 1) "s" else ""} to confirm",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = extendedColors.gold
                    )
                    Text(text = "Review ›", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = extendedColors.gold)
                }
            }
        }

        if (isMember) {
            // Member View Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).border(1.dp, extendedColors.line, RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(text = "You owe", fontSize = 13.sp, color = extendedColors.muted)
                        Text(
                            text = "₦${String.format("%,.2f", myOwes / 100.0)}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = extendedColors.terracotta
                        )
                    }
                }
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).border(1.dp, extendedColors.line, RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(text = "You are owed", fontSize = 13.sp, color = extendedColors.muted)
                        Text(
                            text = "₦${String.format("%,.2f", myOwed / 100.0)}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = extendedColors.teal
                        )
                    }
                }
            }
        } else {
            // Organiser Total Outstanding Card
            Card(
                colors = CardDefaults.cardColors(containerColor = extendedColors.terracottaBg),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().border(1.dp, extendedColors.terracotta, RoundedCornerShape(12.dp))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(text = "Total outstanding", fontSize = 13.sp, color = extendedColors.terracotta)
                    Text(
                        text = "₦${String.format("%,.2f", totalOutstanding / 100.0)}",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = extendedColors.terracotta
                    )
                }
            }
        }

        Text(
            text = if (isMember) "Your balances" else "Who owes whom",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = extendedColors.muted
        )

        // Balances List
        val displayBalances = if (isMember) {
            balances.filter { it.debtor == myName || it.creditor == myName }
        } else {
            balances
        }

        if (displayBalances.isEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().border(1.dp, extendedColors.line, RoundedCornerShape(12.dp))
            ) {
                Box(modifier = Modifier.padding(24.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(text = "All settled. 🎉 Nothing owed.", fontSize = 14.sp, color = extendedColors.muted, fontWeight = FontWeight.Medium)
                }
            }
        } else {
            displayBalances.forEach { balance ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().border(1.dp, extendedColors.line, RoundedCornerShape(12.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(extendedColors.surface2, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = balance.debtor.take(1),
                                        fontWeight = FontWeight.Bold,
                                        color = extendedColors.navy
                                    )
                                }
                                Column {
                                    Text(
                                        text = "${balance.debtor} owes ${balance.creditor}",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = extendedColors.navy
                                    )
                                    Text(
                                        text = "${balance.itemCount} item${if (balance.itemCount > 1) "s" else ""}",
                                        fontSize = 12.sp,
                                        color = extendedColors.muted
                                    )
                                }
                            }

                            Text(
                                text = "₦${String.format("%,.2f", balance.amountKobo / 100.0)}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = extendedColors.terracotta
                            )
                        }

                        if (balance.isStale) {
                            Surface(
                                color = extendedColors.warning,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "⚠ A share is stale. Review splits before settling.",
                                    fontSize = 12.sp,
                                    color = extendedColors.gold,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }

                        if (balance.isPaymentReported) {
                            Surface(
                                color = extendedColors.warning,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Payment reported by ${balance.debtor}",
                                    fontSize = 12.sp,
                                    color = extendedColors.gold,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }

                        // Actions
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            if (!isMember) {
                                OutlinedButton(
                                    onClick = { selectedBalanceForSettle = balance },
                                    enabled = !balance.isStale,
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(text = if (balance.isPaymentReported) "Confirm & settle" else "Settle", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = extendedColors.navy)
                                }
                                OutlinedButton(
                                    onClick = { selectedBalanceForNudge = balance },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(text = "Nudge", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = extendedColors.navy)
                                }
                            } else if (balance.debtor == myName && !balance.isPaymentReported) {
                                OutlinedButton(
                                    onClick = {
                                        balances = balances.map { if (it == balance) it.copy(isPaymentReported = true) else it }
                                        toastMessage = "Sent to ${balance.creditor} to confirm"
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(text = "I've paid", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = extendedColors.navy)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Recent Receipts Header & Card
        Text(
            text = "Recent receipts",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = extendedColors.muted,
            modifier = Modifier.padding(top = 4.dp)
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, extendedColors.line, RoundedCornerShape(12.dp))
                .clickable { onNavigateToReceiptDetail(1L) }
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Shoprite Lekki", fontWeight = FontWeight.Bold, color = extendedColors.navy)
                    Text(text = "Nov 12 · 2 items", fontSize = 12.sp, color = extendedColors.muted)
                }
                Text(text = "₦20,900.00", fontWeight = FontWeight.ExtraBold, color = extendedColors.navy)
            }
        }

        // Collapsible 1: Spending by Payer
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth().border(1.dp, extendedColors.line, RoundedCornerShape(10.dp))
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { isSpendingOpen = !isSpendingOpen }.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Spending by payer", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = extendedColors.navy)
                    Icon(imageVector = if (isSpendingOpen) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, contentDescription = null, tint = extendedColors.muted)
                }
                AnimatedVisibility(visible = isSpendingOpen) {
                    Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        val payers = listOf(Triple("Chidi", 2090000L, 0.5f), Triple("Ada", 1240000L, 0.3f), Triple("Ngozi", 775000L, 0.2f))
                        payers.forEach { (name, amt, pct) ->
                            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(text = name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = extendedColors.navy)
                                    Text(text = "₦${String.format("%,.2f", amt / 100.0)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = extendedColors.navy)
                                }
                                LinearProgressIndicator(progress = { pct }, modifier = Modifier.fillMaxWidth().height(6.dp), color = extendedColors.teal, trackColor = extendedColors.surface2)
                            }
                        }
                    }
                }
            }
        }

        // Collapsible 2: Recently Settled
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth().border(1.dp, extendedColors.line, RoundedCornerShape(10.dp))
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { isSettledOpen = !isSettledOpen }.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Recently settled", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = extendedColors.navy)
                    Icon(imageVector = if (isSettledOpen) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, contentDescription = null, tint = extendedColors.muted)
                }
                AnimatedVisibility(visible = isSettledOpen) {
                    Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (settledItems.isEmpty()) {
                            Text(text = "No recent settlements.", fontSize = 13.sp, color = extendedColors.muted)
                        } else {
                            settledItems.forEach { settled ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(text = settled.text, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = extendedColors.navy)
                                        Text(text = "₦${String.format("%,.2f", settled.amountKobo / 100.0)}", fontSize = 12.sp, color = extendedColors.muted)
                                    }
                                    TextButton(
                                        onClick = {
                                            settledItems.remove(settled)
                                            toastMessage = "Settlement undone"
                                        },
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text(text = "Undo", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = extendedColors.terracotta)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Collapsible 3: Recurring Expenses
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth().border(1.dp, extendedColors.line, RoundedCornerShape(10.dp))
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { isRecurringOpen = !isRecurringOpen }.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Recurring expenses", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = extendedColors.navy)
                    Icon(imageVector = if (isRecurringOpen) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, contentDescription = null, tint = extendedColors.muted)
                }
                AnimatedVisibility(visible = isRecurringOpen) {
                    Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        val recurrings = listOf(Pair("Rent share", 15000000L), Pair("Internet", 1500000L))
                        recurrings.forEach { (name, amt) ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = extendedColors.navy)
                                    Text(text = "₦${String.format("%,.2f", amt / 100.0)}", fontSize = 12.sp, color = extendedColors.muted)
                                }
                                OutlinedButton(
                                    onClick = { toastMessage = "Posted $name for this month" },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(text = "Post this month", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = extendedColors.navy)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Statement PDF Export Button
        Button(
            onClick = { toastMessage = "Statement (PDF) exported successfully" },
            colors = ButtonDefaults.buttonColors(containerColor = extendedColors.navy),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Text(text = "Statement (PDF)", fontWeight = FontWeight.Bold)
        }
    }

    // Settle Dialog Sheet
    selectedBalanceForSettle?.let { balance ->
        Box(
            modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)),
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(text = "Settle up?", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = extendedColors.navy)
                    Text(
                        text = "${balance.debtor} pays ${balance.creditor}\n${balance.itemCount} items · ₦${String.format("%,.2f", balance.amountKobo / 100.0)}",
                        fontSize = 14.sp,
                        color = extendedColors.muted
                    )
                    Button(
                        onClick = {
                            balances = balances.filter { it != balance }
                            settledItems.add(SettledItem(System.currentTimeMillis(), "${balance.debtor} settled with ${balance.creditor}", balance.amountKobo))
                            toastMessage = "Settled amount with ${balance.debtor}"
                            selectedBalanceForSettle = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = extendedColors.navy),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text(text = "Confirm settle", fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = { selectedBalanceForSettle = null },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text(text = "Cancel", fontWeight = FontWeight.Bold, color = extendedColors.navy)
                    }
                }
            }
        }
    }

    // Nudge Sheet
    selectedBalanceForNudge?.let { balance ->
        val msg = "Hi ${balance.debtor}, a friendly reminder: you owe ${balance.creditor} ₦${String.format("%,.2f", balance.amountKobo / 100.0)} on Sabikudi. Thanks!"
        Box(
            modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)),
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(text = "Send a reminder", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = extendedColors.navy)
                    Card(
                        colors = CardDefaults.cardColors(containerColor = extendedColors.surface2),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(text = msg, fontSize = 13.sp, color = extendedColors.navy, modifier = Modifier.padding(12.dp))
                    }
                    Button(
                        onClick = {
                            toastMessage = "Reminder copied to clipboard"
                            selectedBalanceForNudge = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = extendedColors.navy),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text(text = "Copy message", fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = { selectedBalanceForNudge = null },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text(text = "Close", fontWeight = FontWeight.Bold, color = extendedColors.navy)
                    }
                }
            }
        }
    }

    // Toast message handler
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
