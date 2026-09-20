package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TrainingSessionDao {
    @Query("SELECT * FROM training_sessions ORDER BY timestamp DESC")
    fun getAllSessions(): Flow<List<TrainingSessionEntity>>

    @Query("SELECT * FROM training_sessions WHERE drillType = :drillType ORDER BY timestamp DESC")
    fun getSessionsByType(drillType: String): Flow<List<TrainingSessionEntity>>

    @Query("SELECT MAX(score) FROM training_sessions WHERE drillType = :drillType")
    fun getBestScore(drillType: String): Flow<Int?>

    @Query("SELECT MAX(score) FROM training_sessions")
    fun getOverallBestScore(): Flow<Int?>

    @Query("SELECT COUNT(*) FROM training_sessions")
    fun getTotalSessionsCount(): Flow<Int>

    @Query("SELECT AVG(accuracy) FROM training_sessions")
    fun getAverageAccuracy(): Flow<Float?>

    @Query("SELECT SUM(headshots) * 100.0 / NULLIF(SUM(hits), 0) FROM training_sessions")
    fun getOverallHeadshotRate(): Flow<Float?>

    @Query("SELECT SUM(durationSeconds) FROM training_sessions")
    fun getTotalTrainingTimeSeconds(): Flow<Long?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: TrainingSessionEntity): Long

    @Query("DELETE FROM training_sessions")
    suspend fun deleteAllSessions()
}
