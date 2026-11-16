package com.crypticsamsara.sabikudi.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import com.crypticsamsara.sabikudi.data.local.entities.WorkspaceEntity
import com.crypticsamsara.sabikudi.data.preferences.AppMode
import com.crypticsamsara.sabikudi.data.preferences.AppRole
import com.crypticsamsara.sabikudi.ui.theme.SabiKudiTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkspaceSwitcherSheet(
    workspaces: List<WorkspaceEntity>,
    currentWorkspaceId: Long?,
    onWorkspaceSelected: (WorkspaceEntity) -> Unit,
    onJoinWorkspaceWithCode: (String) -> Unit,
    onCreateWorkspaceRequested: () -> Unit,
    onDismissRequest: () -> Unit
) {
    val extendedColors = SabiKudiTheme.extendedColors
    var joinCodeInput by remember { mutableStateOf("") }
    var showJoinDialog by remember { mutableStateOf(false) }

    ModalBottomSheet(onDismissRequest = onDismissRequest) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Workspaces",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = extendedColors.navy
                )
                Text(
                    text = "One phone, many spaces",
                    fontSize = 12.sp,
                    color = extendedColors.muted
                )
            }

            Text(
                text = "Switch workspace to view its receipts, balances, and role.",
                fontSize = 13.sp,
                color = extendedColors.muted
            )

            val displayWorkspaces = if (workspaces.isEmpty()) {
                listOf(
                    WorkspaceEntity(id = 1L, name = "Main Shop", mode = "BUSINESS", inviteCode = "SBK-1001", isCurrent = true),
                    WorkspaceEntity(id = 2L, name = "Flatmates Expenses", mode = "SHARED", inviteCode = "FLT-2002", isCurrent = false)
                )
            } else {
                workspaces
            }

            displayWorkspaces.forEach { ws ->
                val isSelected = ws.id == currentWorkspaceId || (currentWorkspaceId == null && ws.isCurrent)
                val mode = if (ws.mode == "BUSINESS") AppMode.BUSINESS else AppMode.SHARED
                val role = if (mode == AppMode.BUSINESS) AppRole.OWNER else AppRole.ORGANISER

                val tagColor = if (mode == AppMode.BUSINESS) extendedColors.teal else extendedColors.terracotta
                val tagBg = if (mode == AppMode.BUSINESS) extendedColors.tealBg else extendedColors.terracottaBg

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) extendedColors.surface2 else MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) extendedColors.navy else extendedColors.line,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable {
                            onWorkspaceSelected(ws)
                            onDismissRequest()
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = ws.name,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = extendedColors.navy
                                )
                                if (isSelected) {
                                    Text(
                                        text = "✓ Active",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = extendedColors.teal
                                    )
                                }
                            }
                            Text(
                                text = "Code: ${ws.inviteCode} · Role: ${role.name.lowercase().replaceFirstChar { it.uppercase() }}",
                                fontSize = 12.sp,
                                color = extendedColors.muted
                            )
                        }

                        Box(
                            modifier = Modifier
                                .border(1.dp, tagColor, RoundedCornerShape(99.dp))
                                .background(tagBg, RoundedCornerShape(99.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (mode == AppMode.BUSINESS) "Business" else "Shared",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = tagColor
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = extendedColors.line)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { showJoinDialog = true },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                ) {
                    Text(text = "Join with Code", fontWeight = FontWeight.Bold, color = extendedColors.navy)
                }

                Button(
                    onClick = {
                        onCreateWorkspaceRequested()
                        onDismissRequest()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = extendedColors.navy),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                ) {
                    Text(text = "+ New Space", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }
    }

    if (showJoinDialog) {
        AlertDialog(
            onDismissRequest = { showJoinDialog = false },
            title = {
                Text(
                    text = "Join Workspace",
                    fontWeight = FontWeight.Bold,
                    color = extendedColors.navy
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Enter the 8-character invite code shared by the workspace owner (e.g. SBK-8821 or FLT-2002):",
                        fontSize = 13.sp,
                        color = extendedColors.muted
                    )
                    OutlinedTextField(
                        value = joinCodeInput,
                        onValueChange = { joinCodeInput = it.uppercase() },
                        placeholder = { Text("e.g. SBK-8821") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (joinCodeInput.isNotBlank()) {
                            onJoinWorkspaceWithCode(joinCodeInput)
                            showJoinDialog = false
                            onDismissRequest()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = extendedColors.navy)
                ) {
                    Text(text = "Join Space", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showJoinDialog = false }) {
                    Text(text = "Cancel")
                }
            }
        )
    }
}
