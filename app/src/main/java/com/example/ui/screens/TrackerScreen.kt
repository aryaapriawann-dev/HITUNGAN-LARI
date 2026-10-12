package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.GpsOff
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import com.example.viewmodel.RunTrackerViewModel
import com.example.viewmodel.TrackingStatus

@Composable
fun TrackerScreen(
    viewModel: RunTrackerViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val finishedRun by viewModel.finishedRun.collectAsStateWithLifecycle()

    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    var showCustomGoalDialog by remember { mutableStateOf(false) }
    var customKmInput by remember { mutableStateOf("7.0") }
    var showFinishConfirmDialog by remember { mutableStateOf(false) }

    // Summary dialog when workout finishes
    if (finishedRun != null) {
        RunSummaryDialog(
            run = finishedRun!!,
            onSave = { title, mood, notes ->
                viewModel.saveRun(title, mood, notes) {
                    // Saved
                }
            },
            onDiscard = {
                viewModel.discardFinishedRun()
            }
        )
    }

    // Confirmation dialog before finishing
    if (showFinishConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showFinishConfirmDialog = false },
            title = { Text("Akhiri Sesi Lari?") },
            text = {
                Text("Total jarak yang sudah ditempuh adalah ${RunFormatters.formatDistanceKm(uiState.distanceMeters)} KM. Selesai dan simpan data lari ini?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showFinishConfirmDialog = false
                        viewModel.finishRun()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SportVolt, contentColor = Color(0xFF0F172A))
                ) {
                    Text("Ya, Selesai", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showFinishConfirmDialog = false }) {
                    Text("Lanjutkan Lari", color = TextSecondary)
                }
            },
            containerColor = DarkSurface
        )
    }

    // Custom Target Distance Dialog
    if (showCustomGoalDialog) {
        AlertDialog(
            onDismissRequest = { showCustomGoalDialog = false },
            title = { Text("Tentukan Target Jarak (KM)") },
            text = {
                Column {
                    Text("Berapa kilometer target lari kamu hari ini?", color = TextSecondary, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = customKmInput,
                        onValueChange = { customKmInput = it },
                        label = { Text("Jarak Target (KM)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SportVolt,
                            unfocusedBorderColor = DarkOutline
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val km = customKmInput.toDoubleOrNull()
                        if (km != null && km > 0) {
                            viewModel.setTargetDistance(km * 1000.0)
                        }
                        showCustomGoalDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SportVolt, contentColor = Color(0xFF0F172A))
                ) {
                    Text("Terapkan Target")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomGoalDialog = false }) {
                    Text("Batal", color = TextSecondary)
                }
            },
            containerColor = DarkSurface
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(com.example.ui.theme.DarkBackground)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Top Status & Simulation Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(
                            when (uiState.status) {
                                TrackingStatus.RUNNING -> SportVolt
                                TrackingStatus.PAUSED -> SportOrange
                                else -> SportCyan
                            }
                        )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = when (uiState.status) {
                        TrackingStatus.RUNNING -> "SEDANG BERLARI"
                        TrackingStatus.PAUSED -> "JEDA SEMENTARA"
                        else -> "SIAP BERLARI"
                    },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = when (uiState.status) {
                        TrackingStatus.RUNNING -> SportVolt
                        TrackingStatus.PAUSED -> SportOrange
                        else -> TextSecondary
                    }
                )
            }

            // Simulator toggle (Super convenient for testing in emulator)
            if (uiState.status == TrackingStatus.IDLE) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(DarkSurfaceVariant)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Demo Simulasi",
                        fontSize = 11.sp,
                        color = if (uiState.isSimulated) SportVolt else TextMuted
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Switch(
                        checked = uiState.isSimulated,
                        onCheckedChange = { viewModel.toggleSimulatedMode(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = SportVolt,
                            checkedTrackColor = Color(0xFF1E3A1A),
                            uncheckedThumbColor = TextMuted,
                            uncheckedTrackColor = DarkOutline
                        ),
                        modifier = Modifier
                            .height(24.dp)
                            .testTag("simulation_toggle")
                    )
                }
            } else if (uiState.isSimulated) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(SportVolt.copy(alpha = 0.2f))
                        .border(1.dp, SportVolt, RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Mode Simulasi Aktif",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SportVolt
                    )
                }
            }
        }

        // Location Permission Warning if not simulated
        if (!hasLocationPermission && !uiState.isSimulated) {
            Spacer(modifier = Modifier.height(10.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        permissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    },
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2D2012)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.GpsOff, contentDescription = null, tint = SportOrange)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Izin GPS Diperlukan", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                        Text("Ketuk untuk memberikan akses lokasi GPS atau aktifkan mode Demo Simulasi.", fontSize = 11.sp, color = TextSecondary)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Live Route Canvas
        RouteCanvas(
            points = uiState.points,
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp)
                .testTag("route_canvas"),
            isLiveTracking = uiState.status == TrackingStatus.RUNNING
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Distance Target Selector (Shown when IDLE)
        if (uiState.status == TrackingStatus.IDLE) {
            Text(
                text = "PILIH TARGET JARAK",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 1.sp,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GoalDistanceChip(
                    label = "Bebas",
                    isSelected = uiState.targetDistanceMeters == null,
                    onClick = { viewModel.setTargetDistance(null) }
                )
                GoalDistanceChip(
                    label = "1 KM",
                    isSelected = uiState.targetDistanceMeters == 1000.0,
                    onClick = { viewModel.setTargetDistance(1000.0) }
                )
                GoalDistanceChip(
                    label = "3 KM",
                    isSelected = uiState.targetDistanceMeters == 3000.0,
                    onClick = { viewModel.setTargetDistance(3000.0) }
                )
                GoalDistanceChip(
                    label = "5 KM",
                    isSelected = uiState.targetDistanceMeters == 5000.0,
                    onClick = { viewModel.setTargetDistance(5000.0) }
                )
                GoalDistanceChip(
                    label = "10 KM",
                    isSelected = uiState.targetDistanceMeters == 10000.0,
                    onClick = { viewModel.setTargetDistance(10000.0) }
                )
                GoalDistanceChip(
                    label = "21.1 KM",
                    isSelected = uiState.targetDistanceMeters == 21100.0,
                    onClick = { viewModel.setTargetDistance(21100.0) }
                )
                GoalDistanceChip(
                    label = "Kustom ✎",
                    isSelected = uiState.targetDistanceMeters != null &&
                            uiState.targetDistanceMeters !in listOf(1000.0, 3000.0, 5000.0, 10000.0, 21100.0),
                    onClick = { showCustomGoalDialog = true }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
        }

        // Hero Metric Card: Jarak Lari (Distance)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("distance_card"),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkOutline)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 18.dp, horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "JARAK TEMPUH",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = SportVolt,
                    letterSpacing = 1.5.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = RunFormatters.formatDistanceKm(uiState.distanceMeters),
                        fontSize = 58.sp,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary,
                        letterSpacing = (-1).sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "KM",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = SportVolt,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }

                // If target distance set, show progress indicator
                if (uiState.targetDistanceMeters != null) {
                    val targetMeters = uiState.targetDistanceMeters!!
                    val progress = (uiState.distanceMeters / targetMeters).toFloat().coerceIn(0f, 1f)
                    val remainingMeters = (targetMeters - uiState.distanceMeters).coerceAtLeast(0.0)

                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = if (uiState.hasReachedTarget) SportVolt else SportCyan,
                        trackColor = DarkSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (uiState.hasReachedTarget) "Target Selesai! (${(progress * 100).toInt()}%)" else "${(progress * 100).toInt()}% Selesai",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (uiState.hasReachedTarget) SportVolt else TextSecondary
                        )
                        Text(
                            text = if (remainingMeters <= 0) "0.00 KM tersisa" else "Sisa ${RunFormatters.formatDistanceKm(remainingMeters)} KM",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Secondary Metrics Grid: Waktu, Pace Rata-rata, Pace Sekarang, Kalori
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LiveMetricTile(
                icon = Icons.Default.Timer,
                label = "WAKTU",
                value = RunFormatters.formatDuration(uiState.durationSeconds),
                unit = "",
                accentColor = SportCyan,
                modifier = Modifier.weight(1f)
            )
            LiveMetricTile(
                icon = Icons.Default.Speed,
                label = "PACE RATA-RATA",
                value = RunFormatters.formatPace(uiState.avgPaceSeconds),
                unit = "/KM",
                accentColor = SportVolt,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LiveMetricTile(
                icon = Icons.Default.DirectionsRun,
                label = "PACE SEKARANG",
                value = RunFormatters.formatPace(uiState.currentPaceSeconds),
                unit = "/KM",
                accentColor = SportCyan,
                modifier = Modifier.weight(1f)
            )
            LiveMetricTile(
                icon = Icons.Default.LocalFireDepartment,
                label = "KALORI",
                value = "${uiState.calories}",
                unit = "KCAL",
                accentColor = SportOrange,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Control Buttons (Start, Pause, Resume, Stop)
        when (uiState.status) {
            TrackingStatus.IDLE -> {
                Button(
                    onClick = {
                        if (!hasLocationPermission && !uiState.isSimulated) {
                            permissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        } else {
                            viewModel.startRun()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp)
                        .testTag("start_run_button"),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SportVolt,
                        contentColor = Color(0xFF0F172A)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "MULAI LARI",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }
            }

            TrackingStatus.RUNNING -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Jeda / Pause Button
                    Button(
                        onClick = { viewModel.pauseRun() },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .testTag("pause_run_button"),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SportOrange,
                            contentColor = Color(0xFF0F172A)
                        )
                    ) {
                        Icon(Icons.Default.Pause, contentDescription = null, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("JEDA", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

                    // Selesai / Finish Button
                    Button(
                        onClick = { showFinishConfirmDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .testTag("finish_run_button"),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SportRed,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("SELESAI", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }

            TrackingStatus.PAUSED -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Lanjut / Resume Button
                    Button(
                        onClick = { viewModel.resumeRun() },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .testTag("resume_run_button"),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SportVolt,
                            contentColor = Color(0xFF0F172A)
                        )
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("LANJUT", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

                    // Selesai / Finish Button
                    Button(
                        onClick = { showFinishConfirmDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .testTag("finish_paused_run_button"),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SportRed,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("SELESAI", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }

            TrackingStatus.FINISHED -> {
                // Handled by summary dialog
            }
        }

        Spacer(modifier = Modifier.height(28.dp))
    }
}

@Composable
private fun GoalDistanceChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) SportVolt else DarkSurfaceVariant)
            .border(1.dp, if (isSelected) SportVolt else DarkOutline, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color(0xFF0F172A) else TextPrimary
        )
    }
}

@Composable
private fun LiveMetricTile(
    icon: ImageVector,
    label: String,
    value: String,
    unit: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkOutline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = label,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    letterSpacing = 0.5.sp
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = TextPrimary
                )
                if (unit.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = unit,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
            }
        }
    }
}
