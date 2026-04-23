package com.shopwise.ui.screen.scan

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.shopwise.core.GemmaUtils
import com.shopwise.core.UserPreferences
import com.shopwise.core.database.AppDatabase
import com.shopwise.core.database.ScanHistory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class AnalysisResultViewModel(application: Application) : AndroidViewModel(application) {
    private val context = application.applicationContext
    private val userPreferences = UserPreferences(context)
    private val gemmaManager = GemmaUtils.gemmaManager
    private val db = AppDatabase.getDatabase(context)

    private val _analysisResult = MutableStateFlow("")
    val analysisResult: StateFlow<String> = _analysisResult.asStateFlow()

    private val _isAnalyzing = MutableStateFlow(true)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

    fun analyzePicture(bitmap: List<Bitmap>? = null, imageUri: String? = null, onComplete: (String) -> Unit = {}) {
        if (gemmaManager == null || bitmap == null) return

        val userData = userPreferences.getUserData()
        val allergies = (userData["allergies"] as? Set<*>)?.joinToString(", ") ?: "Tidak ada"
        
        val prompt = """
            Anda adalah "ShopWise", asisten AI spesialis nutrisi, toksikologi makanan, dan penjaga profil medis pengguna. Tugas utama Anda adalah membaca, menganalisis, dan mengevaluasi daftar komposisi makanan dari gambar atau teks yang diberikan.
            
            PENTING: Pengguna Anda memiliki kondisi medis yang sangat sensitif dengan alergi: $allergies. 
            Kesalahan dalam mengidentifikasi bahan makanan dapat berakibat fatal. Banyak produsen makanan menyembunyikan alergen atau bahan berbahaya di balik nama ilmiah, kode E (E-numbers), atau istilah payung seperti "perisa alami".
            
            Analisis gambar label makanan ini dan berikan laporan apakah aman atau tidak berdasarkan profil alergi tersebut, jelaskan dengan detail. 
            Berikan nama produk singkat di baris PERTAMA berikan dengan kata Produk: (nama produk, jika tidak ada berikan saja nama yang tepat menurutmu).
            Setelah nama produk, baru berikan kata Analisis: (hasil analisis jelaskan secara detail terkait resiko, manfaat, efek samping dll).
            di akhir reasoning tambahkan kata KESIMPULAN: AMAN / TIDAK AMAN agar saya dapat valuenya.
        """.trimIndent()

        viewModelScope.launch {
            _isAnalyzing.value = true
            _analysisResult.value = ""
            
            // Collect results from gemmaManager
            launch {
                gemmaManager.partialResults.collect { (text, isDone) ->
                    if (text.isNotEmpty()) {
                        _analysisResult.value += text
                    }
                    if (isDone) {
                        _isAnalyzing.value = false
                        val finalResult = _analysisResult.value
                        
                        // Extract Info and Save to Database (Including saving bitmap to local file)
                        saveToHistory(finalResult, bitmap)
                        
                        onComplete(finalResult)
                        this@launch.cancel()
                    }
                }
            }

            gemmaManager.generateResponse(prompt, bitmap)
        }
    }

    private suspend fun saveBitmapToFile(bitmap: Bitmap): String? = withContext(Dispatchers.IO) {
        try {
            val fileName = "scan_${UUID.randomUUID()}.jpg"
            val file = File(context.filesDir, fileName)
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun saveToHistory(result: String, bitmap: List<Bitmap>?) {
        val isSafe = result.contains("AMAN", ignoreCase = false) && !result.contains("TIDAK AMAN", ignoreCase = false)
        
        // Logika Ekstraksi Nama Produk: antara "Produk:" dan "Analisis"
        val startTag = "Produk:"
        val endTag = "Analisis"
        
        val startIndex = result.indexOf(startTag)
        val endIndex = result.indexOf(endTag)

        var productName = if (startIndex != -1 && endIndex != -1 && endIndex > startIndex + startTag.length) {
            result.substring(startIndex + startTag.length, endIndex).trim()
        } else {
            // Fallback: Ambil baris pertama yang mengandung "Produk:"
            result.split("\n").firstOrNull { it.contains(startTag) }
                ?.replace(startTag, "")?.trim() 
                ?: "Unknown Product"
        }
        
        // Bersihkan karakter markdown jika ada (seperti **)
        productName = productName.replace("*", "").trim()
        
        // Batasi panjang nama produk agar tidak kepanjangan di UI
        val finalProductName = if (productName.length > 35) productName.take(32) + "..." else productName

        viewModelScope.launch {
            val localPath = bitmap?.first()?.let { saveBitmapToFile(it) }
            db.scanHistoryDao().insertScan(
                ScanHistory(
                    productName = finalProductName,
                    finalResult = result,
                    isSafe = isSafe,
                    imageUri = localPath
                )
            )
        }
    }
}
