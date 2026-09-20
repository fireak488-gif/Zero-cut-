package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "hud_layouts")
data class HudLayoutEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val fingerType: Int, // 2, 3, or 4
    val fireButtonSize: Int, // e.g. 45 (%)
    val fireButtonX: Float, // relative normalized position 0f..1f
    val fireButtonY: Float,
    val leftFireButtonSize: Int,
    val leftFireButtonX: Float,
    val leftFireButtonY: Float,
    val glooWallSize: Int,
    val glooWallX: Float,
    val glooWallY: Float,
    val jumpButtonSize: Int,
    val jumpButtonX: Float,
    val jumpButtonY: Float,
    val crouchButtonSize: Int,
    val crouchButtonX: Float,
    val crouchButtonY: Float,
    val scopeButtonSize: Int,
    val scopeButtonX: Float,
    val scopeButtonY: Float,
    val analogJoystickSize: Int,
    val isPreset: Boolean = false,
    val description: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
