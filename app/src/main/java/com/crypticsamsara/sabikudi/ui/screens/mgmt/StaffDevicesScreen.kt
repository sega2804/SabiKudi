package com.crypticsamsara.sabikudi.ui.screens.mgmt

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.crypticsamsara.sabikudi.ui.theme.SabiKudiTheme

data class StaffDeviceItem(
    val id: Long,
    val name: String,
    val code: String,
    val status: String, // Active, Pending, Suspended, Locked
    val offlineDays: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffDevicesScreen(
    onBackRequested: () -> Unit,
    modifier: Modifier = Modifier
) {
    val extendedColors = SabiKudiTheme.extendedColors
    val scrollState = rememberScrollState()

    var staffDevices by remember {
        mutableStateOf(
            listOf(
                StaffDeviceItem(1L, "Tunde", "SBK-8821", "Active", 7),
                StaffDeviceItem(2L, "Ngozi", "SBK-9412", "Pending", 7)
            )
        )
    }

    var selectedDeviceForDetail by remember { mutableStateOf<StaffDeviceItem?>(null) }
    var showInviteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Staff & Devices", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = extendedColors.navy) },
                navigationIcon = {
                    IconButton(onClick = onBackRequested) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showInviteDialog = true },
                containerColor = extendedColors.teal,
                contentColor = androidx.compose.ui.graphics.Color.White,
                shape = RoundedCornerShape(99.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Invite Staff")
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = "Connected Staff Devices (${staffDevices.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = extendedColors.muted)

            staffDevices.forEach { device ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, extendedColors.line, RoundedCornerShape(12.dp))
                        .clickable { selectedDeviceForDetail = device }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(text = device.name, fontWeight = FontWeight.Bold, color = extendedColors.navy)
                            Text(text = "Invite code: ${device.code} · ${device.offlineDays}-day offline limit", fontSize = 12.sp, color = extendedColors.muted)
                        }

                        val tagBg = when (device.status) {
                            "Active" -> extendedColors.tealBg
                            "Pending" -> extendedColors.warning
                            else -> extendedColors.surface2
                        }
                        val tagColor = when (device.status) {
                            "Active" -> extendedColors.teal
                            "Pending" -> extendedColors.gold
                            else -> extendedColors.alert
                        }

                        Box(
                            modifier = Modifier
                                .border(1.dp, tagColor, RoundedCornerShape(99.dp))
                                .background(tagBg, RoundedCornerShape(99.dp))
                                .padding(horizontal = 10.dp, vertical = 3.dp)
                        ) {
                            Text(text = device.status, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = tagColor)
                        }
                    }
                }
            }
        }
    }

    // Staff Detail Sheet
    selectedDeviceForDetail?.let { device ->
        ModalBottomSheet(onDismissRequest = { selectedDeviceForDetail = null }) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(text = "${device.name} · Device Settings", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = extendedColors.navy)
                Text(text = "Status: ${device.status} | Code: ${device.code}", fontSize = 13.sp, color = extendedColors.muted)

                HorizontalDivider(color = extendedColors.line)

                Text(text = "Permissions", fontWeight = FontWeight.Bold, color = extendedColors.navy)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = true, onCheckedChange = {})
                    Text(text = "Add receipts & camera scan", fontSize = 13.sp, color = extendedColors.navy)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = true, onCheckedChange = {})
                    Text(text = "See stock quantities", fontSize = 13.sp, color = extendedColors.navy)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = false, onCheckedChange = {})
                    Text(text = "See costs and margins (Owner only)", fontSize = 13.sp, color = extendedColors.muted)
                }

                HorizontalDivider(color = extendedColors.line)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            staffDevices = staffDevices.map { if (it.id == device.id) it.copy(status = "Suspended") else it }
                            selectedDeviceForDetail = null
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "Suspend", fontWeight = FontWeight.Bold, color = extendedColors.navy)
                    }

                    Button(
                        onClick = {
                            staffDevices = staffDevices.filter { it.id != device.id }
                            selectedDeviceForDetail = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = extendedColors.alert),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "Remove & Wipe", fontWeight = FontWeight.Bold, color = androidx.compose.ui.graphics.Color.White)
                    }
                }
            }
        }
    }

    // Invite Dialog
    if (showInviteDialog) {
        AlertDialog(
            onDismissRequest = { showInviteDialog = false },
            title = { Text("Invite Staff", fontWeight = FontWeight.Bold, color = extendedColors.navy) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Generated 24-hour code:", fontSize = 13.sp, color = extendedColors.muted)
                    Text("SBK-9952", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = extendedColors.teal)
                    Text("Share this code via WhatsApp or SMS to allow your staff to connect.", fontSize = 12.sp, color = extendedColors.muted)
                }
            },
            confirmButton = {
                Button(
                    onClick = { showInviteDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = extendedColors.navy)
                ) {
                    Text("Done")
                }
            }
        )
    }
}
