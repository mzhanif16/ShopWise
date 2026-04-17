package com.shopwise

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.shopwise.ui.navigation.AppNavHost
import com.shopwise.ui.theme.ShopWiseTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ShopWiseTheme {
                AppNavHost()
            }
        }
    }
}