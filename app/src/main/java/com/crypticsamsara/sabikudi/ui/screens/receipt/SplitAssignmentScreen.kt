package com.crypticsamsara.sabikudi.ui.screens.receipt

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.crypticsamsara.sabikudi.data.preferences.AppRole
import com.crypticsamsara.sabikudi.ui.theme.NavyPrimary
import com.crypticsamsara.sabikudi.ui.theme.SabiKudiTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SplitAssignmentScreen(
    viewModel: NewReceiptViewModel,
    initialMembers: List<String>,
    userRole: AppRole = AppRole.ORGANISER,
    onNavigateToSplitReview: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val extendedColors = SabiKudiTheme.extendedColors

    var selectedSplitType by remember { mutableStateOf(SplitType.EQUAL) }
    var activeMembers by remember { mutableStateOf(initialMembers) }
    var weights by remember { mutableStateOf(mutableMapOf<String, Float>()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val availableTypes = remember(userRole) {
        when (userRole) {
            AppRole.ORGANISER -> listOf(SplitType.EQUAL to "Equal", SplitType.PERCENT to "Percent", SplitType.EXACT to "Exact", SplitType.SHARES to "Shares")
            AppRole.OWNER -> listOf(SplitType.EQUAL to "Equal", SplitType.EXACT to "Exact")
            AppRole.STAFF, AppRole.MEMBER -> listOf(SplitType.EQUAL to "Equal")
        }
    }

    val allPeople = listOf("Ada", "Chidi", "Ngozi", "Tunde")
    val splitItems = state.items.filter { it.destination == ItemDestination.SPLIT }
    val totalSplitAmount = splitItems.sumOf { it.priceInKobo }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Split assignment",
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Split Type Segmented Control
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(extendedColors.surface2, RoundedCornerShape(12.dp))
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                availableTypes.forEach { (type, label) ->
                    val isSelected = selectedSplitType == type
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .background(if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent, RoundedCornerShape(9.dp))
                            .clip(RoundedCornerShape(9.dp))
                            .clickable {
                                selectedSplitType = type
                                errorMessage = null
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) extendedColors.navy else extendedColors.muted
                        )
                    }
                }
            }

            Text(
                text = when (selectedSplitType) {
                    SplitType.EQUAL -> "Who shares these items? Equal split."
                    SplitType.PERCENT -> "Enter percentages for each person. Must add up to 100%."
                    SplitType.EXACT -> "Enter exact ₦ amounts for each person. Must add up to total price."
                    SplitType.SHARES -> "Enter share ratios for each person (e.g. 1×, 2×)."
                },
                fontSize = 13.sp,
                color = extendedColors.muted
            )

            // Member Chips & Weights
            allPeople.forEach { person ->
                val isSelected = activeMembers.contains(person)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .border(1.5.dp, if (isSelected) extendedColors.terracotta else extendedColors.line, RoundedCornerShape(99.dp))
                            .background(if (isSelected) extendedColors.terracottaBg else MaterialTheme.colorScheme.surface, RoundedCornerShape(99.dp))
                            .clip(RoundedCornerShape(99.dp))
                            .clickable {
                                activeMembers = if (isSelected) activeMembers - person else activeMembers + person
                                errorMessage = null
                            }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = person,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) extendedColors.terracotta else extendedColors.muted
                        )
                    }

                    if (isSelected && selectedSplitType != SplitType.EQUAL) {
                        OutlinedTextField(
                            value = weights[person]?.let { if (it % 1f == 0f) it.toInt().toString() else it.toString() } ?: "",
                            onValueChange = { input ->
                                val v = input.toFloatOrNull() ?: 0f
                                weights = weights.toMutableMap().apply { put(person, v) }
                                errorMessage = null
                            },
                            placeholder = { Text(when (selectedSplitType) {
                                SplitType.PERCENT -> "%"
                                SplitType.EXACT -> "₦"
                                SplitType.SHARES -> "×"
                                else -> ""
                            }) },
                            singleLine = true,
                            modifier = Modifier.width(110.dp)
                        )
                    }
                }
            }

            // Split Items Summary
            splitItems.forEach { item ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().border(1.dp, extendedColors.line, RoundedCornerShape(12.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = item.name, fontWeight = FontWeight.Bold, color = extendedColors.navy)
                        Text(text = "₦${String.format("%,.2f", item.priceInKobo / 100.0)}", fontWeight = FontWeight.ExtraBold, color = extendedColors.navy)
                    }
                }
            }

            if (!errorMessage.isNullOrBlank()) {
                Text(text = errorMessage!!, color = extendedColors.terracotta, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    if (activeMembers.isEmpty()) {
                        errorMessage = "Pick at least one person."
                        return@Button
                    }

                    if (selectedSplitType == SplitType.PERCENT) {
                        val sumPercent = activeMembers.sumOf { (weights[it] ?: 0f).toDouble() }
                        if (Math.abs(sumPercent - 100.0) > 0.01) {
                            errorMessage = "Percentages add up to ${sumPercent}%, not 100%."
                            return@Button
                        }
                    }

                    if (selectedSplitType == SplitType.EXACT) {
                        val sumExactKobo = activeMembers.sumOf { Math.round(((weights[it] ?: 0f) * 100).toDouble()) }
                        if (sumExactKobo != totalSplitAmount) {
                            val exactFormatted = String.format("%,.2f", sumExactKobo / 100.0)
                            val totalFormatted = String.format("%,.2f", totalSplitAmount / 100.0)
                            errorMessage = "Amounts add up to ₦$exactFormatted, but the total is ₦$totalFormatted."
                            return@Button
                        }
                    }

                    viewModel.applySplitConfig(selectedSplitType, activeMembers, weights)
                    onNavigateToSplitReview()
                },
                colors = ButtonDefaults.buttonColors(containerColor = extendedColors.terracotta),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text(text = "Review split", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}
