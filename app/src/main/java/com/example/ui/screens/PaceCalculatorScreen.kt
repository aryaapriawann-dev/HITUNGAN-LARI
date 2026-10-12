package com.example.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

@Composable
fun PaceCalculatorScreen(modifier: Modifier = Modifier) {
    var selectedDistanceKm by remember { mutableStateOf(5.0) }
    var customDistanceInput by remember { mutableStateOf("5.0") }
    var targetMinutesInput by remember { mutableStateOf("28") }
    var targetSecondsInput by remember { mutableStateOf("30") }

    // Reverse calc states
    var paceMinInput by remember { mutableStateOf("5") }
    var paceSecInput by remember { mutableStateOf("42") }

    var activeTab by remember { mutableIntStateOf(0) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(com.example.ui.theme.DarkBackground)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "KALKULATOR PACE & JARAK",
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            color = TextPrimary,
            letterSpacing = 1.sp
        )
        Text(
            text = "Hitung target waktu finish dan kebutuhan pace per kilometer",
            fontSize = 12.sp,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Distance presets selector
        Text(
            text = "JARAK LARI",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextSecondary,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(1.0 to "1 KM", 3.0 to "3 KM", 5.0 to "5K", 10.0 to "10K", 21.0975 to "21.1K (Half)", 42.195 to "42.2K (Full)").forEach { (km, label) ->
                val isSelected = selectedDistanceKm == km
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) SportVolt else DarkSurfaceVariant)
                        .border(1.dp, if (isSelected) SportVolt else DarkOutline, RoundedCornerShape(12.dp))
                        .clickable {
                            selectedDistanceKm = km
                            customDistanceInput = String.format(java.util.Locale.US, "%.1f", km)
                        }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = label,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color(0xFF0F172A) else TextPrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Mode Tab: Hitung Pace atau Hitung Waktu
        TabRow(
            selectedTabIndex = activeTab,
            containerColor = DarkSurface,
            contentColor = SportVolt,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[activeTab]),
                    color = SportVolt
                )
            }
        ) {
            Tab(
                selected = activeTab == 0,
                onClick = { activeTab = 0 },
                text = { Text("Hitung Kebutuhan Pace", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = activeTab == 1,
                onClick = { activeTab = 1 },
                text = { Text("Prediksi Waktu Finish", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (activeTab == 0) {
            // Calculate Required Pace given Target Time
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkOutline)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Target Waktu yang Diinginkan:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = targetMinutesInput,
                            onValueChange = { targetMinutesInput = it },
                            label = { Text("Menit") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f).testTag("calc_target_minutes"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SportVolt,
                                unfocusedBorderColor = DarkOutline
                            )
                        )
                        OutlinedTextField(
                            value = targetSecondsInput,
                            onValueChange = { targetSecondsInput = it },
                            label = { Text("Detik") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f).testTag("calc_target_seconds"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SportVolt,
                                unfocusedBorderColor = DarkOutline
                            )
                        )
                    }

                    val totalTargetSeconds = (targetMinutesInput.toIntOrNull() ?: 0) * 60 +
                            (targetSecondsInput.toIntOrNull() ?: 0)

                    val requiredPaceSec = if (selectedDistanceKm > 0 && totalTargetSeconds > 0) {
                        (totalTargetSeconds / selectedDistanceKm).toInt()
                    } else 0

                    val speedKmh = if (requiredPaceSec > 0) {
                        3600.0 / requiredPaceSec
                    } else 0.0

                    Spacer(modifier = Modifier.height(16.dp))

                    // Result Display
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkSurfaceVariant)
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "PACE YANG HARUS DIJAGA",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SportVolt,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${RunFormatters.formatPace(requiredPaceSec)} /km",
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Black,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Kecepatan: ${String.format(java.util.Locale.US, "%.1f", speedKmh)} km/jam",
                                fontSize = 13.sp,
                                color = SportCyan
                            )
                        }
                    }
                }
            }
        } else {
            // Predict Finish Time given Pace
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkOutline)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Pace Lari Kamu (/KM):",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = paceMinInput,
                            onValueChange = { paceMinInput = it },
                            label = { Text("Menit") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f).testTag("calc_pace_minutes"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SportVolt,
                                unfocusedBorderColor = DarkOutline
                            )
                        )
                        OutlinedTextField(
                            value = paceSecInput,
                            onValueChange = { paceSecInput = it },
                            label = { Text("Detik") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f).testTag("calc_pace_seconds"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SportVolt,
                                unfocusedBorderColor = DarkOutline
                            )
                        )
                    }

                    val paceSeconds = (paceMinInput.toIntOrNull() ?: 0) * 60 +
                            (paceSecInput.toIntOrNull() ?: 0)

                    val projectedTotalSeconds = (paceSeconds * selectedDistanceKm).toLong()

                    Spacer(modifier = Modifier.height(16.dp))

                    // Result Display
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkSurfaceVariant)
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "ESTIMASI WAKTU FINISH",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SportVolt,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = RunFormatters.formatDuration(projectedTotalSeconds),
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Black,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Jarak: ${String.format(java.util.Locale.US, "%.1f", selectedDistanceKm)} KM",
                                fontSize = 13.sp,
                                color = SportCyan
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Running Tips & Zone Guide
        Text(
            text = "PANDUAN ZONA LARI JARAK JAUH",
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
                ZoneRow("Zona 2 (Easy / Aerobik)", "Pace santai, bisa sambil mengobrol. Membangun stamina lari jarak jauh.", SportCyan)
                ZoneRow("Zona 3 (Tempo)", "Pace stabil dan nyaman tapi tegas. Melatih efisiensi lari 5K & 10K.", SportVolt)
                ZoneRow("Zona 4 (Threshold)", "Pace cepat dan menantang. Menaikkan batas laktat.", SportOrange)
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
private fun ZoneRow(
    title: String,
    desc: String,
    accent: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .padding(top = 4.dp)
                .size(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(accent)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text(text = desc, fontSize = 11.sp, color = TextSecondary)
        }
    }
}
