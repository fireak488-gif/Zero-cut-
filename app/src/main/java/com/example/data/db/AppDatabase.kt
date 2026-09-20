package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        TrainingSessionEntity::class,
        SensitivityPresetEntity::class,
        HudLayoutEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun trainingSessionDao(): TrainingSessionDao
    abstract fun sensitivityPresetDao(): SensitivityPresetDao
    abstract fun hudLayoutDao(): HudLayoutDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "zero_shot_panel.db"
                ).addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            getInstance(context).seedInitialData()
                        }
                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }
    }

    suspend fun seedInitialData() {
        val sensitivityDao = sensitivityPresetDao()
        if (sensitivityDao.getCount() == 0) {
            sensitivityDao.insertPresets(
                listOf(
                    SensitivityPresetEntity(
                        name = "Pro Rusher (One-Tap Drag)",
                        deviceCategory = "Flagship / High-End",
                        general = 98,
                        redDot = 96,
                        scope2x = 92,
                        scope4x = 88,
                        sniperScope = 62,
                        freeLook = 75,
                        dpi = 440,
                        fireButtonSize = 44,
                        isDefault = true,
                        notes = "Fast upward drag flick for instant headshots with MP40 and Shotguns. Maximum general mobility."
                    ),
                    SensitivityPresetEntity(
                        name = "Competitive All-Rounder",
                        deviceCategory = "Mid-Range",
                        general = 95,
                        redDot = 90,
                        scope2x = 86,
                        scope4x = 82,
                        sniperScope = 58,
                        freeLook = 68,
                        dpi = 411,
                        fireButtonSize = 48,
                        isDefault = true,
                        notes = "Balanced recoil control and high accuracy across mid-range ARs (AK47, SCAR, M4A1)."
                    ),
                    SensitivityPresetEntity(
                        name = "Sniper & DMR Headhunter",
                        deviceCategory = "Mid-Range / Flagship",
                        general = 90,
                        redDot = 85,
                        scope2x = 80,
                        scope4x = 75,
                        sniperScope = 48,
                        freeLook = 55,
                        dpi = 392,
                        fireButtonSize = 42,
                        isDefault = true,
                        notes = "Steady micro-adjustments for AWM, Woodpecker, and Kar98k quick-switch shots."
                    ),
                    SensitivityPresetEntity(
                        name = "Low-End / 60Hz Smooth Boost",
                        deviceCategory = "Low-End (2-4GB RAM)",
                        general = 100,
                        redDot = 100,
                        scope2x = 96,
                        scope4x = 92,
                        sniperScope = 70,
                        freeLook = 85,
                        dpi = 480,
                        fireButtonSize = 45,
                        isDefault = true,
                        notes = "Compensates for lower touch sampling rates on budget hardware. Maximizes flick velocity."
                    ),
                    SensitivityPresetEntity(
                        name = "PC Emulator (BlueStacks / MSI)",
                        deviceCategory = "PC Emulator",
                        general = 72,
                        redDot = 68,
                        scope2x = 64,
                        scope4x = 60,
                        sniperScope = 40,
                        freeLook = 50,
                        dpi = 240,
                        fireButtonSize = 38,
                        isDefault = true,
                        notes = "Tuned for mouse polling rate (800-1600 DPI) and x/y axis tweak on emulator keymapping."
                    )
                )
            )
        }

        val hudDao = hudLayoutDao()
        if (hudDao.getCount() == 0) {
            hudDao.insertLayouts(
                listOf(
                    HudLayoutEntity(
                        name = "2-Finger Speed Drag",
                        fingerType = 2,
                        fireButtonSize = 45,
                        fireButtonX = 0.82f,
                        fireButtonY = 0.72f,
                        leftFireButtonSize = 50,
                        leftFireButtonX = 0.15f,
                        leftFireButtonY = 0.35f,
                        glooWallSize = 85,
                        glooWallX = 0.22f,
                        glooWallY = 0.65f,
                        jumpButtonSize = 65,
                        jumpButtonX = 0.90f,
                        jumpButtonY = 0.48f,
                        crouchButtonSize = 65,
                        crouchButtonX = 0.78f,
                        crouchButtonY = 0.50f,
                        scopeButtonSize = 70,
                        scopeButtonX = 0.88f,
                        scopeButtonY = 0.30f,
                        analogJoystickSize = 80,
                        isPreset = true,
                        description = "Ideal for classic 2-thumb players. Fire button placed in lower-right quadrant with 35% clearance above for upward drag headshots."
                    ),
                    HudLayoutEntity(
                        name = "3-Finger Claw (Fast Gloo Wall)",
                        fingerType = 3,
                        fireButtonSize = 42,
                        fireButtonX = 0.84f,
                        fireButtonY = 0.74f,
                        leftFireButtonSize = 60,
                        leftFireButtonX = 0.18f,
                        leftFireButtonY = 0.22f,
                        glooWallSize = 90,
                        glooWallX = 0.25f,
                        glooWallY = 0.60f,
                        jumpButtonSize = 70,
                        jumpButtonX = 0.92f,
                        jumpButtonY = 0.50f,
                        crouchButtonSize = 70,
                        crouchButtonX = 0.80f,
                        crouchButtonY = 0.52f,
                        scopeButtonSize = 75,
                        scopeButtonX = 0.90f,
                        scopeButtonY = 0.32f,
                        analogJoystickSize = 85,
                        isPreset = true,
                        description = "Left index finger fires left fire button while left thumb places gloo wall and right thumb drags downward for instant 360 gloo protection."
                    ),
                    HudLayoutEntity(
                        name = "4-Finger Tournament Pro Claw",
                        fingerType = 4,
                        fireButtonSize = 40,
                        fireButtonX = 0.85f,
                        fireButtonY = 0.75f,
                        leftFireButtonSize = 65,
                        leftFireButtonX = 0.18f,
                        leftFireButtonY = 0.18f,
                        glooWallSize = 95,
                        glooWallX = 0.22f,
                        glooWallY = 0.58f,
                        jumpButtonSize = 75,
                        jumpButtonX = 0.86f,
                        jumpButtonY = 0.18f,
                        crouchButtonSize = 75,
                        crouchButtonX = 0.78f,
                        crouchButtonY = 0.52f,
                        scopeButtonSize = 80,
                        scopeButtonX = 0.92f,
                        scopeButtonY = 0.36f,
                        analogJoystickSize = 90,
                        isPreset = true,
                        description = "Simultaneous jump-shot, crouch-shot, and scope flicking. Left index on fire, right index on jump & scope, thumbs on movement & aim."
                    )
                )
            )
        }
    }
}
