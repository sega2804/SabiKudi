package com.crypticsamsara.sabikudi.ui.screens.onboarding

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.crypticsamsara.sabikudi.data.preferences.AppMode
import com.crypticsamsara.sabikudi.data.preferences.AppRole
import com.crypticsamsara.sabikudi.ui.theme.NavyPrimary
import com.crypticsamsara.sabikudi.ui.theme.SabiKudiTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoleSelectionScreen(
    mode: AppMode,
    onRoleSelected: (AppRole) -> Unit,
    onChangeModeRequested: () -> Unit,
    modifier: Modifier = Modifier
) {
    val extendedColors = SabiKudiTheme.extendedColors
    val isBusiness = mode == AppMode.BUSINESS

    val rolesList = if (isBusiness) {
        listOf(
            AppRole.OWNER to Pair(
                "Owner",
                "I run the business. I see costs and margins, approve purchases and manage staff."
            ),
            AppRole.STAFF to Pair(
                "Staff",
                "I buy and stock items for the owner. My receipts are sent for approval."
            )
        )
    } else {
        listOf(
            AppRole.ORGANISER to Pair(
                "Organiser",
                "I run the group. I create groups, set custom splits, confirm payments and settle up."
            ),
            AppRole.MEMBER to Pair(
                "Member",
                "I share costs. I add receipts, see my own balances and tell the organiser when I have paid."
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Your role",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = NavyPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onChangeModeRequested) {
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
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "${if (isBusiness) "Business" else "Shared expenses"} works differently depending on your role. You can change it later in Settings.",
                fontSize = 14.sp,
                color = extendedColors.muted,
                lineHeight = 20.sp
            )

            rolesList.forEach { (role, info) ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, extendedColors.line, RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onRoleSelected(role) }
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = info.first,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = extendedColors.navy
                        )
                        Text(
                            text = info.second,
                            fontSize = 13.sp,
                            color = extendedColors.muted,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            OutlinedButton(
                onClick = onChangeModeRequested,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(
                    text = "Change mode",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = extendedColors.navy
                )
            }
        }
    }
}
