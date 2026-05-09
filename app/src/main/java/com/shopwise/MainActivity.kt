package com.shopwise

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.shopwise.ui.navigation.AppNavHost
import com.shopwise.ui.theme.ShopWiseTheme
import com.shopwise.core.UserPreferences
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Terapkan bahasa yang dipilih sebelum menampilkan UI
        val userPreferences = UserPreferences(this)
        userPreferences.getLanguage()?.let { lang ->
            setAppLocale(lang)
        }
        
        enableEdgeToEdge()
        setContent {
            ShopWiseTheme {
                AppNavHost()
            }
        }
    }

    private fun setAppLocale(language: String) {
        val locale = Locale(language)
        Locale.setDefault(locale)
        
        val resources = resources
        val config = resources.configuration
        config.setLocale(locale)
        
        // Perbarui Activity Resources
        resources.updateConfiguration(config, resources.displayMetrics)
        
        // PENTING: Perbarui juga Application Context agar ViewModel mendapatkan bahasa yang benar
        applicationContext.resources.updateConfiguration(config, resources.displayMetrics)
    }
}