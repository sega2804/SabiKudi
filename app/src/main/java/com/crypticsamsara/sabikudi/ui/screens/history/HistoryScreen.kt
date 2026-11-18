package com.crypticsamsara.sabikudi.ui.screens.history

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.crypticsamsara.sabikudi.data.preferences.AppRole
import com.crypticsamsara.sabikudi.ui.theme.SabiKudiTheme

data class HistoryReceiptItem(
    val id: Long,
    val merchant: String,
    val date: String,
    val amountKobo: Long,
    val destination: String, // Stock, Split, Unassigned
    val itemCount: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    role: AppRole,
    onNavigateToReceiptDetail: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val extendedColors = SabiKudiTheme.extendedColors
    val scrollState = rememberScrollState()

    var selectedSegment by remember { mutableStateOf("Receipts") } // Receipts | Activity
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") } // All, Stock, Split, Unassigned

    val sampleReceipts = remember {
        listOf(
            HistoryReceiptItem(1L, "Shoprite Lekki", "Nov 12", 2090000L, "Split", 2),
            HistoryReceiptItem(2L, "Mile 12 Market", "Nov 10", 2570000L, "Stock", 3),
            HistoryReceiptItem(3L, "Spar Victoria Island", "Nov 8", 1290000L, "Split", 3),
            HistoryReceiptItem(4L, "Ikeja City Mall", "Nov 5", 775000L, "Unassigned", 2),
            HistoryReceiptItem(5L, "Balogun Wholesale", "Nov 3", 2650000L, "Stock", 3)
        )
    }

    val sampleAuditLogs = remember {
        listOf(
            Pair("Nov 12", "Added receipt \"Shoprite Lekki\" (₦20,900.00)"),
            Pair("Nov 12", "Payment of ₦2,000.00 reported by Ada"),
            Pair("Nov 10", "Added receipt \"Mile 12 Market\" to inventory"),
            Pair("Nov 8", "Split assigned for \"Spar Victoria Island\""),
            Pair("Nov 5", "Added receipt \"Ikeja City Mall\"")
        )
    }

    val filteredReceipts = sampleReceipts.filter { receipt ->
        (selectedFilter == "All" || receipt.destination.equals(selectedFilter, ignoreCase = true)) &&
                (searchQuery.isEmpty() || receipt.merchant.contains(searchQuery, ignoreCase = true))
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Segmented Control: Receipts | Activity
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(extendedColors.surface2, RoundedCornerShape(12.dp))
                .padding(3.dp)
        ) {
            val segments = if (role == AppRole.MEMBER) listOf("Receipts") else listOf("Receipts", "Activity")
            segments.forEach { segment ->
                val selected = segment == selectedSegment
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .background(
                            if (selected) MaterialTheme.colorScheme.surface else androidx.compose.ui.graphics.Color.Transparent,
                            RoundedCornerShape(9.dp)
                        )
                        .clickable { selectedSegment = segment },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = segment,
                        fontSize = 13.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        color = if (selected) extendedColors.navy else extendedColors.muted
                    )
                }
            }
        }

        if (selectedSegment == "Receipts") {
            // Search Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search merchant or item…", fontSize = 13.sp, color = extendedColors.muted) },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = extendedColors.muted) },
                shape = RoundedCornerShape(8.dp),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Filter Chips
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val isBusinessRole = role == AppRole.OWNER || role == AppRole.STAFF
                val filters = if (isBusinessRole) listOf("All", "Stock", "Unassigned") else listOf("All", "Split", "Unassigned")
                filters.forEach { filter ->
                    val selected = filter == selectedFilter
                    val chipBg = when {
                        !selected -> MaterialTheme.colorScheme.surface
                        filter == "Stock" -> extendedColors.tealBg
                        filter == "Split" -> extendedColors.terracottaBg
                        else -> extendedColors.navy
                    }
                    val chipTextColor = when {
                        !selected -> extendedColors.muted
                        filter == "Stock" -> extendedColors.teal
                        filter == "Split" -> extendedColors.terracotta
                        else -> androidx.compose.ui.graphics.Color.White
                    }

                    Box(
                        modifier = Modifier
                            .border(1.dp, if (selected) chipTextColor else extendedColors.line, RoundedCornerShape(99.dp))
                            .background(chipBg, RoundedCornerShape(99.dp))
                            .clickable { selectedFilter = filter }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = filter,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = chipTextColor
                        )
                    }
                }
            }

            // Receipts List
            Text(
                text = "Receipts (${filteredReceipts.size})",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = extendedColors.muted
            )

            if (filteredReceipts.isEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().border(1.dp, extendedColors.line, RoundedCornerShape(12.dp))
                ) {
                    Box(modifier = Modifier.padding(24.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(text = "No matching receipts found.", fontSize = 14.sp, color = extendedColors.muted)
                    }
                }
            } else {
                filteredReceipts.forEach { receipt ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, extendedColors.line, RoundedCornerShape(12.dp))
                            .clickable { onNavigateToReceiptDetail(receipt.id) }
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = receipt.merchant,
                                    fontWeight = FontWeight.Bold,
                                    color = extendedColors.navy,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${receipt.date} · ${receipt.itemCount} items",
                                        fontSize = 12.sp,
                                        color = extendedColors.muted,
                                        maxLines = 1
                                    )

                                    val tagBg = when (receipt.destination) {
                                        "Stock" -> extendedColors.tealBg
                                        "Split" -> extendedColors.terracottaBg
                                        else -> extendedColors.surface2
                                    }
                                    val tagColor = when (receipt.destination) {
                                        "Stock" -> extendedColors.teal
                                        "Split" -> extendedColors.terracotta
                                        else -> extendedColors.muted
                                    }
                                    Box(
                                        modifier = Modifier
                                            .background(tagBg, RoundedCornerShape(99.dp))
                                            .border(1.dp, tagColor, RoundedCornerShape(99.dp))
                                            .padding(horizontal = 8.dp, vertical = 1.dp)
                                    ) {
                                        Text(text = receipt.destination, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = tagColor)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = "₦${String.format("%,.2f", receipt.amountKobo / 100.0)}",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = extendedColors.navy,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        } else {
            // Activity Audit Trail List
            Text(
                text = "Audit Trail",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = extendedColors.muted
            )

            sampleAuditLogs.forEach { (date, message) ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().border(1.dp, extendedColors.line, RoundedCornerShape(12.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = date, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = extendedColors.muted)
                        Text(text = "·", fontSize = 14.sp, color = extendedColors.muted)
                        Text(text = message, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = extendedColors.navy)
                    }
                }
            }
        }
    }
}
