package com.shopwise.core

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class UserPreferences(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_FULL_NAME = "full_name"
        private const val KEY_BIRTH_DATE = "birth_date"
        private const val KEY_GENDER = "gender"
        private const val KEY_HEIGHT = "height"
        private const val KEY_WEIGHT = "weight"
        private const val KEY_ALLERGIES = "allergies"
        private const val KEY_SELECTED_MODEL = "selected_model"
        private const val KEY_IS_ONBOARDED = "is_onboarded"
        private const val KEY_LANGUAGE = "language"
        private const val KEY_BACKEND = "backend"
    }

    fun saveUserData(
        fullName: String,
        birthDate: String,
        gender: String,
        height: String,
        weight: String,
        allergies: Set<String>,
        selectedModel: String
    ) {
        sharedPreferences.edit {
            putString(KEY_FULL_NAME, fullName)
            putString(KEY_BIRTH_DATE, birthDate)
            putString(KEY_GENDER, gender)
            putString(KEY_HEIGHT, height)
            putString(KEY_WEIGHT, weight)
            putStringSet(KEY_ALLERGIES, allergies)
            putString(KEY_SELECTED_MODEL, selectedModel)
        }
    }

    fun setOnboarded(value: Boolean) {
        sharedPreferences.edit { putBoolean(KEY_IS_ONBOARDED, value) }
    }

    fun setLanguage(language: String) {
        sharedPreferences.edit { putString(KEY_LANGUAGE, language) }
    }

    fun getLanguage(): String? = sharedPreferences.getString(KEY_LANGUAGE, null)

    fun setBackend(backend: String) {
        sharedPreferences.edit { putString(KEY_BACKEND, backend) }
    }

    fun getBackend(): String = sharedPreferences.getString(KEY_BACKEND, "CPU") ?: "CPU"

    fun getUserData(): Map<String, Any?> {
        return mapOf(
            "fullName" to sharedPreferences.getString(KEY_FULL_NAME, ""),
            "birthDate" to sharedPreferences.getString(KEY_BIRTH_DATE, ""),
            "gender" to sharedPreferences.getString(KEY_GENDER, ""),
            "height" to sharedPreferences.getString(KEY_HEIGHT, ""),
            "weight" to sharedPreferences.getString(KEY_WEIGHT, ""),
            "allergies" to sharedPreferences.getStringSet(KEY_ALLERGIES, emptySet()),
            "selectedModel" to sharedPreferences.getString(KEY_SELECTED_MODEL, "4B")
        )
    }
    
    fun isOnboarded(): Boolean = sharedPreferences.getBoolean(KEY_IS_ONBOARDED, false)
}
