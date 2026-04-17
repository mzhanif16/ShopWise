package com.shopwise.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.shopwise.core.UserPreferences
import com.shopwise.ui.navigation.Routes

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val userPreferences = UserPreferences(application)

    fun getStartDestination(): String {
        return if (userPreferences.isOnboarded()) {
            Routes.DASHBOARD
        } else {
            Routes.ONBOARDING
        }
    }
}
