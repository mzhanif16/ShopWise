package com.shopwise.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [ScanHistory::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun scanHistoryDao(): ScanHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE ScanHistory ADD COLUMN imageUri TEXT")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "shopwise_database"
                )
                .addMigrations(MIGRATION_1_2)
                .fallbackToDestructiveMigration() // Jika migrasi gagal, hapus db lama (aman untuk dev)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
