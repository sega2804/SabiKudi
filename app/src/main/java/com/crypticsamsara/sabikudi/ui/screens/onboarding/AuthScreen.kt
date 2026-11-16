package com.crypticsamsara.sabikudi.ui.screens.onboarding

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.crypticsamsara.sabikudi.ui.theme.NavyPrimary
import com.crypticsamsara.sabikudi.ui.theme.SabiKudiTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit,
    onJoinWorkspaceRequested: () -> Unit,
    modifier: Modifier = Modifier
) {
    val extendedColors = SabiKudiTheme.extendedColors
    var step by remember { mutableIntStateOf(1) } // 1: Phone number, 2: OTP verification
    var phoneNumber by remember { mutableStateOf("+234 ") }
    var otpCode by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Sign in to Sabikudi",
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
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            when (step) {
                1 -> {
                    Text(
                        text = "What is your phone number?",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = extendedColors.navy,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Sabikudi links your receipts, balances, and workspaces securely to your phone number.",
                        fontSize = 13.sp,
                        color = extendedColors.muted,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it; errorMessage = null },
                        label = { Text("Phone Number") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (!errorMessage.isNullOrBlank()) {
                        Text(text = errorMessage!!, color = extendedColors.terracotta, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Button(
                        onClick = {
                            if (phoneNumber.length < 8) {
                                errorMessage = "Please enter a valid phone number."
                            } else {
                                step = 2
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = extendedColors.navy),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text(text = "Send verification code", fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onJoinWorkspaceRequested,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text(text = "Join existing workspace with code", fontWeight = FontWeight.Bold, color = extendedColors.navy)
                    }
                }

                2 -> {
                    Text(
                        text = "Enter verification code",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = extendedColors.navy,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "We sent a 6-digit SMS code to $phoneNumber",
                        fontSize = 13.sp,
                        color = extendedColors.muted,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = otpCode,
                        onValueChange = { otpCode = it; errorMessage = null },
                        label = { Text("6-digit SMS Code") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (!errorMessage.isNullOrBlank()) {
                        Text(text = errorMessage!!, color = extendedColors.terracotta, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Button(
                        onClick = {
                            if (otpCode.length < 4) {
                                errorMessage = "Please enter the verification code."
                            } else {
                                onAuthSuccess()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = extendedColors.teal),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text(text = "Verify & Continue", fontWeight = FontWeight.Bold)
                    }

                    TextButton(
                        onClick = { step = 1 },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Change phone number", color = extendedColors.muted)
                    }
                }
            }
        }
    }
}
