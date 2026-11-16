package com.crypticsamsara.sabikudi.ui.screens.receipt

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
fun ReceiptDetailScreen(
    receiptId: Long,
    onBackRequested: () -> Unit,
    modifier: Modifier = Modifier
) {
    val extendedColors = SabiKudiTheme.extendedColors
    val scrollState = rememberScrollState()

    var keepPermanent by remember { mutableStateOf(false) }
    var isAddedToBalance by remember { mutableStateOf(false) }
    var isStaleSplit by remember { mutableStateOf(receiptId == 2L) } // Demo stale split state for receipt #2

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Shoprite Lekki",
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Receipt Header Info Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().border(1.dp, extendedColors.line, RoundedCornerShape(12.dp))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Nov 12, 2026", fontSize = 13.sp, color = extendedColors.muted)
                        Box(
                            modifier = Modifier
                                .border(1.dp, extendedColors.terracotta, RoundedCornerShape(99.dp))
                                .background(extendedColors.terracottaBg, RoundedCornerShape(99.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(text = "SHARED", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = extendedColors.terracotta)
                        }
                    }
                    Text(text = "Paid by Chidi", fontSize = 13.sp, color = extendedColors.muted)
                }
            }

            // Stale Split Warning Banner
            if (isStaleSplit) {
                Surface(
                    color = extendedColors.goldBg,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().border(1.dp, extendedColors.gold, RoundedCornerShape(12.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "⚠ Stale split: the price changed from ₦11,900.00 to ₦12,400.00 after this was split. Shares still use the old price until you re-split.",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = extendedColors.gold
                        )
                        Button(
                            onClick = { isStaleSplit = false },
                            colors = ButtonDefaults.buttonColors(containerColor = extendedColors.gold),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text(text = "Re-split now", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }

            // Items List
            val items = listOf(
                Triple("Rice 5kg", 850000L, "Split · Ada, Chidi"),
                Triple("Chicken", 1240000L, "Split · Flatmates")
            )

            items.forEach { (name, price, desc) ->
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
                        Column {
                            Text(text = name, fontWeight = FontWeight.Bold, color = extendedColors.navy)
                            Text(text = desc, fontSize = 12.sp, color = extendedColors.muted)
                        }
                        Text(
                            text = "₦${String.format("%,.2f", price / 100.0)}",
                            fontWeight = FontWeight.ExtraBold,
                            color = extendedColors.navy
                        )
                    }
                }
            }

            // Keep Permanent Toggle Card
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
                        Text(text = "Keep image permanently", fontWeight = FontWeight.Bold, color = extendedColors.navy)
                        Text(text = "Overrides the 30-day purge", fontSize = 12.sp, color = extendedColors.muted)
                    }
                    Switch(
                        checked = keepPermanent,
                        onCheckedChange = { keepPermanent = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = extendedColors.teal)
                    )
                }
            }

            OutlinedButton(
                onClick = { /* Edit action */ },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text(text = "Edit receipt", fontWeight = FontWeight.Bold, color = extendedColors.navy)
            }

            if (!isAddedToBalance) {
                Button(
                    onClick = { isAddedToBalance = true },
                    colors = ButtonDefaults.buttonColors(containerColor = extendedColors.navy),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text(text = "Add to balance", fontWeight = FontWeight.Bold)
                }
            } else {
                Surface(
                    color = extendedColors.tealBg,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "✓ Added to balance",
                        color = extendedColors.teal,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(14.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }
}
