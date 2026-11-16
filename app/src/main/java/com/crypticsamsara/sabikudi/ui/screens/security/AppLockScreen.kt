package com.crypticsamsara.sabikudi.ui.screens.security

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.crypticsamsara.sabikudi.ui.theme.NavyPrimary
import com.crypticsamsara.sabikudi.ui.theme.SabiKudiTheme

@Composable
fun AppLockScreen(
    onUnlocked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val extendedColors = SabiKudiTheme.extendedColors
    var pin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(24.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(NavyPrimary, RoundedCornerShape(22.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "S", fontSize = 44.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.surface)
            }

            Text(
                text = "Sabikudi is locked",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = extendedColors.navy
            )

            OutlinedTextField(
                value = pin,
                onValueChange = {
                    pin = it
                    errorMessage = false
                },
                placeholder = { Text("PIN", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.width(200.dp)
            )

            Text(
                text = if (errorMessage) "Wrong PIN. Try 1234." else "Demo PIN: 1234",
                fontSize = 13.sp,
                color = if (errorMessage) extendedColors.terracotta else extendedColors.muted
            )

            Button(
                onClick = {
                    if (pin == "1234") {
                        onUnlocked()
                    } else {
                        errorMessage = true
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = extendedColors.navy),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.width(200.dp).height(48.dp)
            ) {
                Text(text = "Unlock", fontWeight = FontWeight.Bold)
            }
        }
    }
}
