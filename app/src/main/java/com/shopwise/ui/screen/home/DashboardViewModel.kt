package com.shopwise.ui.screen.home

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.shopwise.core.GemmaManager
import com.shopwise.core.GemmaUtils
import com.shopwise.core.UserPreferences
import com.shopwise.ui.screen.onboarding.ModelState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class DashboardViewModel(application: Application) : AndroidViewModel(application) {
    private val context = application.applicationContext
    private val userPreferences = UserPreferences(context)
    val gemmaManager = GemmaManager()

    var isModelInitializing by mutableStateOf(false)
    var isModelReady by mutableStateOf(false)
    var initializationMessage by mutableStateOf("")

    fun initSelectedModel() {
        val userData = userPreferences.getUserData()
        val selectedModelId = userData["selectedModel"] as? String ?: "4B"
        
        // Map model ID ke file name (seperti di GemmaViewModel)
        val fileName = if (selectedModelId == "4B") "gemma-4b.litertlm" else "gemma-2b.litertlm"
        val modelFile = File(context.filesDir, fileName)

        if (modelFile.exists()) {
            isModelInitializing = true
            isModelReady = false
            initializationMessage = "Initializing $selectedModelId model..."

            viewModelScope.launch(Dispatchers.IO) {
                try {
                    gemmaManager.initialize(modelFile.absolutePath)
                    withContext(Dispatchers.Main) {
                        GemmaUtils.gemmaManager = gemmaManager
                        isModelInitializing = false
                        isModelReady = true
                        initializationMessage = "$selectedModelId model is ready"
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        isModelInitializing = false
                        initializationMessage = "Failed to load model"
                    }
                }
            }
        } else {
            initializationMessage = "Model file not found"
        }
    }

    override fun onCleared() {
        super.onCleared()
        gemmaManager.close()
    }
}
