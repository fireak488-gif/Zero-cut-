package com.example.ui.screens.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Adjust
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.QueryBuilder
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.TrainingSessionEntity
import com.example.data.repository.TrainingRepository
import com.example.ui.theme.CyberCyan
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
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun StatsScreen(
    repository: TrainingRepository
) {
    val coroutineScope = rememberCoroutineScope()
    val allSessions by repository.allSessions.collectAsState(initial = emptyList())
    val totalCount by repository.totalSessionsCount.collectAsState(initial = 0)
    val bestScore by repository.overallBestScore.collectAsState(initial = 0)
    val avgAccuracy by repository.averageAccuracy.collectAsState(initial = 0f)
    val headshotRate by repository.overallHeadshotRate.collectAsState(initial = 0f)
    val totalTimeSecs by repository.totalTrainingTimeSeconds.collectAsState(initial = 0L)

    var selectedDrillFilter by remember { mutableStateOf("ALL") }
    var showResetDialog by remember { mutableStateOf(false) }

    val filteredSessions = if (selectedDrillFilter == "ALL") {
        allSessions
    } else {
        allSessions.filter { it.drillType == selectedDrillFilter }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(StealthBlack)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "PLAYER STAT TRACKER",
                        style = MaterialTheme.typography.titleLarge,
                        color = CyberCyan,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Real-time progression telemetry & accuracy analytics",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }

                if (allSessions.isNotEmpty()) {
                    IconButton(onClick = { showResetDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Reset Stats", tint = TextMuted)
                    }
                }
            }
        }

        // Top Summary Cards 2x2 Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatSummaryCard(
                        title = "TOTAL SESSIONS",
                        value = "$totalCount",
                        icon = Icons.Default.Timeline,
                        color = CyberCyan,
                        modifier = Modifier.weight(1f)
                    )
                    StatSummaryCard(
                        title = "BEST SCORE",
                        value = "${bestScore ?: 0} PTS",
                        icon = Icons.Outlined.EmojiEvents,
                        color = GoldenRank,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatSummaryCard(
                        title = "AVG ACCURACY",
                        value = "${(avgAccuracy ?: 0f).toInt()}%",
                        icon = Icons.Default.Adjust,
                        color = MatrixGreen,
                        modifier = Modifier.weight(1f)
                    )
                    StatSummaryCard(
                        title = "HEADSHOT RATE",
                        value = "${(headshotRate ?: 0f).toInt()}%",
                        icon = Icons.Default.Speed,
                        color = HeadshotRed,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val minutes = (totalTimeSecs ?: 0L) / 60
                    val seconds = (totalTimeSecs ?: 0L) % 60
                    StatSummaryCard(
                        title = "TOTAL PRACTICE TIME",
                        value = "${minutes}m ${seconds}s",
                        icon = Icons.Default.QueryBuilder,
                        color = NeonOrange,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Progress Chart Visualizer
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, DarkCardBorder, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "PERFORMANCE TREND LINE (ACCURACY & SCORE)",
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Visual trend over recent completed training sessions",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    if (allSessions.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(DarkSurfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No training history yet. Play Aim Trainer drills to generate telemetry.", color = TextMuted, fontSize = 12.sp)
                        }
                    } else {
                        AccuracyTrendChart(sessions = allSessions.take(15).reversed())
                    }
                }
            }
        }

        // Drill Filter Chips
        item {
            Column {
                Text("FILTER SESSIONS BY DRILL:", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val drills = listOf("ALL", "HEADSHOT", "MOVING_TARGET", "REACTION", "FLICK", "TRACKING", "ACCURACY")
                    items(drills) { d ->
                        FilterChip(
                            selected = selectedDrillFilter == d,
                            onClick = { selectedDrillFilter = d },
                            label = { Text(d.replace("_", " "), fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyberCyan,
                                selectedLabelColor = StealthBlack
                            )
                        )
                    }
                }
            }
        }

        // Training History Log
        item {
            Text(
                text = "SESSION HISTORY LOG (${filteredSessions.size})",
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
        }

        if (filteredSessions.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                ) {
                    Box(modifier = Modifier.padding(20.dp), contentAlignment = Alignment.Center) {
                        Text("No sessions recorded for this category yet.", color = TextMuted, fontSize = 13.sp)
                    }
                }
            }
        } else {
            items(filteredSessions) { session ->
                SessionHistoryItem(session)
            }
        }
    }

    // Reset Confirmation Dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset Training Data?", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("This will permanently clear all recorded drill telemetry and scores from your local profile.", color = TextSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            repository.resetAllTrainingData()
                            showResetDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = HeadshotRed, contentColor = Color.White)
                ) {
                    Text("CLEAR ALL")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("CANCEL", color = TextSecondary)
                }
            },
            containerColor = DarkSurface
        )
    }
}

