package com.shopwise.ui.screen.scan

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.shopwise.core.GemmaUtils
import com.shopwise.core.UserPreferences
import com.shopwise.core.database.AppDatabase
import com.shopwise.core.database.ScanHistory
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AnalysisResultViewModel(application: Application) : AndroidViewModel(application) {
    private val context = application.applicationContext
    private val userPreferences = UserPreferences(context)
    private val gemmaManager = GemmaUtils.gemmaManager
    private val db = AppDatabase.getDatabase(context)

    private val _analysisResult = MutableStateFlow("")
    val analysisResult: StateFlow<String> = _analysisResult.asStateFlow()

    private val _isAnalyzing = MutableStateFlow(true)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

    fun analyzePicture(bitmap: Bitmap? = null, imageUri: String? = null, onComplete: (String) -> Unit = {}) {
        if (gemmaManager == null || bitmap == null) return

        val userData = userPreferences.getUserData()
        val allergies = (userData["allergies"] as? Set<*>)?.joinToString(", ") ?: "Tidak ada"
        
        val prompt = """
            Anda adalah "ShopWise", asisten AI spesialis nutrisi, toksikologi makanan, dan penjaga profil medis pengguna. Tugas utama Anda adalah membaca, menganalisis, dan mengevaluasi daftar komposisi makanan dari gambar atau teks yang diberikan.
            
            PENTING: Pengguna Anda memiliki kondisi medis yang sangat sensitif dengan alergi: $allergies. 
            Kesalahan dalam mengidentifikasi bahan makanan dapat berakibat fatal. Banyak produsen makanan menyembunyikan alergen atau bahan berbahaya di balik nama ilmiah, kode E (E-numbers), atau istilah payung seperti "perisa alami".
            
            Analisis gambar label makanan ini dan berikan laporan apakah aman atau tidak berdasarkan profil alergi tersebut, jelaskan dengan detail. 
            Berikan nama produk singkat di baris PERTAMA.
            di akhir reasoning tambahkan kata KESIMPULAN: AMAN / TIDAK AMAN agar saya dapat valuenya.
        """.trimIndent()

        viewModelScope.launch {
            _isAnalyzing.value = true
            _analysisResult.value = ""
            
            // Collect results from gemmaManager
            val job = launch {
                gemmaManager.partialResults.collect { (text, isDone) ->
                    if (text.isNotEmpty()) {
                        _analysisResult.value += text
                    }
                    if (isDone) {
                        _isAnalyzing.value = false
                        val finalResult = _analysisResult.value
                        
                        // Extract Info and Save to Database
                        saveToHistory(finalResult, imageUri)
                        
                        onComplete(finalResult)
                        this@launch.cancel()
                    }
                }
            }

            gemmaManager.generateResponse(prompt, bitmap)
        }
    }

    private fun saveToHistory(result: String, imageUri: String?) {
        val isSafe = result.contains("AMAN", ignoreCase = false) && !result.contains("TIDAK AMAN", ignoreCase = false)
        
        // Extract product name: ambil baris pertama atau 5 kata pertama
        val firstLine = result.split("\n").firstOrNull { it.isNotBlank() } ?: "Unknown Product"
        val productName = if (firstLine.length > 30) firstLine.take(27) + "..." else firstLine

        viewModelScope.launch {
            db.scanHistoryDao().insertScan(
                ScanHistory(
                    productName = productName,
                    finalResult = result,
                    isSafe = isSafe,
                    imageUri = imageUri
                )
            )
        }
    }
}
