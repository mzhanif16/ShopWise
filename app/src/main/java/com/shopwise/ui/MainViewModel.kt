package com.shopwise.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.shopwise.core.UserPreferences
import com.shopwise.ui.navigation.Routes

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val userPreferences = UserPreferences(application)

    fun getStartDestination(): String {
        val language = userPreferences.getLanguage()
        if (language == null) {
            return Routes.LANGUAGE_SELECTION
        }
        
        return if (userPreferences.isOnboarded()) {
            Routes.DASHBOARD
        } else {
            Routes.ONBOARDING
        }
    }
}
