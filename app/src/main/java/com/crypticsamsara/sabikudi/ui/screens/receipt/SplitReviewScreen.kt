package com.crypticsamsara.sabikudi.ui.screens.receipt

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import com.crypticsamsara.sabikudi.ui.theme.NavyPrimary
import com.crypticsamsara.sabikudi.ui.theme.SabiKudiTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SplitReviewScreen(
    viewModel: NewReceiptViewModel,
    onSplitConfirmed: () -> Unit,
    onBackRequested: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val extendedColors = SabiKudiTheme.extendedColors
    val splitItems = state.items.filter { it.destination == ItemDestination.SPLIT }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Split review",
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Paid By & Split Type Header Card
            Card(
                colors = CardDefaults.cardColors(containerColor = extendedColors.terracottaBg),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().border(1.dp, extendedColors.terracotta, RoundedCornerShape(12.dp))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Paid by ${state.paidBy ?: "—"}",
                        fontWeight = FontWeight.Bold,
                        color = extendedColors.terracotta
                    )
                    Box(
                        modifier = Modifier
                            .background(extendedColors.terracotta, RoundedCornerShape(99.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = splitItems.firstOrNull()?.splitType?.name?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "Equal",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            // Items breakdown
            splitItems.forEach { item ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().border(1.dp, extendedColors.line, RoundedCornerShape(12.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = item.name, fontWeight = FontWeight.Bold, color = extendedColors.navy)
                            Text(text = "₦${String.format("%,.2f", item.priceInKobo / 100.0)}", fontWeight = FontWeight.ExtraBold, color = extendedColors.navy)
                        }

                        item.splitAllocations.forEach { (person, amount) ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = person, fontSize = 13.sp, color = extendedColors.muted)
                                Text(text = "₦${String.format("%,.2f", amount / 100.0)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = extendedColors.navy)
                            }
                        }

                        if (item.splitType == SplitType.EQUAL && item.priceInKobo % item.assignedPeople.size != 0L) {
                            Text(
                                text = "Odd kobo goes to the first people. Same input, same split.",
                                fontSize = 11.sp,
                                color = extendedColors.gold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onSplitConfirmed,
                colors = ButtonDefaults.buttonColors(containerColor = extendedColors.terracotta),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text(text = "Confirm split", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}
