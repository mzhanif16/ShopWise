package com.shopwise.ui.screen.scan

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.shopwise.core.database.AppDatabase
import com.shopwise.core.database.ScanHistory
import kotlinx.coroutines.launch

class DetailResultViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).scanHistoryDao()
    
    var scanResult by mutableStateOf<ScanHistory?>(null)
        private set
    
    var isLoading by mutableStateOf(true)
        private set

    fun loadScanDetail(id: Int) {
        viewModelScope.launch {
            isLoading = true
            scanResult = dao.getScanById(id)
            isLoading = false
        }
    }
}
