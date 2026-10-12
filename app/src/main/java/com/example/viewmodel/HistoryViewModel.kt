package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.RunDatabase
import com.example.data.model.RunRecord
import com.example.data.repository.RunRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

data class RunBadge(
    val id: String,
    val title: String,
    val description: String,
    val iconName: String,
    val isUnlocked: Boolean,
    val progressText: String
)

data class WeeklyGoal(
    val targetKm: Double = 15.0,
    val currentKm: Double = 0.0
)

class HistoryViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: RunRepository
    val allRuns: StateFlow<List<RunRecord>>

    private val _selectedRun = MutableStateFlow<RunRecord?>(null)
    val selectedRun: StateFlow<RunRecord?> = _selectedRun.asStateFlow()

    private val _weeklyTargetKm = MutableStateFlow(15.0)
    val weeklyTargetKm: StateFlow<Double> = _weeklyTargetKm.asStateFlow()

    init {
        val db = RunDatabase.getDatabase(application)
        repository = RunRepository(db.runDao())
        allRuns = repository.allRuns.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    fun selectRun(run: RunRecord?) {
        _selectedRun.value = run
    }

    fun deleteRun(id: Long) {
        viewModelScope.launch {
            repository.deleteRunById(id)
            if (_selectedRun.value?.id == id) {
                _selectedRun.value = null
            }
        }
    }

    fun setWeeklyTarget(km: Double) {
        _weeklyTargetKm.value = km
    }

    fun getWeeklyDistanceKm(runs: List<RunRecord>): Double {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val startOfWeek = cal.timeInMillis

        val weeklyRuns = runs.filter { it.startTime >= startOfWeek }
        val sumMeters = weeklyRuns.sumOf { it.distanceMeters }
        return sumMeters / 1000.0
    }

    fun getMonthlyDistanceKm(runs: List<RunRecord>): Double {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val startOfMonth = cal.timeInMillis

        val monthlyRuns = runs.filter { it.startTime >= startOfMonth }
        val sumMeters = monthlyRuns.sumOf { it.distanceMeters }
        return sumMeters / 1000.0
    }

    fun computeBadges(runs: List<RunRecord>): List<RunBadge> {
        val totalRuns = runs.size
        val maxDistKm = (runs.maxOfOrNull { it.distanceMeters } ?: 0.0) / 1000.0
        val fastestPace = runs.filter { it.distanceMeters >= 1000 && it.avgPaceSeconds > 0 }
            .minOfOrNull { it.avgPaceSeconds } ?: 9999
        val totalKm = runs.sumOf { it.distanceMeters } / 1000.0

        return listOf(
            RunBadge(
                id = "first_run",
                title = "Langkah Perdana",
                description = "Menyelesaikan sesi lari pertama",
                iconName = "flag",
                isUnlocked = totalRuns >= 1,
                progressText = if (totalRuns >= 1) "Tercapai!" else "$totalRuns/1 Lari"
            ),
            RunBadge(
                id = "finisher_5k",
                title = "Pelari 5K",
                description = "Lari sejauh 5 KM dalam satu sesi",
                iconName = "military_tech",
                isUnlocked = maxDistKm >= 5.0,
                progressText = String.format(java.util.Locale.US, "%.1f / 5.0 km", maxDistKm.coerceAtMost(5.0))
            ),
            RunBadge(
                id = "finisher_10k",
                title = "Penakluk 10K",
                description = "Lari sejauh 10 KM dalam satu sesi",
                iconName = "emoji_events",
                isUnlocked = maxDistKm >= 10.0,
                progressText = String.format(java.util.Locale.US, "%.1f / 10.0 km", maxDistKm.coerceAtMost(10.0))
            ),
            RunBadge(
                id = "half_marathon",
                title = "Half Marathoner",
                description = "Menempuh jarak 21.1 KM",
                iconName = "workspace_premium",
                isUnlocked = maxDistKm >= 21.1,
                progressText = String.format(java.util.Locale.US, "%.1f / 21.1 km", maxDistKm.coerceAtMost(21.1))
            ),
            RunBadge(
                id = "speed_demon",
                title = "Kilat Kecepatan",
                description = "Pace lari rata-rata di bawah 5'00\" /km",
                iconName = "bolt",
                isUnlocked = fastestPace < 300,
                progressText = if (fastestPace < 300) "Pace ${fastestPace / 60}'${fastestPace % 60}\"" else "Pace < 5'00\""
            ),
            RunBadge(
                id = "marathon_club_50",
                title = "Akumulasi 50 KM",
                description = "Total akumulasi jarak lari mencapai 50 KM",
                iconName = "stars",
                isUnlocked = totalKm >= 50.0,
                progressText = String.format(java.util.Locale.US, "%.1f / 50.0 km", totalKm.coerceAtMost(50.0))
            )
        )
    }
}
