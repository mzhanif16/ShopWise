package com.shopwise.ui.screen.onboarding

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

data class ModelState(
    val id: String,
    val name: String,
    val url: String,
    val fileName: String,
    val isDownloaded: Boolean = false,
    val isDownloading: Boolean = false,
    val downloadProgress: Float = 0f,
    val downloadedBytesDisplay: String = "",
    val downloadSpeed: String = ""
)

class GemmaViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "GemmaViewModel"
    private val context = application.applicationContext
    
    val models = mutableStateMapOf<String, ModelState>()
    var activeDownloadId by mutableStateOf<String?>(null)
    
    private var downloadJob: Job? = null

    init {
        models["4B"] = ModelState(
            id = "4B",
            name = "Gemma 4B",
            url = "https://huggingface.co/litert-community/gemma-4-E4B-it-litert-lm/resolve/main/gemma-4-E4B-it.litertlm",
            fileName = "gemma-4b.litertlm"
        )
        models["2B"] = ModelState(
            id = "2B",
            name = "Gemma 2B",
            url = "https://huggingface.co/litert-community/gemma-4-E2B-it-litert-lm/resolve/main/gemma-4-E2B-it.litertlm",
            fileName = "gemma-2b.litertlm"
        )
        checkModelsStatus()
    }

    fun checkModelsStatus() {
        models.forEach { (id, state) ->
            val file = File(context.filesDir, state.fileName)
            // Consider it downloaded if it's large enough (simple check)
            val exists = file.exists() && file.length() > 500_000_000 
            models[id] = state.copy(isDownloaded = exists)
        }
    }

    fun downloadModel(modelId: String) {
        val model = models[modelId] ?: return
        if (model.isDownloading || model.isDownloaded) return
        
        cancelActiveDownload()
        
        activeDownloadId = modelId
        models[modelId] = model.copy(isDownloading = true, downloadProgress = 0f)
        
        val modelFile = File(context.filesDir, model.fileName)
        
        downloadJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                var currentUrl = model.url
                var connection: HttpURLConnection
                var responseCode: Int
                var redirectCount = 0
                
                while (redirectCount < 5 && isActive) {
                    val url = URL(currentUrl)
                    connection = url.openConnection() as HttpURLConnection
                    connection.instanceFollowRedirects = false
                    connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Android)")
                    responseCode = connection.responseCode
                    if (responseCode in 300..308) {
                        currentUrl = connection.getHeaderField("Location")
                        redirectCount++
                        connection.disconnect()
                    } else break
                }
                
                if (!isActive) return@launch
                
                connection = URL(currentUrl).openConnection() as HttpURLConnection
                connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Android)")
                
                responseCode = connection.responseCode
                val inputStream = connection.inputStream
                val outputStream = FileOutputStream(modelFile)
                val totalToDownload = connection.contentLengthLong
                
                val data = ByteArray(65536)
                var totalDownloaded = 0L
                var lastTotalForSpeed = 0L
                var count: Int = 0
                var lastUpdate = System.currentTimeMillis()
                
                while (isActive && inputStream.read(data).also { count = it } != -1) {
                    totalDownloaded += count
                    outputStream.write(data, 0, count)
                    
                    val now = System.currentTimeMillis()
                    if (now - lastUpdate > 800) {
                        val timeDiffSeconds = (now - lastUpdate) / 1000f
                        val speedMbps = ((totalDownloaded - lastTotalForSpeed) / timeDiffSeconds) / (1024 * 1024)
                        
                        withContext(Dispatchers.Main) {
                            models[modelId]?.let { 
                                models[modelId] = it.copy(
                                    downloadSpeed = String.format(Locale.US, "%.2f MB/s", speedMbps),
                                    downloadProgress = if (totalToDownload > 0) totalDownloaded.toFloat() / totalToDownload else 0f,
                                    downloadedBytesDisplay = "${totalDownloaded / 1024 / 1024} MB / ${totalToDownload / 1024 / 1024} MB"
                                )
                            }
                        }
                        lastUpdate = now
                        lastTotalForSpeed = totalDownloaded
                    }
                }
                
                outputStream.flush()
                outputStream.close()
                inputStream.close()
                
                withContext(Dispatchers.Main) {
                    models[modelId]?.let {
                        models[modelId] = it.copy(
                            isDownloading = false,
                            isDownloaded = true,
                            downloadProgress = 1f
                        )
                    }
                    if (activeDownloadId == modelId) activeDownloadId = null
                }
            } catch (e: Exception) {
                Log.e(TAG, "Download failed", e)
                withContext(Dispatchers.Main) {
                    models[modelId]?.let {
                        models[modelId] = it.copy(isDownloading = false)
                    }
                    if (activeDownloadId == modelId) activeDownloadId = null
                }
            }
        }
    }

    fun cancelActiveDownload() {
        downloadJob?.cancel()
        activeDownloadId?.let { id ->
            models[id]?.let {
                models[id] = it.copy(isDownloading = false)
            }
        }
        activeDownloadId = null
    }
}
