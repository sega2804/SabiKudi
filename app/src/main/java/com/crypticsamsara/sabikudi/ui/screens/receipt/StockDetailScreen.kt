package com.crypticsamsara.sabikudi.ui.screens.receipt

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
import com.crypticsamsara.sabikudi.data.preferences.AppRole
import com.crypticsamsara.sabikudi.ui.theme.SabiKudiTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockDetailScreen(
    itemId: Long,
    role: AppRole,
    onBackRequested: () -> Unit,
    modifier: Modifier = Modifier
) {
    val extendedColors = SabiKudiTheme.extendedColors
    val scrollState = rememberScrollState()
    val isOwner = role == AppRole.OWNER

    var itemName by remember { mutableStateOf("Indomie carton") }
    var category by remember { mutableStateOf("Groceries") }
    var quantity by remember { mutableIntStateOf(3) }
    var threshold by remember { mutableIntStateOf(5) }
    var costPriceText by remember { mutableStateOf("3500.00") }
    var sellPriceText by remember { mutableStateOf("4500.00") }
    var expiryDateText by remember { mutableStateOf("2026-11-25") }

    val cost = costPriceText.toDoubleOrNull() ?: 0.0
    val sell = sellPriceText.toDoubleOrNull() ?: 0.0
    val profitPerUnit = if (sell > cost) sell - cost else 0.0
    val margin = if (sell > 0) (((sell - cost) / sell) * 100).toInt() else 0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Stock Item Detail", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = extendedColors.navy) },
                navigationIcon = {
                    IconButton(onClick = onBackRequested) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().border(1.dp, extendedColors.line, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = itemName, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = extendedColors.navy)
                    Text(text = "Category: $category", fontSize = 13.sp, color = extendedColors.muted)
                }
            }

            // Quantity Adjuster Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().border(1.dp, extendedColors.line, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(text = "Stock Quantity", fontWeight = FontWeight.Bold, color = extendedColors.navy)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { if (quantity > 0) quantity-- },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(text = "−", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = extendedColors.navy)
                        }

                        Text(
                            text = "$quantity in stock",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = extendedColors.navy
                        )

                        OutlinedButton(
                            onClick = { quantity++ },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(text = "+", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = extendedColors.navy)
                        }
                    }

                    if (quantity <= threshold) {
                        Surface(
                            color = extendedColors.goldBg,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "⚠ Below low stock threshold ($threshold). Time to restock.",
                                color = extendedColors.gold,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }
            }

            // Low Stock Threshold Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().border(1.dp, extendedColors.line, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "Low Stock Alert Threshold", fontWeight = FontWeight.Bold, color = extendedColors.navy)
                    OutlinedTextField(
                        value = threshold.toString(),
                        onValueChange = { threshold = it.toIntOrNull() ?: 0 },
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Owner Only: Cost, Margins & Price Trends
            if (isOwner) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().border(1.dp, extendedColors.line, RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(text = "Price Trends & Profit Margin (Owner)", fontWeight = FontWeight.Bold, color = extendedColors.navy)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = costPriceText,
                                onValueChange = { costPriceText = it },
                                label = { Text("Cost Basis (₦)") },
                                singleLine = true,
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = sellPriceText,
                                onValueChange = { sellPriceText = it },
                                label = { Text("Selling Price (₦)") },
                                singleLine = true,
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Surface(
                            color = extendedColors.tealBg,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Margin $margin% · profit ₦${String.format("%,.2f", profitPerUnit)} per unit",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = extendedColors.teal
                                )
                            }
                        }

                        OutlinedTextField(
                            value = expiryDateText,
                            onValueChange = { expiryDateText = it },
                            label = { Text("Expiry Date (YYYY-MM-DD)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            Button(
                onClick = onBackRequested,
                colors = ButtonDefaults.buttonColors(containerColor = extendedColors.teal),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text(text = "Save Stock Details", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}
