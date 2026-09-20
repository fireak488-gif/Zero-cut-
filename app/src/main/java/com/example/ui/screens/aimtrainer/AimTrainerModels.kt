package com.example.ui.screens.aimtrainer

enum class DrillType(
    val title: String,
    val subtitle: String,
    val iconName: String,
    val defaultDurationSecs: Int
) {
    HEADSHOT(
        title = "Headshot Drill",
        subtitle = "Hit the head hitbox for critical 3x score multiplier",
        iconName = "headshot",
        defaultDurationSecs = 30
    ),
    MOVING_TARGET(
        title = "Moving-Target Drill",
        subtitle = "Predict enemy strafing and movement paths",
        iconName = "moving",
        defaultDurationSecs = 30
    ),
    REACTION(
        title = "Reaction-Time Drill",
        subtitle = "Measure reaction speed in milliseconds when target triggers",
        iconName = "reaction",
        defaultDurationSecs = 20
    ),
    FLICK(
        title = "Flick-Shot Practice",
        subtitle = "Rapidly snap crosshair from center to peripheral targets",
        iconName = "flick",
        defaultDurationSecs = 30
    ),
    TRACKING(
        title = "Tracking Practice",
        subtitle = "Keep reticle locked on smoothly maneuvering drone",
        iconName = "tracking",
        defaultDurationSecs = 30
    ),
    ACCURACY(
        title = "Accuracy Test",
        subtitle = "Precision shooting with penalty for missed shots",
        iconName = "accuracy",
        defaultDurationSecs = 30
    )
}

enum class DrillState {
    READY,
    COUNTDOWN,
    ACTIVE,
    COMPLETED
}

data class FloatingHitEffect(
    val id: Long,
    val text: String,
    val x: Float,
    val y: Float,
    val isHeadshot: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

data class TargetEntity(
    val id: Long,
    var x: Float, // 0f..1f
    var y: Float, // 0f..1f
    var radius: Float, // in dp
    var velocityX: Float = 0f,
    var velocityY: Float = 0f,
    var isHeadshotZone: Boolean = false,
    var isReactionTriggered: Boolean = false,
    var spawnTime: Long = System.currentTimeMillis()
)
