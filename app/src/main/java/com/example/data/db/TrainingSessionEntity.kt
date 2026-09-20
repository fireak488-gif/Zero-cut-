package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "training_sessions")
data class TrainingSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val drillType: String, // "HEADSHOT", "REACTION", "TRACKING", "FLICK", "ACCURACY"
    val score: Int,
    val accuracy: Float, // percentage e.g. 88.5f
    val headshots: Int,
    val hits: Int,
    val misses: Int,
    val averageReactionMs: Long,
    val durationSeconds: Int,
    val timestamp: Long = System.currentTimeMillis()
)
