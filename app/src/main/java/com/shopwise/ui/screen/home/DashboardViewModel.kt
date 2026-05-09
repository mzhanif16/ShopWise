package com.shopwise.ui.screen.home

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.shopwise.R
import com.shopwise.core.GemmaManager
import com.shopwise.core.GemmaUtils
import com.shopwise.core.UserPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class DashboardViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "DashboardViewModel"
    private val context = application.applicationContext
    private val userPreferences = UserPreferences(context)
    val gemmaManager = GemmaManager()

    var isModelInitializing by mutableStateOf(false)
    var isModelReady by mutableStateOf(false)
    var initializationMessage by mutableStateOf("")
    var errorMessage by mutableStateOf("")

    var selectedModel by mutableStateOf("4B")
        private set
    
    var currentBackend by mutableStateOf(userPreferences.getBackend())
        private set

    // Minimum size requirements to consider a model file valid for initialization
    private val MIN_SIZE_4B = 3_000_000_000L // 3GB
    private val MIN_SIZE_2B = 2_000_000_000L // 2GB

    fun initSelectedModel() {
        val userData = userPreferences.getUserData()
        val modelId = userData["selectedModel"] as? String ?: "4B"
        selectedModel = modelId
        currentBackend = userPreferences.getBackend()
        
        val fileName = if (selectedModel == "4B") "gemma-4b.litertlm" else "gemma-2b.litertlm"
        val modelFile = File(context.filesDir, fileName)
        val minRequiredSize = if (selectedModel == "4B") MIN_SIZE_4B else MIN_SIZE_2B

        if (modelFile.exists() && modelFile.length() >= minRequiredSize) {
            if (isModelReady) return // Already loaded

            isModelInitializing = true
            isModelReady = false
            initializationMessage = context.getString(R.string.initializing)

            viewModelScope.launch(Dispatchers.IO) {
                try {
                    val useGpu = currentBackend == "GPU"
                    gemmaManager.initialize(modelFile.absolutePath, useGpu)
                    withContext(Dispatchers.Main) {
                        GemmaUtils.gemmaManager = gemmaManager
                        isModelInitializing = false
                        isModelReady = true
                        initializationMessage = context.getString(R.string.brain_active)
                        Log.d(TAG, "Model initialized successfully: $modelId with $currentBackend")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to initialize model: $modelId", e)
                    withContext(Dispatchers.Main) {
                        isModelInitializing = false
                        isModelReady = false
                        errorMessage = e.message ?: "Unknown Error"
                        initializationMessage = context.getString(R.string.failed_load_model)
                    }
                }
            }
        } else {
            isModelReady = false
            isModelInitializing = false
            initializationMessage = if (!modelFile.exists()) 
                context.getString(R.string.not_downloaded) 
            else 
                context.getString(R.string.waiting_download)
            Log.w(TAG, "Model file not ready for $modelId: exists=${modelFile.exists()}, size=${modelFile.length()}")
        }
    }
    
    fun updateSelectedModel(modelId: String) {
        if (selectedModel == modelId && isModelReady) return
        
        val userData = userPreferences.getUserData()
        userPreferences.saveUserData(
            fullName = userData["fullName"] as? String ?: "",
            birthDate = userData["birthDate"] as? String ?: "",
            gender = userData["gender"] as? String ?: "",
            height = userData["height"] as? String ?: "",
            weight = userData["weight"] as? String ?: "",
            allergies = userData["allergies"] as? Set<String> ?: emptySet(),
            selectedModel = modelId
        )
        
        // Close current model before switching
        gemmaManager.close()
        isModelReady = false
        
        initSelectedModel()
    }
    
    fun updateBackend(backend: String) {
        if (currentBackend == backend) return
        
        userPreferences.setBackend(backend)
        currentBackend = backend
        
        // Restart model with new backend
        gemmaManager.close()
        isModelReady = false
        initSelectedModel()
    }

    override fun onCleared() {
        super.onCleared()
        gemmaManager.close()
    }
}
