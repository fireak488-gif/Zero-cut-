package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sensitivity_presets")
data class SensitivityPresetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val deviceCategory: String, // "Mid-range", "Flagship", "Low-end", "Emulator PC", "iPad / Tablet"
    val general: Int,
    val redDot: Int,
    val scope2x: Int,
    val scope4x: Int,
    val sniperScope: Int,
    val freeLook: Int,
    val dpi: Int = 411,
    val fireButtonSize: Int = 46,
    val isDefault: Boolean = false,
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
