package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface HudLayoutDao {
    @Query("SELECT * FROM hud_layouts ORDER BY isPreset DESC, fingerType ASC, id ASC")
    fun getAllLayouts(): Flow<List<HudLayoutEntity>>

    @Query("SELECT * FROM hud_layouts WHERE fingerType = :fingerType")
    fun getLayoutsByFingers(fingerType: Int): Flow<List<HudLayoutEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLayout(layout: HudLayoutEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLayouts(layouts: List<HudLayoutEntity>)

    @Update
    suspend fun updateLayout(layout: HudLayoutEntity)

    @Delete
    suspend fun deleteLayout(layout: HudLayoutEntity)

    @Query("DELETE FROM hud_layouts WHERE isPreset = 0")
    suspend fun deleteCustomLayouts()

    @Query("SELECT COUNT(*) FROM hud_layouts")
    suspend fun getCount(): Int
}
