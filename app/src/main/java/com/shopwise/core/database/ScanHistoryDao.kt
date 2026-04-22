package com.shopwise.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ScanHistoryDao {
    @Insert
    suspend fun insertScan(scan: ScanHistory)

    @Query("SELECT * FROM ScanHistory ORDER BY timestamp DESC")
    fun getAllScans(): Flow<List<ScanHistory>>

    @Query("DELETE FROM ScanHistory")
    suspend fun clearHistory()
}
