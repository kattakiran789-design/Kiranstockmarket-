package com.telugustockpro.ui.screens.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.telugustockpro.ui.theme.TradingViewColors

@Composable
fun SplashScreen(
    onSplashComplete: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "splash")

    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(2000)
        onSplashComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        TradingViewColors.Background,
                        TradingViewColors.Surface
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Animated Logo
            Icon(
                imageVector = Icons.Filled.ShowChart,
                contentDescription = "Logo",
                tint = TradingViewColors.Blue,
                modifier = Modifier
                    .size(100.dp)
                    .scale(scale)
                    .alpha(alpha)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // App Name
            Text(
                text = "Telugu Stock Pro",
                style = MaterialTheme.typography.headlineLarge,
                color = TradingViewColors.TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Tagline
            Text(
                text = "Smart Trading Analysis",
                style = MaterialTheme.typography.bodyLarge,
                color = TradingViewColors.TextSecondary
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Loading indicator
            CircularProgressIndicator(
                color = TradingViewColors.Blue,
                modifier = Modifier.size(32.dp),
                strokeWidth = 3.dp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Loading market data...",
                style = MaterialTheme.typography.bodySmall,
                color = TradingViewColors.TextTertiary
            )
        }

        // Version at bottom
        Text(
            text = "v1.0.0",
            style = MaterialTheme.typography.labelSmall,
            color = TradingViewColors.TextTertiary,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )
    }
}
