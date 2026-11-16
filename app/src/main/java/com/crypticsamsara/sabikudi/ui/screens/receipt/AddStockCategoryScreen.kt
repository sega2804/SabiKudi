package com.crypticsamsara.sabikudi.ui.screens.receipt

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.crypticsamsara.sabikudi.ui.theme.NavyPrimary
import com.crypticsamsara.sabikudi.ui.theme.SabiKudiTheme
import com.crypticsamsara.sabikudi.ui.screens.onboarding.TagBadge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddStockCategoryScreen(
    viewModel: NewReceiptViewModel,
    itemIndex: Int,
    onStockSaved: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val extendedColors = SabiKudiTheme.extendedColors
    val item = state.items.getOrNull(itemIndex) ?: state.items.first()

    var quantity by remember { mutableStateOf(item.stkQuantity.toString()) }
    var threshold by remember { mutableStateOf(item.stkThreshold.toString()) }
    var selectedCategory by remember { mutableStateOf(item.category) }
    var customCategory by remember { mutableStateOf("") }

    val presetCategories = listOf("Groceries", "Beverages", "Toiletries", "Packaging", "Supplies", "Other")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Add to stock",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = NavyPrimary
                    )
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Item Header Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().border(1.dp, extendedColors.line, RoundedCornerShape(12.dp))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = item.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = extendedColors.navy)
                    Text(text = "₦${String.format("%,.2f", item.priceInKobo / 100.0)}", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = extendedColors.navy)
                }
            }

            // Quantity & Threshold
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Quantity on hand") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = threshold,
                    onValueChange = { threshold = it },
                    label = { Text("Low threshold") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            Text(
                text = "Category",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = extendedColors.navy
            )

            // Category Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                presetCategories.take(3).forEach { cat ->
                    val isSelected = selectedCategory == cat
                    Surface(
                        onClick = { selectedCategory = cat },
                        shape = RoundedCornerShape(99.dp),
                        color = if (isSelected) extendedColors.teal else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.5.dp, if (isSelected) extendedColors.teal else extendedColors.line),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Box(modifier = Modifier.padding(horizontal = 12.dp), contentAlignment = androidx.compose.ui.Alignment.Center) {
                            Text(text = cat, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isSelected) androidx.compose.ui.graphics.Color.White else extendedColors.muted)
                        }
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                presetCategories.drop(3).forEach { cat ->
                    val isSelected = selectedCategory == cat
                    Surface(
                        onClick = { selectedCategory = cat },
                        shape = RoundedCornerShape(99.dp),
                        color = if (isSelected) extendedColors.teal else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.5.dp, if (isSelected) extendedColors.teal else extendedColors.line),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Box(modifier = Modifier.padding(horizontal = 12.dp), contentAlignment = androidx.compose.ui.Alignment.Center) {
                            Text(text = cat, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isSelected) androidx.compose.ui.graphics.Color.White else extendedColors.muted)
                        }
                    }
                }
            }

            OutlinedTextField(
                value = customCategory,
                onValueChange = {
                    customCategory = it
                    if (it.isNotBlank()) selectedCategory = it.trim()
                },
                placeholder = { Text("Or type a new category") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    val q = quantity.toIntOrNull() ?: 10
                    val t = threshold.toIntOrNull() ?: 3
                    viewModel.updateStockConfig(itemIndex, selectedCategory, q, t)
                    onStockSaved()
                },
                colors = ButtonDefaults.buttonColors(containerColor = extendedColors.navy),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text(text = "Save to stock", fontWeight = FontWeight.Bold)
            }
        }
    }
}
