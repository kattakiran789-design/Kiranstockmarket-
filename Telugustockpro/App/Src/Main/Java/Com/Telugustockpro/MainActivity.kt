package com.telugustockpro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.telugustockpro.ui.TeluguStockMainApp
import com.telugustockpro.ui.theme.TeluguStockProTheme
import com.telugustockpro.ui.theme.TradingViewColors
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            TeluguStockProTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = TradingViewColors.Background
                ) {
                    TeluguStockMainApp()
                }
            }
        }
    }
}
