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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.KmSplit
import com.example.data.model.RunRecord
import com.example.ui.components.RouteCanvas
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
import com.example.viewmodel.HistoryViewModel

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    modifier: Modifier = Modifier
) {
    val runs by viewModel.allRuns.collectAsStateWithLifecycle()
    val selectedRun by viewModel.selectedRun.collectAsStateWithLifecycle()

    var runToDelete by remember { mutableStateOf<RunRecord?>(null) }

    // Delete confirmation dialog
    if (runToDelete != null) {
        AlertDialog(
            onDismissRequest = { runToDelete = null },
            title = { Text("Hapus Sesi Lari?") },
            text = { Text("Data lari \"${runToDelete?.title}\" akan dihapus secara permanen.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        runToDelete?.let { viewModel.deleteRun(it.id) }
                        runToDelete = null
                    }
                ) {
                    Text("Hapus", color = SportRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { runToDelete = null }) {
                    Text("Batal", color = TextSecondary)
                }
            },
            containerColor = DarkSurface
        )
    }

    // Detail dialog when user taps a run item
    if (selectedRun != null) {
        RunDetailDialog(
            run = selectedRun!!,
            onDismiss = { viewModel.selectRun(null) },
            onDelete = {
                val r = selectedRun!!
                viewModel.selectRun(null)
                runToDelete = r
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(com.example.ui.theme.DarkBackground)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Screen Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "RIWAYAT LARI",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = TextPrimary,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "${runs.size} sesi tersimpan",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (runs.isEmpty()) {
            // Empty State
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 40.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(DarkSurfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsRun,
                            contentDescription = null,
                            tint = SportVolt,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Belum Ada Riwayat Lari",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Mulai sesi lari pertamamu di tab \"Lari\" untuk melacak jarak, rute, dan kecepatan!",
                        fontSize = 13.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("history_list"),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(runs, key = { it.id }) { run ->
                    RunItemCard(
                        run = run,
                        onClick = { viewModel.selectRun(run) },
                        onDeleteClick = { runToDelete = run }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(30.dp))
                }
            }
        }
    }
}

@Composable
private fun RunItemCard(
    run: RunRecord,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag("run_item_${run.id}"),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkOutline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Top Row: Title, Mood, Date, Delete Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = run.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = RunFormatters.formatDateTime(run.startTime),
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }

                if (run.mood.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkSurfaceVariant)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = run.mood,
                            fontSize = 11.sp,
                            color = SportVolt,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                }

                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Hapus",
                        tint = TextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Main stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Distance
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = RunFormatters.formatDistanceKm(run.distanceMeters),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = SportVolt
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "KM",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = SportVolt,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }

                // Sub stats
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "Waktu", fontSize = 10.sp, color = TextMuted)
                        Text(
                            text = RunFormatters.formatDuration(run.durationSeconds),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "Pace", fontSize = 10.sp, color = TextMuted)
                        Text(
                            text = "${RunFormatters.formatPace(run.avgPaceSeconds)}/km",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "Kalori", fontSize = 10.sp, color = TextMuted)
                        Text(
                            text = "${run.calories} kcal",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = SportOrange
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RunDetailDialog(
    run: RunRecord,
    onDismiss: () -> Unit,
    onDelete: () -> Unit
) {
    val points = remember(run) { run.getPoints() }
    val splits = remember(run) { run.getSplits() }

    Dialog(
        onDismissRequest = onDismiss,
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
                    .verticalScroll(rememberScrollState())
            ) {
                // Header with close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = run.title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text(text = RunFormatters.formatDateTime(run.startTime), fontSize = 11.sp, color = TextSecondary)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup", tint = TextPrimary)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Route Canvas preview
                RouteCanvas(
                    points = points,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    isLiveTracking = false
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Key metrics 4-grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DetailMetricItem("Jarak", "${RunFormatters.formatDistanceKm(run.distanceMeters)} KM", SportVolt, Modifier.weight(1f))
                    DetailMetricItem("Waktu", RunFormatters.formatDuration(run.durationSeconds), SportCyan, Modifier.weight(1f))
                    DetailMetricItem("Pace Rata-rata", "${RunFormatters.formatPace(run.avgPaceSeconds)}/km", SportVolt, Modifier.weight(1f))
                    DetailMetricItem("Kalori", "${run.calories} kcal", SportOrange, Modifier.weight(1f))
                }

                if (run.notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(text = "Catatan:", fontSize = 12.sp, color = TextSecondary)
                    Text(text = run.notes, fontSize = 13.sp, color = TextPrimary, modifier = Modifier.padding(top = 2.dp))
                }

                // Splits
                if (splits.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Rincian Split KM",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkSurfaceVariant)
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        splits.forEach { split ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Kilometer ${split.kilometer}", fontSize = 12.sp, color = TextPrimary)
                                Text(
                                    text = "Pace ${RunFormatters.formatPace(split.paceSeconds)}/km (${RunFormatters.formatDuration(split.durationSeconds)})",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = SportVolt
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Delete button
                TextButton(
                    onClick = onDelete,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = SportRed, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Hapus Riwayat Ini", color = SportRed, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun DetailMetricItem(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = label, fontSize = 9.sp, color = TextMuted, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )
        }
    }
}
