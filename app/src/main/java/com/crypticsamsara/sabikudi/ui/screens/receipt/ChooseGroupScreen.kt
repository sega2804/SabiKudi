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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.crypticsamsara.sabikudi.ui.theme.NavyPrimary
import com.crypticsamsara.sabikudi.ui.theme.SabiKudiTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChooseGroupScreen(
    merchantName: String = "Shoprite Ikeja",
    onGroupSelected: (List<String>) -> Unit,
    onNavigateToGroupsManager: () -> Unit,
    modifier: Modifier = Modifier
) {
    val extendedColors = SabiKudiTheme.extendedColors
    val suggestedGroup = Pair("Flatmates", listOf("Ada", "Chidi", "Ngozi"))
    val otherGroups = listOf(
        Pair("Shop staff", listOf("Tunde", "Ada"))
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Choose group",
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Suggested Group Card
            Card(
                colors = CardDefaults.cardColors(containerColor = extendedColors.terracottaBg),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, extendedColors.terracotta, RoundedCornerShape(12.dp))
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onGroupSelected(suggestedGroup.second) }
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
                        Box(
                            modifier = Modifier
                                .border(1.dp, extendedColors.terracotta, RoundedCornerShape(99.dp))
                                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(99.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Suggested",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = extendedColors.terracotta
                            )
                        }
                        Text(text = "›", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = extendedColors.terracotta)
                    }

                    Text(
                        text = suggestedGroup.first,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = extendedColors.navy
                    )
                    Text(
                        text = "Used last time at $merchantName · ${suggestedGroup.second.joinToString(", ")}",
                        fontSize = 12.sp,
                        color = extendedColors.muted
                    )
                }
            }

            Text(
                text = "Other groups",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = extendedColors.muted
            )

            otherGroups.forEach { (groupName, members) ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, extendedColors.line, RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onGroupSelected(members) }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(text = groupName, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = extendedColors.navy)
                            Text(text = members.joinToString(", "), fontSize = 13.sp, color = extendedColors.muted)
                        }
                        Text(text = "›", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = extendedColors.muted)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = { onGroupSelected(listOf("Ada", "Chidi", "Ngozi", "Tunde")) },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text(text = "One-off split: add people", fontWeight = FontWeight.Bold, color = extendedColors.navy)
            }

            OutlinedButton(
                onClick = onNavigateToGroupsManager,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text(text = "Manage groups", fontWeight = FontWeight.Bold, color = extendedColors.navy)
            }
        }
    }
}
