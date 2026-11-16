package com.crypticsamsara.sabikudi.ui.screens.receipt

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.crypticsamsara.sabikudi.ui.theme.NavyPrimary
import com.crypticsamsara.sabikudi.ui.theme.SabiKudiTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraScanScreen(
    onCaptureComplete: () -> Unit,
    onManualEntryRequested: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val extendedColors = SabiKudiTheme.extendedColors

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var permissionDenied by remember { mutableStateOf(false) }
    var showOcrFailSheet by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
        if (!granted) {
            permissionDenied = true
        }
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission && !permissionDenied) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Scan receipt",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = NavyPrimary
                    )
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            if (!hasCameraPermission) {
                // Camera Denied / Off view matching mockup
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(extendedColors.surface2)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(extendedColors.warning, RoundedCornerShape(20.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "!", fontSize = 36.sp, fontWeight = FontWeight.Bold, color = extendedColors.gold)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Camera is off",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = extendedColors.navy
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Sabikudi needs the camera to scan receipts. You can still type receipts in manually.",
                        fontSize = 14.sp,
                        color = extendedColors.muted,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                        colors = ButtonDefaults.buttonColors(containerColor = extendedColors.navy),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text(text = "Open settings", fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = onManualEntryRequested,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text(text = "Enter manually", fontWeight = FontWeight.Bold, color = extendedColors.navy)
                    }
                }
            } else {
                // Camera View Finder with dashed frame
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(vertical = 24.dp)
                            .border(2.dp, Color.White.copy(alpha = 0.6f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Align the receipt in the frame",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = onCaptureComplete,
                            colors = ButtonDefaults.buttonColors(containerColor = extendedColors.terracotta),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Text(text = "Capture", fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        OutlinedButton(
                            onClick = { showOcrFailSheet = true },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Text(text = "Simulate unreadable photo", fontWeight = FontWeight.Medium)
                        }

                        TextButton(
                            onClick = onManualEntryRequested,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "Enter manually instead", color = Color.White.copy(alpha = 0.8f))
                        }
                    }
                }
            }

            // OCR Failure Dialog Sheet
            if (showOcrFailSheet) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.6f)),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Couldn't read this receipt",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = extendedColors.navy
                            )
                            Text(
                                text = "The photo looks blurry or too dark. Retake it, or type the receipt in.",
                                fontSize = 14.sp,
                                color = extendedColors.muted
                            )
                            Button(
                                onClick = { showOcrFailSheet = false },
                                colors = ButtonDefaults.buttonColors(containerColor = extendedColors.navy),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth().height(48.dp)
                            ) {
                                Text(text = "Retake photo", fontWeight = FontWeight.Bold)
                            }
                            OutlinedButton(
                                onClick = {
                                    showOcrFailSheet = false
                                    onManualEntryRequested()
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth().height(48.dp)
                            ) {
                                Text(text = "Enter manually", fontWeight = FontWeight.Bold, color = extendedColors.navy)
                            }
                        }
                    }
                }
            }
        }
    }
}
