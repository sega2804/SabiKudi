package com.crypticsamsara.sabikudi.ui.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.crypticsamsara.sabikudi.data.preferences.AppMode
import com.crypticsamsara.sabikudi.ui.theme.NavyPrimary
import com.crypticsamsara.sabikudi.ui.theme.SabiKudiTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModeSelectionScreen(
    onModeSelected: (AppMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val extendedColors = SabiKudiTheme.extendedColors

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Choose a mode",
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
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Pick how you'll use Sabikudi. You can change this later in Settings. Nothing is deleted when you switch.",
                fontSize = 14.sp,
                color = extendedColors.muted,
                lineHeight = 20.sp
            )

            // Business Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, extendedColors.line, RoundedCornerShape(12.dp))
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onModeSelected(AppMode.BUSINESS) }
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Business",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = extendedColors.navy
                    )
                    Text(
                        text = "Track stock inventory, low-stock alerts and business purchases.",
                        fontSize = 13.sp,
                        color = extendedColors.muted
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        TagBadge(text = "Stock", textColor = extendedColors.teal, bgColor = extendedColors.tealBg, borderColor = extendedColors.teal)
                    }
                }
            }

            // Shared Expenses Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, extendedColors.line, RoundedCornerShape(12.dp))
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onModeSelected(AppMode.SHARED) }
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Shared expenses",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = extendedColors.navy
                    )
                    Text(
                        text = "Split receipts with flatmates, friends and family. Balances come first.",
                        fontSize = 13.sp,
                        color = extendedColors.muted
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        TagBadge(text = "Split", textColor = extendedColors.terracotta, bgColor = extendedColors.terracottaBg, borderColor = extendedColors.terracotta)
                    }
                }
            }
        }
    }
}

@Composable
fun TagBadge(
    text: String,
    textColor: Color,
    bgColor: Color,
    borderColor: Color
) {
    Box(
        modifier = Modifier
            .border(1.5.dp, borderColor, RoundedCornerShape(99.dp))
            .background(bgColor, RoundedCornerShape(99.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}