@Composable
fun StatSummaryCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.border(1.dp, DarkCardBorder, RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(color.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(title, style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 9.sp)
                Text(value, style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
fun AccuracyTrendChart(sessions: List<TrainingSessionEntity>) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurfaceVariant)
            .padding(8.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Draw horizontal reference lines (0%, 50%, 100%)
            drawLine(Color(0x22FFFFFF), Offset(0f, 0f), Offset(w, 0f), strokeWidth = 1f)
            drawLine(Color(0x22FFFFFF), Offset(0f, h * 0.5f), Offset(w, h * 0.5f), strokeWidth = 1f)
            drawLine(Color(0x22FFFFFF), Offset(0f, h), Offset(w, h), strokeWidth = 1f)

            if (sessions.size < 2) {
                val pt = sessions.firstOrNull()
                if (pt != null) {
                    val y = h - (pt.accuracy / 100f) * h
                    drawCircle(CyberCyan, radius = 6f, center = Offset(w * 0.5f, y))
                }
                return@Canvas
            }

            val stepX = w / (sessions.size - 1)
            val path = Path()
            val fillPath = Path()

            sessions.forEachIndexed { index, s ->
                val x = index * stepX
                val y = (h - (s.accuracy.coerceIn(0f, 100f) / 100f) * (h - 20f) - 10f)

                if (index == 0) {
                    path.moveTo(x, y)
                    fillPath.moveTo(x, h)
                    fillPath.lineTo(x, y)
                } else {
                    path.lineTo(x, y)
                    fillPath.lineTo(x, y)
                }

                drawCircle(color = CyberCyan, radius = 4f, center = Offset(x, y))
            }

            fillPath.lineTo(w, h)
            fillPath.close()

            // Fill gradient under curve
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(CyberCyan.copy(alpha = 0.25f), Color.Transparent)
                )
            )

            // Stroke line
            drawPath(
                path = path,
                color = CyberCyan,
                style = Stroke(width = 3.5f, cap = StrokeCap.Round)
            )
        }
    }
}

@Composable
fun SessionHistoryItem(session: TrainingSessionEntity) {
    val dateStr = remember(session.timestamp) {
        val sdf = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
        sdf.format(Date(session.timestamp))
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, DarkCardBorder, RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = session.drillType.replace("_", " "),
                        style = MaterialTheme.typography.titleSmall,
                        color = CyberCyan,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = dateStr,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row {
                    Text(
                        text = "Acc: ${session.accuracy.toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (session.accuracy >= 75) MatrixGreen else TextSecondary
                    )
                    Text(" • ", color = TextMuted)
                    Text(
                        text = "Headshots: ${session.headshots}",
                        style = MaterialTheme.typography.bodySmall,
                        color = HeadshotRed
                    )
                    Text(" • ", color = TextMuted)
                    Text(
                        text = "Hits: ${session.hits}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${session.score} PTS",
                    style = MaterialTheme.typography.titleMedium,
                    color = GoldenRank,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "${session.durationSeconds}s",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
            }
        }
    }
}
