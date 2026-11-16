package com.crypticsamsara.sabikudi.ui.screens.receipt

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.crypticsamsara.sabikudi.ui.theme.NavyPrimary
import com.crypticsamsara.sabikudi.ui.theme.SabiKudiTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewReceiptScreen(
    viewModel: NewReceiptViewModel,
    onNavigateToCameraScan: () -> Unit,
    onNavigateToReview: () -> Unit,
    onBackRequested: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val extendedColors = SabiKudiTheme.extendedColors
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "New receipt",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = NavyPrimary
                    )
                },
                actions = {
                    OutlinedButton(
                        onClick = onNavigateToCameraScan,
                        shape = RoundedCornerShape(99.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Scan",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Scan", fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
            Text(
                text = "Type the details below, or tap the camera to scan a receipt.",
                fontSize = 13.sp,
                color = extendedColors.muted
            )

            // Merchant and Date Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, extendedColors.line, RoundedCornerShape(12.dp))
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = state.merchant,
                        onValueChange = { viewModel.updateMerchant(it) },
                        label = { Text("Merchant") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = state.date,
                        onValueChange = { viewModel.updateDate(it) },
                        label = { Text("Date") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Text(
                text = "Items",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = extendedColors.muted
            )

            state.items.forEachIndexed { index, item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = item.name,
                        onValueChange = { viewModel.updateItemName(index, it) },
                        label = { Text("Item name") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = (item.priceInKobo / 100.0).toString(),
                        onValueChange = {
                            val parsed = (it.toDoubleOrNull() ?: 0.0) * 100
                            viewModel.updateItemPrice(index, parsed.toLong())
                        },
                        label = { Text("Price (₦)") },
                        singleLine = true,
                        modifier = Modifier.width(120.dp)
                    )
                }
            }

            OutlinedButton(
                onClick = { viewModel.addItem() },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text(text = "+ Add item", fontWeight = FontWeight.Bold, color = extendedColors.navy)
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
                onClick = onNavigateToReview,
                colors = ButtonDefaults.buttonColors(containerColor = extendedColors.navy),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text(text = "Continue to review", fontWeight = FontWeight.Bold)
            }
        }
    }
}
