package com.crypticsamsara.sabikudi.ui.screens.receipt

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.crypticsamsara.sabikudi.ui.theme.NavyPrimary
import com.crypticsamsara.sabikudi.ui.theme.SabiKudiTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptReviewScreen(
    viewModel: NewReceiptViewModel,
    isBusinessMode: Boolean,
    isStaffRole: Boolean,
    onNavigateToStockConfig: (Int) -> Unit,
    onNavigateToGroupSplit: () -> Unit,
    onReceiptSaved: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val extendedColors = SabiKudiTheme.extendedColors
    val totalAmount = state.items.sumOf { item -> item.priceInKobo }
    val scrollState = rememberScrollState()

    var showPaidBySheet by remember { mutableStateOf(false) }
    val people = listOf("Ada", "Chidi", "Ngozi", "Tunde")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Review items",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = NavyPrimary
                    )
                }
            )
        },
        modifier = modifier
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
                // Header Card
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
                        OutlinedTextField(
                            value = state.merchant,
                            onValueChange = { viewModel.updateMerchant(it) },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "₦${String.format("%,.2f", totalAmount / 100.0)}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = extendedColors.navy
                        )
                    }
                }

                // Paid By Picker Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, extendedColors.line, RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { showPaidBySheet = true }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Paid by", fontWeight = FontWeight.Bold, color = extendedColors.navy)
                        Text(
                            text = "${state.paidBy ?: "Choose"} ›",
                            fontWeight = FontWeight.Bold,
                            color = extendedColors.terracotta
                        )
                    }
                }

                Text(
                    text = "All fields are editable. Tap a chip to choose where each item goes.",
                    fontSize = 12.sp,
                    color = extendedColors.muted
                )

                // Items list
                state.items.forEachIndexed { index, item ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().border(1.dp, extendedColors.line, RoundedCornerShape(12.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = item.name,
                                    onValueChange = { viewModel.updateItemName(index, it) },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = (item.priceInKobo / 100.0).toString(),
                                    onValueChange = {
                                        val parsed = (it.toDoubleOrNull() ?: 0.0) * 100
                                        viewModel.updateItemPrice(index, parsed.toLong())
                                    },
                                    singleLine = true,
                                    modifier = Modifier.width(110.dp)
                                )
                            }

                            OutlinedTextField(
                                value = item.note,
                                onValueChange = { viewModel.updateItemNote(index, it) },
                                placeholder = { Text("Note (optional)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Destination Chips
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                DestinationChip(
                                    text = "Unassigned",
                                    selected = item.destination == ItemDestination.UNASSIGNED,
                                    onClick = { viewModel.setItemDestination(index, ItemDestination.UNASSIGNED) }
                                )

                                if (isBusinessMode) {
                                    DestinationChip(
                                        text = "Stock",
                                        selected = item.destination == ItemDestination.STOCK,
                                        onClick = {
                                            viewModel.setItemDestination(index, ItemDestination.STOCK)
                                            onNavigateToStockConfig(index)
                                        },
                                        accentColor = extendedColors.teal
                                    )
                                } else {
                                    DestinationChip(
                                        text = "Split",
                                        selected = item.destination == ItemDestination.SPLIT,
                                        onClick = {
                                            viewModel.setItemDestination(index, ItemDestination.SPLIT)
                                            onNavigateToGroupSplit()
                                        },
                                        accentColor = extendedColors.terracotta
                                    )
                                }
                            }
                        }
                    }
                }

                if (!state.error.isNullOrBlank()) {
                    Text(
                        text = state.error!!,
                        color = extendedColors.terracotta,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        val modeStr = if (isBusinessMode) "BUSINESS" else "SHARED"
                        val roleStr = if (isStaffRole) "staff" else "owner"
                        viewModel.saveReceipt(mode = modeStr, role = roleStr) { receiptId ->
                            onReceiptSaved(receiptId)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = extendedColors.navy),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text(text = "Save receipt", fontWeight = FontWeight.Bold)
                }
            }

            // Paid By Sheet
            if (showPaidBySheet) {
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
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(text = "Who paid?", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = extendedColors.navy)
                            people.forEach { person ->
                                OutlinedButton(
                                    onClick = {
                                        viewModel.updatePaidBy(person)
                                        showPaidBySheet = false
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth().height(48.dp)
                                ) {
                                    Text(text = person, fontWeight = FontWeight.Bold, color = extendedColors.navy)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DestinationChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    accentColor: Color = NavyPrimary
) {
    val extendedColors = SabiKudiTheme.extendedColors
    val bg = if (selected) accentColor else MaterialTheme.colorScheme.surface
    val fg = if (selected) Color.White else extendedColors.muted
    val borderCol = if (selected) accentColor else extendedColors.line

    Box(
        modifier = Modifier
            .border(1.5.dp, borderCol, RoundedCornerShape(99.dp))
            .background(bg, RoundedCornerShape(99.dp))
            .clip(RoundedCornerShape(99.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = fg
        )
    }
}
