package com.crypticsamsara.sabikudi.ui.screens.onboarding

import androidx.compose.foundation.background
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
    var step by remember { mutableIntStateOf(1) } // 1: Phone, 2: Phone OTP, 3: Email Step-Up Code
    var phoneNumber by remember { mutableStateOf("+234") }
    var otpCode by remember { mutableStateOf("") }
    var emailCode by remember { mutableStateOf("") }
    var maskedEmail by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLimitedSession by remember { mutableStateOf(false) }

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
                        onValueChange = { input ->
                            errorMessage = null
                            phoneNumber = input
                        },
                        label = { Text("Phone Number (E.164 format)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (!errorMessage.isNullOrBlank()) {
                        Text(text = errorMessage!!, color = extendedColors.terracotta, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Button(
                        onClick = {
                            val clean = phoneNumber.trim()
                            if (!clean.startsWith("+") || clean.length < 11) {
                                errorMessage = "Enter phone with country code (e.g., +2348012345678)"
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
                        onValueChange = { input ->
                            val digits = input.filter { it.isDigit() }
                            if (digits.length > 6) {
                                errorMessage = "Code cannot exceed 6 digits"
                                otpCode = digits.take(6)
                            } else {
                                errorMessage = null
                                otpCode = digits
                            }
                        },
                        label = { Text("6-digit SMS Code") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (!errorMessage.isNullOrBlank()) {
                        Text(text = errorMessage!!, color = extendedColors.terracotta, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    if (isLimitedSession) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(extendedColors.terracotta.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "Notice: Signing in on a new device without a recovery email grants a 24-hour limited session. Destructive actions are locked.",
                                fontSize = 12.sp,
                                color = extendedColors.terracotta,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Button(
                        onClick = {
                            if (otpCode.length < 6) {
                                errorMessage = "Code must be 6 digits"
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

                3 -> {
                    Text(
                        text = "Verify Recovery Email",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = extendedColors.navy,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Because you are signing in as an owner on a new device, a security code was sent to $maskedEmail.",
                        fontSize = 13.sp,
                        color = extendedColors.muted,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = emailCode,
                        onValueChange = { input ->
                            val digits = input.filter { it.isDigit() }
                            if (digits.length > 6) {
                                errorMessage = "Code cannot exceed 6 digits"
                                emailCode = digits.take(6)
                            } else {
                                errorMessage = null
                                emailCode = digits
                            }
                        },
                        label = { Text("6-digit Email Code") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (!errorMessage.isNullOrBlank()) {
                        Text(text = errorMessage!!, color = extendedColors.terracotta, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Button(
                        onClick = {
                            if (emailCode.length < 6) {
                                errorMessage = "Code must be 6 digits"
                            } else {
                                onAuthSuccess()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = extendedColors.navy),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text(text = "Verify Email Code & Continue", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
