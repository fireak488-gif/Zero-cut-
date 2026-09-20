package com.example.data.repository

import com.example.data.db.HudLayoutDao
import com.example.data.db.HudLayoutEntity
import com.example.data.db.SensitivityPresetDao
import com.example.data.db.SensitivityPresetEntity
import com.example.data.db.TrainingSessionDao
import com.example.data.db.TrainingSessionEntity
import kotlinx.coroutines.flow.Flow

class TrainingRepository(
    private val sessionDao: TrainingSessionDao,
    private val sensitivityDao: SensitivityPresetDao,
    private val hudDao: HudLayoutDao
) {
    // Training Sessions
    val allSessions: Flow<List<TrainingSessionEntity>> = sessionDao.getAllSessions()
    val totalSessionsCount: Flow<Int> = sessionDao.getTotalSessionsCount()
    val overallBestScore: Flow<Int?> = sessionDao.getOverallBestScore()
    val averageAccuracy: Flow<Float?> = sessionDao.getAverageAccuracy()
    val overallHeadshotRate: Flow<Float?> = sessionDao.getOverallHeadshotRate()
    val totalTrainingTimeSeconds: Flow<Long?> = sessionDao.getTotalTrainingTimeSeconds()

    fun getSessionsByType(drillType: String): Flow<List<TrainingSessionEntity>> =
        sessionDao.getSessionsByType(drillType)

    fun getBestScore(drillType: String): Flow<Int?> =
        sessionDao.getBestScore(drillType)

    suspend fun saveTrainingSession(session: TrainingSessionEntity): Long =
        sessionDao.insertSession(session)

    suspend fun resetAllTrainingData() {
        sessionDao.deleteAllSessions()
    }

    // Sensitivity Presets
    val allSensitivityPresets: Flow<List<SensitivityPresetEntity>> =
        sensitivityDao.getAllPresets()

    suspend fun saveSensitivityPreset(preset: SensitivityPresetEntity): Long =
        sensitivityDao.insertPreset(preset)

    suspend fun updateSensitivityPreset(preset: SensitivityPresetEntity) =
        sensitivityDao.updatePreset(preset)

    suspend fun deleteSensitivityPreset(preset: SensitivityPresetEntity) =
        sensitivityDao.deletePreset(preset)

    // HUD Layouts
    val allHudLayouts: Flow<List<HudLayoutEntity>> = hudDao.getAllLayouts()

    fun getHudLayoutsByFingers(fingerType: Int): Flow<List<HudLayoutEntity>> =
        hudDao.getLayoutsByFingers(fingerType)

    suspend fun saveHudLayout(layout: HudLayoutEntity): Long =
        hudDao.insertLayout(layout)

    suspend fun updateHudLayout(layout: HudLayoutEntity) =
        hudDao.updateLayout(layout)

    suspend fun deleteHudLayout(layout: HudLayoutEntity) =
        hudDao.deleteLayout(layout)
}
