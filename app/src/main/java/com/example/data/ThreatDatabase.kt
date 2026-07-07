package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ThreatAlert::class], version = 1, exportSchema = false)
abstract class ThreatDatabase : RoomDatabase() {
    abstract fun threatDao(): ThreatDao

    companion object {
        @Volatile
        private var INSTANCE: ThreatDatabase? = null

        fun getDatabase(context: Context): ThreatDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ThreatDatabase::class.java,
                    "cyberguard_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
