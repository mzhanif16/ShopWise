package com.shopwise.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ScanHistory")
data class ScanHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val productName: String,
    val finalResult: String,
    val isSafe: Boolean,
    val imageUri: String?,
    val timestamp: Long = System.currentTimeMillis()
)
