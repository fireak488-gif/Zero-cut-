package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SensitivityPresetDao {
    @Query("SELECT * FROM sensitivity_presets ORDER BY isDefault DESC, timestamp DESC")
    fun getAllPresets(): Flow<List<SensitivityPresetEntity>>

    @Query("SELECT * FROM sensitivity_presets WHERE id = :id")
    suspend fun getPresetById(id: Long): SensitivityPresetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreset(preset: SensitivityPresetEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPresets(presets: List<SensitivityPresetEntity>)

    @Update
    suspend fun updatePreset(preset: SensitivityPresetEntity)

    @Delete
    suspend fun deletePreset(preset: SensitivityPresetEntity)

    @Query("DELETE FROM sensitivity_presets WHERE isDefault = 0")
    suspend fun deleteCustomPresets()

    @Query("SELECT COUNT(*) FROM sensitivity_presets")
    suspend fun getCount(): Int
}
