package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "threat_alerts")
data class ThreatAlert(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val appName: String,
    val packageName: String,
    val title: String,
    val messageSnippet: String,
    val fraudType: String,
    val severity: String,
    val timestamp: Long = System.currentTimeMillis(),
    val stepsToTake: String
)
