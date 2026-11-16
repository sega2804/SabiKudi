package com.crypticsamsara.sabikudi.ui.screens.splash

import android.app.Activity
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Lock system status bar & navigation bar to pitch black with light icons during splash
    val view = LocalView.current
    DisposableEffect(Unit) {
        val window = (view.context as Activity).window
        val controller = WindowCompat.getInsetsController(window, view)
        val prevStatusBar = window.statusBarColor
        val prevNavColor = window.navigationBarColor
        val prevLightStatus = controller.isAppearanceLightStatusBars
        val prevLightNav = controller.isAppearanceLightNavigationBars

        window.statusBarColor = android.graphics.Color.BLACK
        window.navigationBarColor = android.graphics.Color.BLACK
        controller.isAppearanceLightStatusBars = false
        controller.isAppearanceLightNavigationBars = false

        onDispose {
            window.statusBarColor = prevStatusBar
            window.navigationBarColor = prevNavColor
            controller.isAppearanceLightStatusBars = prevLightStatus
            controller.isAppearanceLightNavigationBars = prevLightNav
        }
    }

    // Animation progress states matching v10 mockup timing
    val logoScale = remember { Animatable(0.3f) }
    val logoAlpha = remember { Animatable(0f) }
    val textAlpha = remember { Animatable(0f) }
    val textOffsetY = remember { Animatable(24f) }

    LaunchedEffect(Unit) {
        // Logo spring pop and fade in
        logoAlpha.animateTo(1f, animationSpec = tween(350))
        logoScale.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 750, easing = CubicBezierEasing(0.34f, 1.56f, 0.64f, 1f))
        )

        delay(150)

        // Text slide up and fade in
        textAlpha.animateTo(1f, animationSpec = tween(450))
        textOffsetY.animateTo(0f, animationSpec = tween(450, easing = FastOutSlowInEasing))

        // Wait before proceeding
        delay(1300)
        onSplashFinished()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable { onSplashFinished() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            // Receipt Stamp Logo Vector Canvas Rendering
            Box(
                modifier = Modifier
                    .size(170.dp)
                    .scale(logoScale.value)
                    .alpha(logoAlpha.value),
                contentAlignment = Alignment.Center
            ) {
                SabikudiLogoCanvas(modifier = Modifier.fillMaxSize())
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Typography: SabiKudi - know your money.
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .offset(y = textOffsetY.value.dp)
                    .alpha(textAlpha.value)
            ) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = Color(0xFFFFFFFF))) {
                            append("Sabi")
                        }
                        withStyle(SpanStyle(color = Color(0xFF3DD598))) {
                            append("Kudi")
                        }
                    },
                    fontSize = 44.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.5).sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "know your money.",
                    fontSize = 15.sp,
                    color = Color(0xFFA7B1AB),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun SabikudiLogoCanvas(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val width = size.width
        val scale = width / 96f

        // Draw receipt body path
        val receiptPath = Path().apply {
            moveTo(22f * scale, 8f * scale)
            lineTo(62f * scale, 8f * scale)
            lineTo(78f * scale, 24f * scale)
            lineTo(78f * scale, 88f * scale)
            lineTo(70f * scale, 80f * scale)
            lineTo(62f * scale, 88f * scale)
            lineTo(54f * scale, 80f * scale)
            lineTo(46f * scale, 88f * scale)
            lineTo(38f * scale, 80f * scale)
            lineTo(30f * scale, 88f * scale)
            lineTo(22f * scale, 80f * scale)
            close()
        }
        drawPath(receiptPath, color = Color(0xFF3DD598))

        // Folded corner
        val foldPath = Path().apply {
            moveTo(62f * scale, 8f * scale)
            lineTo(62f * scale, 24f * scale)
            lineTo(78f * scale, 24f * scale)
            close()
        }
        drawPath(foldPath, color = Color(0xFF9A8CFF))

        // Top circle dot
        drawCircle(color = Color(0xFFF2B632), radius = 5f * scale, center = Offset(44f * scale, 20f * scale))

        // Item text lines
        val linePaintColor = Color(0xFF000000)
        drawRect(color = linePaintColor, topLeft = Offset(30f * scale, 30f * scale), size = Size(24f * scale, 4.5f * scale))
        drawRect(color = linePaintColor, topLeft = Offset(60f * scale, 30f * scale), size = Size(12f * scale, 4.5f * scale))
        drawRect(color = linePaintColor, topLeft = Offset(30f * scale, 39f * scale), size = Size(24f * scale, 4.5f * scale))
        drawRect(color = linePaintColor, topLeft = Offset(60f * scale, 39f * scale), size = Size(12f * scale, 4.5f * scale))
        drawRect(color = linePaintColor, topLeft = Offset(30f * scale, 48f * scale), size = Size(24f * scale, 4.5f * scale))
        drawRect(color = linePaintColor, topLeft = Offset(60f * scale, 48f * scale), size = Size(12f * scale, 4.5f * scale))

        // Dashed divider line
        drawLine(
            color = linePaintColor,
            start = Offset(30f * scale, 57f * scale),
            end = Offset(70f * scale, 57f * scale),
            strokeWidth = 2f * scale,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
        )

        // Bottom summary chips
        drawRoundRect(color = Color(0xFFF2B632), topLeft = Offset(30f * scale, 62f * scale), size = Size(16f * scale, 6f * scale), cornerRadius = CornerRadius(3f * scale))
        drawRoundRect(color = Color(0xFFF2B632), topLeft = Offset(52f * scale, 62f * scale), size = Size(20f * scale, 6f * scale), cornerRadius = CornerRadius(3f * scale))

        // Stamp badge circle
        drawCircle(color = Color(0xFFF2B632), radius = 11f * scale, center = Offset(71f * scale, 78f * scale))
        drawCircle(color = Color(0xFF3DD598), radius = 11f * scale, center = Offset(71f * scale, 78f * scale), style = Stroke(width = 3f * scale))

        // Checkmark
        val checkPath = Path().apply {
            moveTo(65f * scale, 78f * scale)
            lineTo(69f * scale, 82f * scale)
            lineTo(77f * scale, 73f * scale)
        }
        drawPath(
            path = checkPath,
            color = Color(0xFF1A1405),
            style = Stroke(
                width = 3.5f * scale,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    }
}
