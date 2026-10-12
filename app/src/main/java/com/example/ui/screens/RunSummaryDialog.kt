package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.KmSplit
import com.example.data.model.RunRecord
import com.example.ui.theme.DarkOutline
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.SportCyan
import com.example.ui.theme.SportOrange
import com.example.ui.theme.SportRed
import com.example.ui.theme.SportVolt
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.RunFormatters

@Composable
fun RunSummaryDialog(
    run: RunRecord,
    onSave: (title: String, mood: String, notes: String) -> Unit,
    onDiscard: () -> Unit
) {
    var title by remember { mutableStateOf(run.title) }
    var mood by remember { mutableStateOf("Hebat") }
    var notes by remember { mutableStateOf("") }

    val splits = remember(run) { run.getSplits() }
    val isTargetMet = run.targetDistanceMeters != null && run.distanceMeters >= run.targetDistanceMeters

    Dialog(
        onDismissRequest = { /* Require explicit action */ },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, DarkOutline, RoundedCornerShape(24.dp)),
            color = DarkSurface
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header badge
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(if (isTargetMet) SportVolt.copy(alpha = 0.2f) else SportCyan.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isTargetMet) Icons.Default.EmojiEvents else Icons.Default.DirectionsRun,
                        contentDescription = null,
                        tint = if (isTargetMet) SportVolt else SportCyan,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = if (isTargetMet) "Target Jarak Tercapai! 🏆" else "Lari Selesai! 🎉",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = RunFormatters.formatDateTime(run.startTime),
                    fontSize = 12.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Big Distance Hero
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "TOTAL JARAK",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SportVolt,
                            letterSpacing = 1.sp
                        )
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = RunFormatters.formatDistanceKm(run.distanceMeters),
                                fontSize = 42.sp,
                                fontWeight = FontWeight.Black,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "KM",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = SportVolt,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }

                        if (run.targetDistanceMeters != null) {
                            val targetKm = run.targetDistanceMeters / 1000.0
                            Text(
                                text = "Target: ${String.format(java.util.Locale.US, "%.1f", targetKm)} KM (${((run.distanceMeters / run.targetDistanceMeters) * 100).toInt()}%)",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 3 Stats Columns: Waktu, Pace, Kalori
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SummaryMetricCard(
                        icon = Icons.Default.Timer,
                        label = "Waktu",
                        value = RunFormatters.formatDuration(run.durationSeconds),
                        tint = SportCyan,
                        modifier = Modifier.weight(1f)
                    )
                    SummaryMetricCard(
                        icon = Icons.Default.Speed,
                        label = "Pace Rata-rata",
                        value = "${RunFormatters.formatPace(run.avgPaceSeconds)}/km",
                        tint = SportVolt,
                        modifier = Modifier.weight(1f)
                    )
                    SummaryMetricCard(
                        icon = Icons.Default.LocalFireDepartment,
                        label = "Kalori",
                        value = "${run.calories} kcal",
                        tint = SportOrange,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Splits Breakdown
                if (splits.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Split Kilometer",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val minPace = splits.minOfOrNull { it.paceSeconds } ?: 300
                    val maxPace = (splits.maxOfOrNull { it.paceSeconds } ?: 400).coerceAtLeast(minPace + 30)

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkSurfaceVariant)
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        splits.forEach { split ->
                            SplitRow(split = split, minPace = minPace, maxPace = maxPace)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Title Input
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Judul Sesi Lari") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("run_title_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SportVolt,
                        unfocusedBorderColor = DarkOutline,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Mood selector
                Text(
                    text = "Kondisi / Rasa Lari:",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val moodOptions = listOf("😊 Hebat", "👍 Nyaman", "💦 Berat", "😴 Lelah")
                    moodOptions.forEach { option ->
                        val isSelected = mood == option.substring(3)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) SportVolt.copy(alpha = 0.2f) else DarkSurfaceVariant)
                                .border(
                                    1.dp,
                                    if (isSelected) SportVolt else DarkOutline,
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable { mood = option.substring(3) }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = option,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) SportVolt else TextSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Notes Input
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Catatan / Cuaca / Jalur") },
                    maxLines = 2,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("run_notes_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SportVolt,
                        unfocusedBorderColor = DarkOutline,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDiscard,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("discard_run_button"),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SportRed.copy(alpha = 0.6f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SportRed)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Buang", fontSize = 13.sp)
                    }

                    Button(
                        onClick = { onSave(title, mood, notes) },
                        modifier = Modifier
                            .weight(2f)
                            .height(48.dp)
                            .testTag("save_run_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SportVolt,
                            contentColor = Color(0xFF0F172A)
                        )
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Simpan Sesi Lari", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryMetricCard(
    icon: ImageVector,
    label: String,
    value: String,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = label, fontSize = 10.sp, color = TextMuted)
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun SplitRow(
    split: KmSplit,
    minPace: Int,
    maxPace: Int
) {
    val isFastest = split.paceSeconds == minPace
    val paceRatio = ((split.paceSeconds - minPace).toFloat() / (maxPace - minPace).coerceAtLeast(1).toFloat())
        .coerceIn(0f, 1f)

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Km ${split.kilometer}",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (isFastest) SportVolt else TextPrimary,
            modifier = Modifier.width(44.dp)
        )

        // Visual pace bar
        Box(
            modifier = Modifier
                .weight(1f)
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0x22FFFFFF))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.3f + (0.7f * (1f - paceRatio * 0.7f)))
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (isFastest) SportVolt else SportCyan)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = RunFormatters.formatPace(split.paceSeconds),
            fontSize = 12.sp,
            fontWeight = if (isFastest) FontWeight.Bold else FontWeight.Normal,
            color = if (isFastest) SportVolt else TextSecondary,
            modifier = Modifier.width(48.dp),
            textAlign = TextAlign.End
        )
    }
}
