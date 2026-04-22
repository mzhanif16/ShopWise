package com.shopwise.ui.screen.scan

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.shopwise.core.GemmaUtils
import com.shopwise.core.UserPreferences
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AnalysisResultViewModel(application: Application) : AndroidViewModel(application) {
    private val context = application.applicationContext
    private val userPreferences = UserPreferences(context)
    private val gemmaManager = GemmaUtils.gemmaManager

    private val _analysisResult = MutableStateFlow("")
    val analysisResult: StateFlow<String> = _analysisResult.asStateFlow()

    private val _isAnalyzing = MutableStateFlow(true)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

    fun analyzePicture(bitmap: Bitmap? = null, onComplete: (String) -> Unit = {}) {
        if (gemmaManager == null || bitmap == null) return

        val userData = userPreferences.getUserData()
        val allergies = (userData["allergies"] as? Set<*>)?.joinToString(", ") ?: "Tidak ada"
        
        val prompt = """
            Anda adalah "ShopWise", asisten AI spesialis nutrisi, toksikologi makanan, dan penjaga profil medis pengguna. Tugas utama Anda adalah membaca, menganalisis, dan mengevaluasi daftar komposisi makanan dari gambar atau teks yang diberikan.
            
            PENTING: Pengguna Anda memiliki kondisi medis yang sangat sensitif dengan alergi: $allergies. 
            Kesalahan dalam mengidentifikasi bahan makanan dapat berakibat fatal. Banyak produsen makanan menyembunyikan alergen atau bahan berbahaya di balik nama ilmiah, kode E (E-numbers), atau istilah payung seperti "perisa alami".
            
            Analisis gambar label makanan ini dan berikan laporan apakah aman atau tidak berdasarkan profil alergi tersebut. di akhir reasoning tambahkan kata KESIMPULAN: AMAN / TIDAK AMAN agar saya dapat valuenya.
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
                        onComplete(_analysisResult.value)

                        this@launch.cancel()
                    }
                }
            }

            gemmaManager.generateResponse(prompt, bitmap)
        }
    }
}
