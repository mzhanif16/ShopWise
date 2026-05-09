package com.shopwise.ui.screen.language

import android.app.Activity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.shopwise.core.UserPreferences
import com.shopwise.ui.navigation.Routes
import com.shopwise.ui.theme.PrimaryColor
import java.util.Locale

@Composable
fun LanguageSelectionScreen(navController: NavController) {
    val context = LocalContext.current
    val userPreferences = remember { UserPreferences(context) }
    var selectedLanguage by remember { mutableStateOf(userPreferences.getLanguage() ?: "") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Select Language",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Text(
            text = "Pilih Bahasa",
            fontSize = 18.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(48.dp))

        LanguageOptionCard(
            title = "English",
            isSelected = selectedLanguage == "en",
            onClick = { selectedLanguage = "en" }
        )

        Spacer(modifier = Modifier.height(16.dp))

        LanguageOptionCard(
            title = "Bahasa Indonesia",
            isSelected = selectedLanguage == "in",
            onClick = { selectedLanguage = "in" }
        )

        Spacer(modifier = Modifier.height(64.dp))

        Button(
            onClick = {
                if (selectedLanguage.isNotEmpty()) {
                    userPreferences.setLanguage(selectedLanguage)
                    
                    // Terapkan locale ke Activity DAN Application Context
                    updateLocale(context as Activity, selectedLanguage)
                    
                    navController.navigate(Routes.ONBOARDING) {
                        popUpTo(Routes.LANGUAGE_SELECTION) { inclusive = true }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = selectedLanguage.isNotEmpty(),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Continue / Lanjutkan",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

private fun updateLocale(activity: Activity, languageCode: String) {
    val locale = Locale(languageCode)
    Locale.setDefault(locale)
    
    val resources = activity.resources
    val config = resources.configuration
    config.setLocale(locale)
    
    // Perbarui Activity Resources
    resources.updateConfiguration(config, resources.displayMetrics)
    
    // PENTING: Perbarui juga Application Context agar ViewModel mendapatkan bahasa yang benar
    activity.applicationContext.resources.updateConfiguration(config, resources.displayMetrics)
}

@Composable
fun LanguageOptionCard(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) PrimaryColor else Color.LightGray
        ),
        color = if (isSelected) PrimaryColor.copy(alpha = 0.05f) else Color.White
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) PrimaryColor else Color.Black
            )
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(selectedColor = PrimaryColor)
            )
        }
    }
}
