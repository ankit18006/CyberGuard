package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ThreatDao {
    @Query("SELECT * FROM threat_alerts ORDER BY timestamp DESC")
    fun getAllThreats(): Flow<List<ThreatAlert>>

    @Query("SELECT * FROM threat_alerts WHERE id = :id")
    suspend fun getThreatById(id: Int): ThreatAlert?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertThreat(threat: ThreatAlert): Long

    @Query("DELETE FROM threat_alerts WHERE id = :id")
    suspend fun deleteThreatById(id: Int)

    @Query("DELETE FROM threat_alerts")
    suspend fun clearAllThreats()
}
