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
import kotlinx.coroutines.NonCancellable
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

    // Approximate target sizes
    private val MODEL_SIZES = mapOf(
        "4B" to 3_842_678_656L,
        "2B" to 2_674_530_688L
    )

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
            val fileSize = if (file.exists()) file.length() else 0L
            val expectedSize = MODEL_SIZES[id] ?: 1_000_000_000L
            
            // Simpler check as requested: if file exists and is reasonably large (at least 90% of expected)
            val isComplete = file.exists() && fileSize > (expectedSize * 0.9)
            
            models[id] = state.copy(
                isDownloaded = isComplete,
                downloadProgress = if (isComplete) 1f 
                    else if (expectedSize > 0) (fileSize.toFloat() / expectedSize).coerceIn(0f, 0.99f) 
                    else 0f,
                downloadedBytesDisplay = if (file.exists()) "${fileSize / 1024 / 1024} MB" else ""
            )
        }
    }

    fun downloadModel(modelId: String) {
        val model = models[modelId] ?: return
        if (model.isDownloading || model.isDownloaded) return
        
        cancelActiveDownload()
        activeDownloadId = modelId
        
        val modelFile = File(context.filesDir, model.fileName)
        
        downloadJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                val existingLength = if (modelFile.exists()) modelFile.length() else 0L
                val expectedTotal = MODEL_SIZES[modelId] ?: 0L
                
                withContext(Dispatchers.Main) {
                    models[modelId] = models[modelId]?.copy(
                        isDownloading = true,
                        downloadProgress = if (expectedTotal > 0) existingLength.toFloat() / expectedTotal else 0f
                    ) ?: return@withContext
                }

                var currentUrl = model.url
                var connection: HttpURLConnection? = null
                var redirectCount = 0
                
                while (redirectCount < 10 && isActive) {
                    val url = URL(currentUrl)
                    connection = url.openConnection() as HttpURLConnection
                    connection.instanceFollowRedirects = false
                    connection.setRequestProperty("User-Agent", "Mozilla/5.0")
                    connection.connectTimeout = 20000
                    connection.readTimeout = 20000
                    
                    val responseCode = connection.responseCode
                    if (responseCode in 300..308) {
                        currentUrl = connection.getHeaderField("Location")
                        redirectCount++
                        connection.disconnect()
                    } else break
                }
                
                if (connection == null || !isActive) return@launch

                connection.disconnect()
                connection = URL(currentUrl).openConnection() as HttpURLConnection
                connection.setRequestProperty("User-Agent", "Mozilla/5.0")
                if (existingLength > 0) {
                    connection.setRequestProperty("Range", "bytes=$existingLength-")
                }
                
                val responseCode = connection.responseCode
                if (responseCode == 416) {
                    withContext(Dispatchers.Main) { checkModelsStatus() }
                    return@launch
                }

                val isResuming = responseCode == HttpURLConnection.HTTP_PARTIAL
                val startByte = if (isResuming) existingLength else 0L
                val contentLength = connection.contentLengthLong
                val totalToDownload = if (isResuming && contentLength > 0) contentLength + existingLength else {
                    if (contentLength > 0) contentLength else expectedTotal
                }

                val inputStream = connection.inputStream
                val outputStream = FileOutputStream(modelFile, isResuming)
                
                val data = ByteArray(128 * 1024)
                var totalDownloaded = startByte
                var lastTotalForSpeed = startByte
                var count: Int = 0
                var lastUpdate = System.currentTimeMillis()
                
                while (isActive && inputStream.read(data).also { count = it } != -1) {
                    totalDownloaded += count
                    outputStream.write(data, 0, count)
                    
                    val now = System.currentTimeMillis()
                    if (now - lastUpdate > 800) {
                        val timeDiffSeconds = (now - lastUpdate) / 1000f
                        val speedMbps = if (timeDiffSeconds > 0) 
                            ((totalDownloaded - lastTotalForSpeed) / timeDiffSeconds) / (1024 * 1024) 
                            else 0f
                        
                        val progress = if (totalToDownload > 0) totalDownloaded.toFloat() / totalToDownload else 0f
                        
                        withContext(Dispatchers.Main) {
                            models[modelId]?.let { 
                                models[modelId] = it.copy(
                                    downloadSpeed = String.format(Locale.US, "%.2f MB/s", speedMbps),
                                    downloadProgress = progress,
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
                connection.disconnect()
                
                val isActuallyCompleted = isActive && (totalDownloaded >= (totalToDownload * 0.98))
                
                withContext(NonCancellable) {
                    withContext(Dispatchers.Main) {
                        if (isActuallyCompleted) {
                            models[modelId] = models[modelId]?.copy(
                                isDownloading = false,
                                isDownloaded = true,
                                downloadProgress = 1f,
                                downloadSpeed = ""
                            )!!
                        } else {
                            models[modelId] = models[modelId]?.copy(
                                isDownloading = false,
                                downloadSpeed = ""
                            )!!
                        }
                        if (activeDownloadId == modelId) activeDownloadId = null
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Download error", e)
                withContext(NonCancellable) {
                    withContext(Dispatchers.Main) {
                        models[modelId]?.let {
                            models[modelId] = it.copy(
                                isDownloading = false,
                                downloadSpeed = "Stopped: ${e.message?.take(20)}"
                            )
                        }
                        if (activeDownloadId == modelId) activeDownloadId = null
                    }
                }
            }
        }
    }

    fun cancelActiveDownload() {
        downloadJob?.cancel()
        activeDownloadId?.let { id ->
            models[id]?.let {
                models[id] = it.copy(isDownloading = false, downloadSpeed = "")
            }
        }
        activeDownloadId = null
    }
}
