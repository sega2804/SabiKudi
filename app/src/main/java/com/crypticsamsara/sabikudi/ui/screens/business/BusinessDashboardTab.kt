package com.crypticsamsara.sabikudi.ui.screens.business

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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

data class InventoryItem(
    val name: String,
    val category: String,
    val quantity: Int,
    val threshold: Int
)

data class PendingReceipt(
    val id: Long,
    val merchant: String,
    val staffName: String,
    val itemCount: Int,
    val amountKobo: Long
)

@Composable
fun BusinessDashboardTab(
    role: AppRole,
    onNavigateToReceiptDetail: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val extendedColors = SabiKudiTheme.extendedColors
    val scrollState = rememberScrollState()
    val isOwner = role == AppRole.OWNER

    var inventory by remember {
        mutableStateOf(
            mutableListOf(
                InventoryItem("Indomie carton", "Groceries", 3, 5),
                InventoryItem("Nylon bags", "Packaging", 40, 20),
                InventoryItem("Soap 500ml", "Toiletries", 2, 6),
                InventoryItem("Bottled water", "Beverages", 48, 24),
                InventoryItem("Receipt rolls", "Supplies", 4, 10),
                InventoryItem("Garri 5kg", "Groceries", 12, 5)
            )
        )
    }

    var pendingStaffReceipts by remember {
        mutableStateOf(
            mutableListOf(
                PendingReceipt(6L, "Oyingbo Wholesale", "Tunde", 2, 1030000L)
            )
        )
    }

    var selectedReceiptForReject by remember { mutableStateOf<PendingReceipt?>(null) }
    var rejectionReasonText by remember { mutableStateOf("") }
    var toastMessage by remember { mutableStateOf<String?>(null) }

    // Collapsible states
    var isRestockOpen by remember { mutableStateOf(false) }
    var isCategorySpendingOpen by remember { mutableStateOf(false) }

    val lowStockCount = inventory.count { it.quantity <= it.threshold }
    val outstandingAmount = 450000L // ₦4,500.00

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Staff Offline Warning Banner (Staff Role)
        if (!isOwner) {
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
                    Column {
                        Text(text = "⚠ Offline sync pending", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = extendedColors.gold)
                        Text(text = "Day 5 of 7 · 2 days remaining before device lock", fontSize = 12.sp, color = extendedColors.gold)
                    }
                }
            }
        }

        // Staff Waiting to Join Banner (Owner Role)
        if (isOwner) {
            Surface(
                color = extendedColors.tealBg,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().border(1.dp, extendedColors.teal, RoundedCornerShape(12.dp))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Staff waiting to join (1) ›", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = extendedColors.teal)
                    Text(text = "Review", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = extendedColors.teal)
                }
            }
        }

        // Top Cards: Low stock & Outstanding
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
                    Text(text = "Low stock", fontSize = 13.sp, color = extendedColors.muted)
                    Text(
                        text = lowStockCount.toString(),
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = extendedColors.teal
                    )
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f).border(1.dp, extendedColors.line, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(text = "Outstanding", fontSize = 13.sp, color = extendedColors.muted)
                    Text(
                        text = "₦${String.format("%,.2f", outstandingAmount / 100.0)}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = extendedColors.terracotta
                    )
                }
            }
        }

        // Pending Staff Receipts Section (Owner Only)
        if (isOwner && pendingStaffReceipts.isNotEmpty()) {
            Text(
                text = "Needs your review",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = extendedColors.muted
            )

            pendingStaffReceipts.forEach { receipt ->
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
                            Column {
                                Text(text = receipt.merchant, fontWeight = FontWeight.Bold, color = extendedColors.navy)
                                Text(text = "From ${receipt.staffName} · ${receipt.itemCount} items", fontSize = 12.sp, color = extendedColors.muted)
                            }
                            Text(text = "₦${String.format("%,.2f", receipt.amountKobo / 100.0)}", fontWeight = FontWeight.ExtraBold, color = extendedColors.navy)
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { onNavigateToReceiptDetail(receipt.id) },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(text = "Review", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = extendedColors.navy)
                            }
                            Button(
                                onClick = {
                                    pendingStaffReceipts.remove(receipt)
                                    toastMessage = "Approved receipt from ${receipt.staffName}"
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = extendedColors.teal),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(text = "Approve", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            OutlinedButton(
                                onClick = { selectedReceiptForReject = receipt },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(text = "Reject", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = extendedColors.alert)
                            }
                        }
                    }
                }
            }
        }

        // Inventory Header & CSV Export
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Inventory",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = extendedColors.muted
            )
            TextButton(onClick = { toastMessage = "Exported inventory CSV file" }) {
                Text(text = "Export CSV", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = extendedColors.teal)
            }
        }

        inventory.forEachIndexed { index, item ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, extendedColors.line, RoundedCornerShape(12.dp))
                    .clickable { onNavigateToReceiptDetail(index.toLong()) }
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = item.name, fontWeight = FontWeight.Bold, color = extendedColors.navy)
                        Text(text = item.category, fontSize = 12.sp, color = extendedColors.muted)
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (item.quantity <= item.threshold) {
                            Box(
                                modifier = Modifier
                                    .background(extendedColors.warning, RoundedCornerShape(99.dp))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(text = "Low", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = extendedColors.gold)
                            }
                        }
                        Text(
                            text = "${item.quantity} left",
                            fontWeight = FontWeight.Bold,
                            color = extendedColors.navy
                        )
                    }
                }
            }
        }

        // Collapsible 1: Restock & Expiry
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth().border(1.dp, extendedColors.line, RoundedCornerShape(10.dp))
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { isRestockOpen = !isRestockOpen }.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Restock & expiry alerts", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = extendedColors.navy)
                    Icon(imageVector = if (isRestockOpen) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, contentDescription = null, tint = extendedColors.muted)
                }
                AnimatedVisibility(visible = isRestockOpen) {
                    Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Indomie carton", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = extendedColors.navy)
                            Text(text = "3 left · threshold 5", fontSize = 12.sp, color = extendedColors.gold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Soap 500ml", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = extendedColors.navy)
                            Text(text = "Expires Nov 25", fontSize = 12.sp, color = extendedColors.alert)
                        }
                    }
                }
            }
        }

        // Collapsible 2: Spending by Category
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth().border(1.dp, extendedColors.line, RoundedCornerShape(10.dp))
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { isCategorySpendingOpen = !isCategorySpendingOpen }.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Spending by category", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = extendedColors.navy)
                    Icon(imageVector = if (isCategorySpendingOpen) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, contentDescription = null, tint = extendedColors.muted)
                }
                AnimatedVisibility(visible = isCategorySpendingOpen) {
                    Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        val categories = listOf(Triple("Groceries", 2850000L, 0.55f), Triple("Beverages", 1450000L, 0.30f), Triple("Toiletries", 960000L, 0.15f))
                        categories.forEach { (cat, amt, pct) ->
                            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(text = cat, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = extendedColors.navy)
                                    Text(text = "₦${String.format("%,.2f", amt / 100.0)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = extendedColors.navy)
                                }
                                LinearProgressIndicator(progress = { pct }, modifier = Modifier.fillMaxWidth().height(6.dp), color = extendedColors.teal, trackColor = extendedColors.surface2)
                            }
                        }
                    }
                }
            }
        }
    }

    // Rejection Dialog Sheet
    selectedReceiptForReject?.let { receipt ->
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
                    Text(text = "Reject receipt from ${receipt.staffName}?", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = extendedColors.navy)
                    OutlinedTextField(
                        value = rejectionReasonText,
                        onValueChange = { rejectionReasonText = it },
                        label = { Text("Reason for rejection (e.g. Photo blurry)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = {
                            pendingStaffReceipts.remove(receipt)
                            toastMessage = "Rejected receipt from ${receipt.staffName}"
                            selectedReceiptForReject = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = extendedColors.alert),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text(text = "Confirm rejection", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    OutlinedButton(
                        onClick = { selectedReceiptForReject = null },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text(text = "Cancel", fontWeight = FontWeight.Bold, color = extendedColors.navy)
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
