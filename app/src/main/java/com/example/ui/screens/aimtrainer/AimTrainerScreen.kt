package com.example.ui.screens.aimtrainer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Adjust
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.TrainingSessionEntity
import com.example.data.repository.TrainingRepository
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.GoldenRank
import com.example.ui.theme.HeadshotRed
import com.example.ui.theme.MatrixGreen
import com.example.ui.theme.NeonOrange
import com.example.ui.theme.StealthBlack
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.SoundHapticManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

@Composable
fun AimTrainerScreen(
    initialDrillType: DrillType = DrillType.HEADSHOT,
    repository: TrainingRepository,
    soundHapticManager: SoundHapticManager,
    onNavigateBack: (() -> Unit)? = null
) {
    var selectedDrill by remember { mutableStateOf(initialDrillType) }
    var drillState by remember { mutableStateOf(DrillState.READY) }

    // Session Metrics
    var score by remember { mutableIntStateOf(0) }
    var hits by remember { mutableIntStateOf(0) }
    var headshots by remember { mutableIntStateOf(0) }
    var misses by remember { mutableIntStateOf(0) }
    var timeLeftSeconds by remember { mutableIntStateOf(selectedDrill.defaultDurationSecs) }
    var reactionTimes = remember { mutableStateListOf<Long>() }
    var trackingTimeMs by remember { mutableLongStateOf(0L) }
    var totalActiveTimeMs by remember { mutableLongStateOf(0L) }
    val floatingEffects = remember { mutableStateListOf<FloatingHitEffect>() }

    // Personal best from state
    var personalBestScore by remember { mutableIntStateOf(0) }

    // Countdown state
    var countdownValue by remember { mutableIntStateOf(3) }

    val coroutineScope = rememberCoroutineScope()

    // Observe personal best
    LaunchedEffect(selectedDrill) {
        repository.getBestScore(selectedDrill.name).collect { pb ->
            personalBestScore = pb ?: 0
        }
    }

    // Cleanup floating effects periodically
    LaunchedEffect(floatingEffects.size) {
        if (floatingEffects.isNotEmpty()) {
            delay(800)
            if (floatingEffects.isNotEmpty()) {
                val now = System.currentTimeMillis()
                floatingEffects.removeAll { now - it.timestamp > 900 }
            }
        }
    }

    // Timer loop for ACTIVE state
    LaunchedEffect(drillState) {
        if (drillState == DrillState.COUNTDOWN) {
            countdownValue = 3
            soundHapticManager.playCountdownBeep(false)
            while (countdownValue > 1) {
                delay(1000)
                countdownValue--
                soundHapticManager.playCountdownBeep(false)
            }
            delay(1000)
            soundHapticManager.playCountdownBeep(true)
            drillState = DrillState.ACTIVE
            timeLeftSeconds = selectedDrill.defaultDurationSecs
        } else if (drillState == DrillState.ACTIVE) {
            while (timeLeftSeconds > 0 && drillState == DrillState.ACTIVE) {
                delay(1000)
                timeLeftSeconds--
                totalActiveTimeMs += 1000
            }
            if (drillState == DrillState.ACTIVE) {
                drillState = DrillState.COMPLETED

                // Calculate final metrics and save
                val accuracyPct = if (hits + misses > 0) (hits.toFloat() / (hits + misses)) * 100f else 0f
                val avgReaction = if (reactionTimes.isNotEmpty()) reactionTimes.average().toLong() else 0L

                val session = TrainingSessionEntity(
                    drillType = selectedDrill.name,
                    score = score,
                    accuracy = accuracyPct,
                    headshots = headshots,
                    hits = hits,
                    misses = misses,
                    averageReactionMs = avgReaction,
                    durationSeconds = selectedDrill.defaultDurationSecs - timeLeftSeconds
                )
                repository.saveTrainingSession(session)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StealthBlack)
            .padding(16.dp)
    ) {
        // Top Drill Selector
        if (drillState == DrillState.READY || drillState == DrillState.COMPLETED) {
            Text(
                text = "AIM TRAINER SUITE",
                style = MaterialTheme.typography.titleLarge,
                color = CyberCyan,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
            Text(
                text = "Select drill mode to improve reflex, tracking & drag headshots",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(DrillType.values()) { drill ->
                    val isSelected = drill == selectedDrill
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) DarkSurfaceVariant else DarkSurface
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) CyberCyan else DarkCardBorder,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                if (drillState == DrillState.READY || drillState == DrillState.COMPLETED) {
                                    selectedDrill = drill
                                    drillState = DrillState.READY
                                    score = 0
                                    hits = 0
                                    headshots = 0
                                    misses = 0
                                    reactionTimes.clear()
                                    floatingEffects.clear()
                                }
                            }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) CyberCyan else TextMuted)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = drill.title,
                                style = MaterialTheme.typography.labelLarge,
                                color = if (isSelected) TextPrimary else TextSecondary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
        }

        // Live HUD Metrics Bar (Active or Countdown)
        if (drillState == DrillState.ACTIVE || drillState == DrillState.COUNTDOWN) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurface, RoundedCornerShape(12.dp))
                    .border(1.dp, DarkCardBorder, RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("SCORE", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    Text(
                        text = "$score",
                        style = MaterialTheme.typography.titleLarge,
                        color = CyberCyan,
                        fontWeight = FontWeight.Black
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("HEADSHOTS", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "$headshots",
                            style = MaterialTheme.typography.titleLarge,
                            color = HeadshotRed,
                            fontWeight = FontWeight.Black
                        )
                        if (hits > 0) {
                            Text(
                                text = " (${(headshots * 100 / hits)}%)",
                                style = MaterialTheme.typography.labelMedium,
                                color = TextSecondary
                            )
                        }
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("ACCURACY", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    val acc = if (hits + misses > 0) (hits * 100 / (hits + misses)) else 100
                    Text(
                        text = "$acc%",
                        style = MaterialTheme.typography.titleLarge,
                        color = if (acc >= 75) MatrixGreen else NeonOrange,
                        fontWeight = FontWeight.Black
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("TIME", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    Text(
                        text = "${timeLeftSeconds}s",
                        style = MaterialTheme.typography.titleLarge,
                        color = if (timeLeftSeconds <= 5) DangerRed else TextPrimary,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }

        // Drill Main Canvas / Display
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(DarkSurface)
                .border(1.5.dp, if (drillState == DrillState.ACTIVE) CyberCyan else DarkCardBorder, RoundedCornerShape(16.dp))
        ) {
            when (drillState) {
                DrillState.READY -> {
                    DrillReadyOverview(
                        drill = selectedDrill,
                        personalBest = personalBestScore,
                        onStart = {
                            score = 0
                            hits = 0
                            headshots = 0
                            misses = 0
                            reactionTimes.clear()
                            floatingEffects.clear()
                            trackingTimeMs = 0L
                            totalActiveTimeMs = 0L
                            drillState = DrillState.COUNTDOWN
                        }
                    )
                }
                DrillState.COUNTDOWN -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "GET READY",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextMuted,
                                letterSpacing = 2.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "$countdownValue",
                                fontSize = 84.sp,
                                fontWeight = FontWeight.Black,
                                color = CyberCyan
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = selectedDrill.title.uppercase(),
                                style = MaterialTheme.typography.labelLarge,
                                color = NeonOrange
                            )
                        }
                    }
                }
                DrillState.ACTIVE -> {
                    ActiveDrillInteractiveArena(
                        drillType = selectedDrill,
                        onHit = { isHeadshot, points, px, py ->
                            hits++
                            if (isHeadshot) {
                                headshots++
                                soundHapticManager.playHeadshotFeedback()
                                floatingEffects.add(
                                    FloatingHitEffect(
                                        id = System.nanoTime(),
                                        text = "+$points CRITICAL!",
                                        x = px,
                                        y = py,
                                        isHeadshot = true
                                    )
                                )
                            } else {
                                soundHapticManager.playHitFeedback()
                                floatingEffects.add(
                                    FloatingHitEffect(
                                        id = System.nanoTime(),
                                        text = "+$points",
                                        x = px,
                                        y = py,
                                        isHeadshot = false
                                    )
                                )
                            }
                            score += points
                        },
                        onMiss = { px, py ->
                            misses++
                            soundHapticManager.playMissFeedback()
                            floatingEffects.add(
                                FloatingHitEffect(
                                    id = System.nanoTime(),
                                    text = "MISS",
                                    x = px,
                                    y = py,
                                    isHeadshot = false
                                )
                            )
                        },
                        onReactionRecorded = { ms ->
                            reactionTimes.add(ms)
                        },
                        onTrackingProgress = { onTargetMs ->
                            trackingTimeMs += onTargetMs
                        }
                    )

                    // Floating Damage Numbers
                    floatingEffects.forEach { effect ->
                        Box(
                            modifier = Modifier
                                .offset {
                                    val elapsed = (System.currentTimeMillis() - effect.timestamp).coerceAtMost(800)
                                    val offsetY = -(elapsed * 0.15f).toInt()
                                    IntOffset(
                                        x = effect.x.toInt().coerceIn(20, 1000),
                                        y = (effect.y.toInt() + offsetY).coerceIn(20, 1800)
                                    )
                                }
                        ) {
                            Text(
                                text = effect.text,
                                style = MaterialTheme.typography.labelLarge,
                                color = if (effect.isHeadshot) HeadshotRed else if (effect.text == "MISS") TextMuted else CyberCyan,
                                fontWeight = FontWeight.Black,
                                fontSize = if (effect.isHeadshot) 18.sp else 14.sp
                            )
                        }
                    }
                }
                DrillState.COMPLETED -> {
                    DrillResultsCard(
                        drill = selectedDrill,
                        score = score,
                        hits = hits,
                        headshots = headshots,
                        misses = misses,
                        reactionTimes = reactionTimes,
                        personalBest = personalBestScore,
                        onRetry = {
                            score = 0
                            hits = 0
                            headshots = 0
                            misses = 0
                            reactionTimes.clear()
                            floatingEffects.clear()
                            drillState = DrillState.COUNTDOWN
                        },
                        onSelectAnother = {
                            drillState = DrillState.READY
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun DrillReadyOverview(
    drill: DrillType,
    personalBest: Int,
    onStart: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(DarkSurfaceVariant, CircleShape)
                .border(2.dp, CyberCyan, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = when (drill) {
                    DrillType.HEADSHOT -> Icons.Default.Adjust
                    DrillType.REACTION -> Icons.Default.FlashOn
                    DrillType.TRACKING -> Icons.Default.Speed
                    DrillType.MOVING_TARGET -> Icons.Default.TrendingUp
                    else -> Icons.Default.Adjust
                },
                contentDescription = null,
                tint = CyberCyan,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = drill.title,
            style = MaterialTheme.typography.headlineMedium,
            color = TextPrimary,
            fontWeight = FontWeight.Black
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = drill.subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Personal Best Card
        Row(
            modifier = Modifier
                .background(DarkSurfaceVariant, RoundedCornerShape(12.dp))
                .border(1.dp, DarkCardBorder, RoundedCornerShape(12.dp))
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.EmojiEvents,
                contentDescription = null,
                tint = GoldenRank,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "PERSONAL BEST: ",
                style = MaterialTheme.typography.labelMedium,
                color = TextMuted
            )
            Text(
                text = "$personalBest PTS",
                style = MaterialTheme.typography.labelLarge,
                color = GoldenRank,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = onStart,
            colors = ButtonDefaults.buttonColors(
                containerColor = CyberCyan,
                contentColor = StealthBlack
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth(0.65f)
                .height(52.dp)
                .testTag("start_drill_button")
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("START DRILL", fontWeight = FontWeight.Black, fontSize = 16.sp)
        }
    }
}

@Composable
fun ActiveDrillInteractiveArena(
    drillType: DrillType,
    onHit: (isHeadshot: Boolean, points: Int, x: Float, y: Float) -> Unit,
    onMiss: (x: Float, y: Float) -> Unit,
    onReactionRecorded: (ms: Long) -> Unit,
    onTrackingProgress: (ms: Long) -> Unit
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val arenaWidth = constraints.maxWidth.toFloat()
        val arenaHeight = constraints.maxHeight.toFloat()

        when (drillType) {
            DrillType.HEADSHOT -> {
                HeadshotPracticeCanvas(arenaWidth, arenaHeight, onHit, onMiss)
            }
            DrillType.MOVING_TARGET -> {
                MovingTargetCanvas(arenaWidth, arenaHeight, onHit, onMiss)
            }
            DrillType.REACTION -> {
                ReactionTimeCanvas(arenaWidth, arenaHeight, onHit, onReactionRecorded)
            }
            DrillType.FLICK -> {
                FlickShotCanvas(arenaWidth, arenaHeight, onHit, onMiss)
            }
            DrillType.TRACKING -> {
                TrackingPracticeCanvas(arenaWidth, arenaHeight, onTrackingProgress)
            }
            DrillType.ACCURACY -> {
                AccuracyTestCanvas(arenaWidth, arenaHeight, onHit, onMiss)
            }
        }
    }
}

@Composable
fun HeadshotPracticeCanvas(
    width: Float,
    height: Float,
    onHit: (Boolean, Int, Float, Float) -> Unit,
    onMiss: (Float, Float) -> Unit
) {
    // Target mannequin positions
    var dummyX by remember { mutableFloatStateOf(width * 0.5f) }
    var dummyY by remember { mutableFloatStateOf(height * 0.45f) }
    val headRadius = 42f
    val bodyWidth = 110f
    val bodyHeight = 140f

    fun reposition() {
        dummyX = Random.nextFloat() * (width - 240f) + 120f
        dummyY = Random.nextFloat() * (height - 300f) + 120f
    }

    LaunchedEffect(Unit) {
        reposition()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val headDist = sqrt((offset.x - dummyX) * (offset.x - dummyX) + (offset.y - (dummyY - 70f)) * (offset.y - (dummyY - 70f)))
                    val inBody = offset.x in (dummyX - bodyWidth / 2)..(dummyX + bodyWidth / 2) &&
                            offset.y in (dummyY - 20f)..(dummyY + bodyHeight)

                    if (headDist <= headRadius) {
                        onHit(true, 300, offset.x, offset.y)
                        reposition()
                    } else if (inBody) {
                        onHit(false, 80, offset.x, offset.y)
                        reposition()
                    } else {
                        onMiss(offset.x, offset.y)
                    }
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCrosshairGrid(size)

            // Draw Body Hitbox
            drawRoundRect(
                color = Color(0xFF1E293B),
                topLeft = Offset(dummyX - bodyWidth / 2, dummyY - 20f),
                size = Size(bodyWidth, bodyHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f, 16f)
            )
            drawRoundRect(
                color = Color(0xFF334155),
                topLeft = Offset(dummyX - bodyWidth / 2, dummyY - 20f),
                size = Size(bodyWidth, bodyHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f, 16f),
                style = Stroke(width = 2.5f)
            )

            // Draw Head Hitbox (Glowing Red for Free Fire Drag Headshot indicator)
            val headCenter = Offset(dummyX, dummyY - 70f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(HeadshotRed, Color(0xFF8B0000)),
                    center = headCenter,
                    radius = headRadius
                ),
                radius = headRadius,
                center = headCenter
            )
            drawCircle(
                color = Color.White,
                radius = headRadius,
                center = headCenter,
                style = Stroke(width = 3f)
            )
            // Bullseye inside head
            drawCircle(
                color = Color.Yellow,
                radius = headRadius * 0.35f,
                center = headCenter
            )
        }
    }
}

@Composable
fun MovingTargetCanvas(
    width: Float,
    height: Float,
    onHit: (Boolean, Int, Float, Float) -> Unit,
    onMiss: (Float, Float) -> Unit
) {
    var targetX by remember { mutableFloatStateOf(width * 0.5f) }
    var targetY by remember { mutableFloatStateOf(height * 0.4f) }
    var vx by remember { mutableFloatStateOf(8f) }
    var vy by remember { mutableFloatStateOf(4f) }
    val radius = 46f

    LaunchedEffect(width, height) {
        while (true) {
            delay(16)
            targetX += vx
            targetY += vy

            if (targetX - radius <= 20f || targetX + radius >= width - 20f) {
                vx = -vx
            }
            if (targetY - radius <= 30f || targetY + radius >= height - 30f) {
                vy = -vy
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val dist = sqrt((offset.x - targetX) * (offset.x - targetX) + (offset.y - targetY) * (offset.y - targetY))
                    if (dist <= radius) {
                        val isCenter = dist <= radius * 0.45f
                        val pts = if (isCenter) 250 else 100
                        onHit(isCenter, pts, offset.x, offset.y)
                        // randomize direction on hit
                        vx = (if (Random.nextBoolean()) 1 else -1) * (Random.nextFloat() * 6f + 6f)
                        vy = (if (Random.nextBoolean()) 1 else -1) * (Random.nextFloat() * 4f + 3f)
                    } else {
                        onMiss(offset.x, offset.y)
                    }
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCrosshairGrid(size)

            val center = Offset(targetX, targetY)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(NeonOrange, Color(0xFFBF360C)),
                    center = center,
                    radius = radius
                ),
                radius = radius,
                center = center
            )
            drawCircle(
                color = Color.White,
                radius = radius,
                center = center,
                style = Stroke(width = 3f)
            )
            // Center ring
            drawCircle(
                color = CyberCyan,
                radius = radius * 0.4f,
                center = center
            )
        }
    }
}

@Composable
fun ReactionTimeCanvas(
    width: Float,
    height: Float,
    onHit: (Boolean, Int, Float, Float) -> Unit,
    onReactionRecorded: (Long) -> Unit
) {
    var isTriggered by remember { mutableStateOf(false) }
    var triggerTimestamp by remember { mutableLongStateOf(0L) }
    var lastReactionMs by remember { mutableLongStateOf(0L) }
    var waitingForNext by remember { mutableStateOf(false) }

    LaunchedEffect(waitingForNext) {
        isTriggered = false
        val delayMs = Random.nextLong(1400, 3800)
        delay(delayMs)
        isTriggered = true
        triggerTimestamp = System.currentTimeMillis()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isTriggered) MatrixGreen.copy(alpha = 0.15f) else Color.Transparent)
            .pointerInput(isTriggered) {
                detectTapGestures { offset ->
                    if (isTriggered) {
                        val reactionMs = System.currentTimeMillis() - triggerTimestamp
                        lastReactionMs = reactionMs
                        onReactionRecorded(reactionMs)
                        val points = (500 - reactionMs).coerceAtLeast(50).toInt()
                        onHit(reactionMs < 250, points, offset.x, offset.y)
                        waitingForNext = !waitingForNext
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .clip(CircleShape)
                    .background(if (isTriggered) MatrixGreen else HeadshotRed)
                    .border(4.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isTriggered) "TAP NOW!" else "WAIT...",
                    style = MaterialTheme.typography.titleLarge,
                    color = StealthBlack,
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (lastReactionMs > 0) {
                Text(
                    text = "$lastReactionMs ms",
                    style = MaterialTheme.typography.headlineMedium,
                    color = if (lastReactionMs < 230) MatrixGreen else CyberCyan,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = when {
                        lastReactionMs < 200 -> "PRO ESPORTS REFLEX"
                        lastReactionMs < 260 -> "EXCELLENT REACTION"
                        lastReactionMs < 340 -> "GOOD REACTION"
                        else -> "KEEP PRACTICING"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
            } else {
                Text(
                    text = "Tap the green circle as soon as it appears",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
fun FlickShotCanvas(
    width: Float,
    height: Float,
    onHit: (Boolean, Int, Float, Float) -> Unit,
    onMiss: (Float, Float) -> Unit
) {
    var targetX by remember { mutableFloatStateOf(width * 0.5f) }
    var targetY by remember { mutableFloatStateOf(height * 0.4f) }
    val radius = 38f

    fun spawnNewTarget() {
        val angle = Random.nextFloat() * 2f * Math.PI.toFloat()
        val dist = Random.nextFloat() * (minOf(width, height) * 0.38f) + 80f
        targetX = (width * 0.5f + cos(angle) * dist).coerceIn(radius + 20f, width - radius - 20f)
        targetY = (height * 0.5f + sin(angle) * dist).coerceIn(radius + 20f, height - radius - 20f)
    }

    LaunchedEffect(Unit) {
        spawnNewTarget()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val dist = sqrt((offset.x - targetX) * (offset.x - targetX) + (offset.y - targetY) * (offset.y - targetY))
                    if (dist <= radius) {
                        val isCenter = dist <= radius * 0.5f
                        val points = if (isCenter) 200 else 100
                        onHit(isCenter, points, offset.x, offset.y)
                        spawnNewTarget()
                    } else {
                        onMiss(offset.x, offset.y)
                    }
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCrosshairGrid(size)

            // Center reference reticle
            val center = Offset(size.width * 0.5f, size.height * 0.5f)
            drawCircle(
                color = CyberCyan.copy(alpha = 0.4f),
                radius = 24f,
                center = center,
                style = Stroke(width = 2f)
            )
            drawLine(
                color = CyberCyan.copy(alpha = 0.5f),
                start = Offset(center.x - 30f, center.y),
                end = Offset(center.x + 30f, center.y),
                strokeWidth = 2f
            )
            drawLine(
                color = CyberCyan.copy(alpha = 0.5f),
                start = Offset(center.x, center.y - 30f),
                end = Offset(center.x, center.y + 30f),
                strokeWidth = 2f
            )

            // Flick target
            val targetCenter = Offset(targetX, targetY)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(CyberCyan, Color(0xFF006064)),
                    center = targetCenter,
                    radius = radius
                ),
                radius = radius,
                center = targetCenter
            )
            drawCircle(
                color = Color.White,
                radius = radius,
                center = targetCenter,
                style = Stroke(width = 3f)
            )
            drawCircle(
                color = HeadshotRed,
                radius = radius * 0.4f,
                center = targetCenter
            )
        }
    }
}

@Composable
fun TrackingPracticeCanvas(
    width: Float,
    height: Float,
    onTrackingProgress: (Long) -> Unit
) {
    var droneX by remember { mutableFloatStateOf(width * 0.5f) }
    var droneY by remember { mutableFloatStateOf(height * 0.5f) }
    var fingerX by remember { mutableFloatStateOf(-100f) }
    var fingerY by remember { mutableFloatStateOf(-100f) }
    var isFingerTouching by remember { mutableStateOf(false) }
    var onTargetTimeRatio by remember { mutableFloatStateOf(0f) }
    val radius = 54f

    var angle by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(width, height) {
        while (true) {
            delay(16)
            angle += 0.035f
            droneX = width * 0.5f + cos(angle) * (width * 0.35f)
            droneY = height * 0.5f + sin(angle * 2f) * (height * 0.28f)

            if (isFingerTouching) {
                val dist = sqrt((fingerX - droneX) * (fingerX - droneX) + (fingerY - droneY) * (fingerY - droneY))
                if (dist <= radius) {
                    onTrackingProgress(16L)
                    onTargetTimeRatio = (onTargetTimeRatio * 0.95f + 1f * 0.05f).coerceIn(0f, 1f)
                } else {
                    onTargetTimeRatio = (onTargetTimeRatio * 0.95f).coerceIn(0f, 1f)
                }
            } else {
                onTargetTimeRatio = (onTargetTimeRatio * 0.92f).coerceIn(0f, 1f)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        isFingerTouching = true
                        fingerX = offset.x
                        fingerY = offset.y
                    },
                    onDrag = { change, _ ->
                        fingerX = change.position.x
                        fingerY = change.position.y
                    },
                    onDragEnd = {
                        isFingerTouching = false
                    },
                    onDragCancel = {
                        isFingerTouching = false
                    }
                )
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCrosshairGrid(size)

            val droneCenter = Offset(droneX, droneY)
            val isLocked = onTargetTimeRatio > 0.6f

            drawCircle(
                brush = Brush.radialGradient(
                    colors = if (isLocked) listOf(MatrixGreen, Color(0xFF004D40))
                    else listOf(CyberCyan, Color(0xFF006064)),
                    center = droneCenter,
                    radius = radius
                ),
                radius = radius,
                center = droneCenter
            )
            drawCircle(
                color = if (isLocked) MatrixGreen else Color.White,
                radius = radius,
                center = droneCenter,
                style = Stroke(width = 3.5f)
            )

            // Draw target reticle inside drone
            drawCircle(
                color = Color.White,
                radius = radius * 0.45f,
                center = droneCenter,
                style = Stroke(width = 2f)
            )
        }

        // Tracking % overlay
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "KEEP FINGER / RETICLE ON DRONE",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { onTargetTimeRatio },
                modifier = Modifier
                    .width(180.dp)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = MatrixGreen,
                trackColor = DarkSurfaceVariant
            )
        }
    }
}

@Composable
fun AccuracyTestCanvas(
    width: Float,
    height: Float,
    onHit: (Boolean, Int, Float, Float) -> Unit,
    onMiss: (Float, Float) -> Unit
) {
    var targets = remember { mutableStateListOf<Offset>() }
    val radius = 32f

    fun refreshTargets() {
        targets.clear()
        for (i in 0 until 4) {
            val tx = Random.nextFloat() * (width - 120f) + 60f
            val ty = Random.nextFloat() * (height - 140f) + 70f
            targets.add(Offset(tx, ty))
        }
    }

    LaunchedEffect(Unit) {
        refreshTargets()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val hitIndex = targets.indexOfFirst {
                        sqrt((offset.x - it.x) * (offset.x - it.x) + (offset.y - it.y) * (offset.y - it.y)) <= radius
                    }

                    if (hitIndex != -1) {
                        onHit(true, 150, offset.x, offset.y)
                        targets.removeAt(hitIndex)
                        if (targets.isEmpty()) {
                            refreshTargets()
                        }
                    } else {
                        onMiss(offset.x, offset.y)
                    }
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCrosshairGrid(size)

            targets.forEach { target ->
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(NeonOrange, Color(0xFFD84315)),
                        center = target,
                        radius = radius
                    ),
                    radius = radius,
                    center = target
                )
                drawCircle(
                    color = Color.White,
                    radius = radius,
                    center = target,
                    style = Stroke(width = 2.5f)
                )
                drawCircle(
                    color = HeadshotRed,
                    radius = radius * 0.45f,
                    center = target
                )
            }
        }
    }
}

fun DrawScope.drawCrosshairGrid(size: Size) {
    val gridSpacing = 60f
    var x = 0f
    while (x < size.width) {
        drawLine(
            color = Color(0x1100E5FF),
            start = Offset(x, 0f),
            end = Offset(x, size.height),
            strokeWidth = 1f
        )
        x += gridSpacing
    }
    var y = 0f
    while (y < size.height) {
        drawLine(
            color = Color(0x1100E5FF),
            start = Offset(0f, y),
            end = Offset(size.width, y),
            strokeWidth = 1f
        )
        y += gridSpacing
    }
}

@Composable
fun DrillResultsCard(
    drill: DrillType,
    score: Int,
    hits: Int,
    headshots: Int,
    misses: Int,
    reactionTimes: List<Long>,
    personalBest: Int,
    onRetry: () -> Unit,
    onSelectAnother: () -> Unit
) {
    val totalShots = hits + misses
    val accuracyPct = if (totalShots > 0) (hits * 100 / totalShots) else 0
    val headshotPct = if (hits > 0) (headshots * 100 / hits) else 0
    val isNewBest = score > personalBest && score > 0

    val grade = when {
        accuracyPct >= 85 && headshotPct >= 60 -> "S"
        accuracyPct >= 70 && headshotPct >= 40 -> "A"
        accuracyPct >= 50 -> "B"
        else -> "C"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Grade Badge
        Box(
            modifier = Modifier
                .size(76.dp)
                .background(
                    when (grade) {
                        "S" -> GoldenRank.copy(alpha = 0.2f)
                        "A" -> CyberCyan.copy(alpha = 0.2f)
                        else -> DarkSurfaceVariant
                    },
                    CircleShape
                )
                .border(
                    3.dp,
                    when (grade) {
                        "S" -> GoldenRank
                        "A" -> CyberCyan
                        else -> TextMuted
                    },
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "GRADE $grade",
                style = MaterialTheme.typography.titleLarge,
                color = when (grade) {
                    "S" -> GoldenRank
                    "A" -> CyberCyan
                    else -> TextPrimary
                },
                fontWeight = FontWeight.Black
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "DRILL COMPLETE",
            style = MaterialTheme.typography.titleSmall,
            color = MatrixGreen,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        )

        Text(
            text = "$score PTS",
            fontSize = 44.sp,
            fontWeight = FontWeight.Black,
            color = CyberCyan
        )

        if (isNewBest) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(GoldenRank.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Icon(Icons.Outlined.EmojiEvents, contentDescription = null, tint = GoldenRank, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("NEW PERSONAL BEST!", style = MaterialTheme.typography.labelSmall, color = GoldenRank, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Stats Matrix
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurfaceVariant, RoundedCornerShape(12.dp))
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("ACCURACY", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                Text("$accuracyPct%", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("HEADSHOT %", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                Text("$headshotPct%", style = MaterialTheme.typography.titleMedium, color = HeadshotRed, fontWeight = FontWeight.Bold)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("HITS / MISS", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                Text("$hits / $misses", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
            }
        }

        if (reactionTimes.isNotEmpty()) {
            Spacer(modifier = Modifier.height(10.dp))
            val avg = reactionTimes.average().toLong()
            Text(
                text = "Average Reaction Time: $avg ms",
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onSelectAnother,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            ) {
                Text("ALL DRILLS", color = TextPrimary)
            }
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = StealthBlack),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("retry_drill_button")
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("PLAY AGAIN", fontWeight = FontWeight.Bold)
            }
        }
    }
}
