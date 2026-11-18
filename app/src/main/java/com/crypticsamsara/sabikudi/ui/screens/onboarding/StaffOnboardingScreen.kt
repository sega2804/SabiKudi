package com.crypticsamsara.sabikudi.ui.screens.onboarding

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.crypticsamsara.sabikudi.ui.theme.SabiKudiTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffOnboardingScreen(
    onOnboardingComplete: () -> Unit,
    onBackRequested: () -> Unit,
    modifier: Modifier = Modifier
) {
    val extendedColors = SabiKudiTheme.extendedColors
    var step by remember { mutableIntStateOf(1) } // 1: Code, 2: SMS, 3: Consent, 4: Waiting
    var workspaceCode by remember { mutableStateOf("") }
    var smsCode by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Join Workspace", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = extendedColors.navy) }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (step) {
                1 -> {
                    Text(
                        text = "Enter Workspace Code",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = extendedColors.navy
                    )
                    Text(
                        text = "Ask your business owner for a 24-hour workspace invite code.",
                        fontSize = 13.sp,
                        color = extendedColors.muted,
                        textAlign = TextAlign.Center
                    )

                    OutlinedTextField(
                        value = workspaceCode,
                        onValueChange = { input ->
                            if (input.length > 8) {
                                errorMessage = "Code cannot exceed 8 characters"
                                workspaceCode = input.uppercase().take(8)
                            } else {
                                errorMessage = null
                                workspaceCode = input.uppercase()
                            }
                        },
                        label = { Text("Workspace Code (e.g. SBK-8821)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (!errorMessage.isNullOrBlank()) {
                        Text(text = errorMessage!!, color = extendedColors.terracotta, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            if (workspaceCode.length > 8) {
                                errorMessage = "Code cannot exceed 8 characters"
                            } else if (workspaceCode.length < 4) {
                                errorMessage = "Code must be at least 4 characters"
                            } else {
                                errorMessage = null
                                step = 2
                            }
                        },
                        enabled = workspaceCode.length >= 4,
                        colors = ButtonDefaults.buttonColors(containerColor = extendedColors.navy),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text(text = "Continue", fontWeight = FontWeight.Bold)
                    }
                }

                2 -> {
                    Text(
                        text = "SMS Verification",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = extendedColors.navy
                    )
                    Text(
                        text = "Enter the 6-digit verification code sent to your phone number.",
                        fontSize = 13.sp,
                        color = extendedColors.muted,
                        textAlign = TextAlign.Center
                    )

                    OutlinedTextField(
                        value = smsCode,
                        onValueChange = { input ->
                            val digits = input.filter { it.isDigit() }
                            if (digits.length > 6) {
                                errorMessage = "Code cannot exceed 6 digits"
                                smsCode = digits.take(6)
                            } else {
                                errorMessage = null
                                smsCode = digits
                            }
                        },
                        label = { Text("SMS Verification Code") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (!errorMessage.isNullOrBlank()) {
                        Text(text = errorMessage!!, color = extendedColors.terracotta, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            if (smsCode.length > 6) {
                                errorMessage = "Code cannot exceed 6 digits"
                            } else if (smsCode.length < 6) {
                                errorMessage = "Code must be 6 digits"
                            } else {
                                errorMessage = null
                                step = 3
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = extendedColors.navy),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text(text = "Verify Code", fontWeight = FontWeight.Bold)
                    }
                }

                3 -> {
                    Text(
                        text = "Workspace Policy & Consent",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = extendedColors.navy
                    )

                    Card(
                        colors = CardDefaults.cardColors(containerColor = extendedColors.surface2),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().border(1.dp, extendedColors.line, RoundedCornerShape(12.dp))
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(text = "• Receipts you save will be submitted to the business owner.", fontSize = 13.sp, color = extendedColors.navy)
                            Text(text = "• App requires internet sync at least once every 7 days.", fontSize = 13.sp, color = extendedColors.navy)
                            Text(text = "• Photo attachments are automatically purged 30 days after review.", fontSize = 13.sp, color = extendedColors.navy)
                        }
                    }

                    Button(
                        onClick = { step = 4 },
                        colors = ButtonDefaults.buttonColors(containerColor = extendedColors.teal),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text(text = "I Agree & Join", fontWeight = FontWeight.Bold)
                    }
                }

                4 -> {
                    Text(
                        text = "Waiting for Owner Approval",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = extendedColors.navy
                    )
                    Text(
                        text = "Your request has been submitted. The business owner will review and activate your staff device.",
                        fontSize = 13.sp,
                        color = extendedColors.muted,
                        textAlign = TextAlign.Center
                    )

                    CircularProgressIndicator(color = extendedColors.teal)

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = onOnboardingComplete,
                        colors = ButtonDefaults.buttonColors(containerColor = extendedColors.navy),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text(text = "Go to Dashboard", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
