package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.DarkOutline
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.SportCyan
import com.example.ui.theme.SportOrange
import com.example.ui.theme.SportVolt
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.RunFormatters
import com.example.viewmodel.HistoryViewModel
import com.example.viewmodel.RunBadge

@Composable
fun StatsAndGoalsScreen(
    viewModel: HistoryViewModel,
    modifier: Modifier = Modifier
) {
    val runs by viewModel.allRuns.collectAsStateWithLifecycle()
    val weeklyTargetKm by viewModel.weeklyTargetKm.collectAsStateWithLifecycle()

    val totalMeters = remember(runs) { runs.sumOf { it.distanceMeters } }
    val totalSeconds = remember(runs) { runs.sumOf { it.durationSeconds } }
    val totalCalories = remember(runs) { runs.sumOf { it.calories } }
    val totalRunsCount = runs.size

    val weeklyKm = remember(runs) { viewModel.getWeeklyDistanceKm(runs) }
    val monthlyKm = remember(runs) { viewModel.getMonthlyDistanceKm(runs) }

    // Personal Bests
    val longestRunMeters = remember(runs) { runs.maxOfOrNull { it.distanceMeters } ?: 0.0 }
    val fastestPaceSec = remember(runs) {
        runs.filter { it.distanceMeters >= 1000 && it.avgPaceSeconds > 0 }
            .minOfOrNull { it.avgPaceSeconds } ?: 0
    }
    val longestDurationSec = remember(runs) { runs.maxOfOrNull { it.durationSeconds } ?: 0L }

    val badges = remember(runs) { viewModel.computeBadges(runs) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(com.example.ui.theme.DarkBackground)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "STATISTIK & TARGET JARAK",
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            color = TextPrimary,
            letterSpacing = 1.sp
        )
        Text(
            text = "Pantau perkembangan jarak tempuh dan pencapaian lari",
            fontSize = 12.sp,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Target Mingguan Card
        val weeklyProgress = (weeklyKm / weeklyTargetKm).toFloat().coerceIn(0f, 1f)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("weekly_target_card"),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkOutline)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(SportVolt.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = SportVolt, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("TARGET MINGGU INI", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SportVolt, letterSpacing = 1.sp)
                            Text("${String.format(java.util.Locale.US, "%.1f", weeklyKm)} / ${String.format(java.util.Locale.US, "%.0f", weeklyTargetKm)} KM", fontSize = 18.sp, fontWeight = FontWeight.Black, color = TextPrimary)
                        }
                    }

                    Text(
                        text = "${(weeklyProgress * 100).toInt()}%",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = if (weeklyProgress >= 1f) SportVolt else SportCyan
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                LinearProgressIndicator(
                    progress = { weeklyProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = if (weeklyProgress >= 1f) SportVolt else SportCyan,
                    trackColor = DarkSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Target selector chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Ubah Target:", fontSize = 11.sp, color = TextMuted)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(10.0, 15.0, 20.0, 30.0).forEach { km ->
                            val isSelected = weeklyTargetKm == km
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) SportVolt else DarkSurfaceVariant)
                                    .clickable { viewModel.setWeeklyTarget(km) }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "${km.toInt()}K",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color(0xFF0F172A) else TextPrimary
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Total Summary Stats
        Text(
            text = "AKUMULASI TOTAL",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextSecondary,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TotalStatCard(
                label = "Total Jarak",
                value = "${RunFormatters.formatDistanceKm(totalMeters)} KM",
                subtext = "Bulan ini: ${String.format(java.util.Locale.US, "%.1f", monthlyKm)} km",
                accent = SportVolt,
                modifier = Modifier.weight(1f)
            )
            TotalStatCard(
                label = "Total Durasi",
                value = RunFormatters.formatDuration(totalSeconds),
                subtext = "$totalRunsCount sesi lari",
                accent = SportCyan,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TotalStatCard(
                label = "Total Kalori",
                value = "$totalCalories kcal",
                subtext = "Energi terbakar",
                accent = SportOrange,
                modifier = Modifier.weight(1f)
            )
            TotalStatCard(
                label = "Total Lari",
                value = "$totalRunsCount Sesi",
                subtext = "Konsistensi lari",
                accent = Color(0xFFA855F7),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Rekor Pribadi (Personal Bests)
        Text(
            text = "REKOR PRIBADI (PERSONAL BEST)",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextSecondary,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkOutline)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                PbRow(
                    icon = Icons.Default.DirectionsRun,
                    title = "Jarak Terjauh",
                    value = if (longestRunMeters > 0) "${RunFormatters.formatDistanceKm(longestRunMeters)} KM" else "-- KM",
                    accent = SportVolt
                )
                PbRow(
                    icon = Icons.Default.Speed,
                    title = "Pace Rata-rata Tercepat",
                    value = if (fastestPaceSec > 0) "${RunFormatters.formatPace(fastestPaceSec)}/km" else "--/km",
                    accent = SportCyan
                )
                PbRow(
                    icon = Icons.Default.Timer,
                    title = "Durasi Terlama",
                    value = if (longestDurationSec > 0) RunFormatters.formatDuration(longestDurationSec) else "--:--",
                    accent = SportOrange
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Lencana Prestasi (Badges)
        Text(
            text = "LENCANA PENCAPAIAN JARAK",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextSecondary,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            badges.forEach { badge ->
                BadgeCard(badge = badge)
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
private fun TotalStatCard(
    label: String,
    value: String,
    subtext: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkOutline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Text(text = label, fontSize = 11.sp, color = TextMuted)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, fontSize = 17.sp, fontWeight = FontWeight.Black, color = accent)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = subtext, fontSize = 11.sp, color = TextSecondary)
        }
    }
}

@Composable
private fun PbRow(
    icon: ImageVector,
    title: String,
    value: String,
    accent: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = accent, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Text(text = title, fontSize = 13.sp, color = TextPrimary)
        }
        Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = accent)
    }
}

@Composable
private fun BadgeCard(badge: RunBadge) {
    val icon = when (badge.iconName) {
        "flag" -> Icons.Default.Flag
        "military_tech" -> Icons.Default.MilitaryTech
        "emoji_events" -> Icons.Default.EmojiEvents
        "workspace_premium" -> Icons.Default.WorkspacePremium
        "bolt" -> Icons.Default.Bolt
        else -> Icons.Default.Stars
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (badge.isUnlocked) DarkSurface else DarkSurface.copy(alpha = 0.6f)
        ),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (badge.isUnlocked) SportVolt.copy(alpha = 0.5f) else DarkOutline
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        if (badge.isUnlocked) SportVolt.copy(alpha = 0.2f) else DarkSurfaceVariant
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (badge.isUnlocked) SportVolt else TextMuted,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = badge.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (badge.isUnlocked) TextPrimary else TextMuted
                    )
                    if (badge.isUnlocked) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = SportVolt,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                Text(
                    text = badge.description,
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }

            Text(
                text = badge.progressText,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (badge.isUnlocked) SportVolt else TextMuted
            )
        }
    }
}
