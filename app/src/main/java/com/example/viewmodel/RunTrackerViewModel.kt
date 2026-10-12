package com.example.viewmodel

import android.app.Application
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.RunDatabase
import com.example.data.model.KmSplit
import com.example.data.model.RunPoint
import com.example.data.model.RunRecord
import com.example.data.repository.RunRepository
import com.example.tracker.LocationTracker
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class TrackingStatus {
    IDLE, RUNNING, PAUSED, FINISHED
}

data class TrackerUiState(
    val status: TrackingStatus = TrackingStatus.IDLE,
    val distanceMeters: Double = 0.0,
    val durationSeconds: Long = 0L,
    val currentSpeedKmh: Double = 0.0,
    val avgPaceSeconds: Int = 0,
    val currentPaceSeconds: Int = 0,
    val calories: Int = 0,
    val estimatedCadenceSpm: Int = 0,
    val points: List<RunPoint> = emptyList(),
    val splits: List<KmSplit> = emptyList(),
    val targetDistanceMeters: Double? = null,
    val isSimulated: Boolean = false,
    val hasReachedTarget: Boolean = false,
    val startTimeMillis: Long = 0L,
    val isGpsActive: Boolean = false
)

class RunTrackerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: RunRepository
    private val locationTracker: LocationTracker

    private val _uiState = MutableStateFlow(TrackerUiState())
    val uiState: StateFlow<TrackerUiState> = _uiState.asStateFlow()

    private val _finishedRun = MutableStateFlow<RunRecord?>(null)
    val finishedRun: StateFlow<RunRecord?> = _finishedRun.asStateFlow()

    private var timerJob: Job? = null
    private var lastKmSplitDistance = 0.0
    private var lastKmSplitTimeSeconds = 0L
    private var maxRecordedSpeedKmh = 0.0

    init {
        val db = RunDatabase.getDatabase(application)
        repository = RunRepository(db.runDao())

        locationTracker = LocationTracker(application) { newPoint ->
            processNewLocation(newPoint)
        }
    }

    fun setTargetDistance(meters: Double?) {
        if (_uiState.value.status == TrackingStatus.IDLE) {
            _uiState.update { it.copy(targetDistanceMeters = meters) }
        }
    }

    fun toggleSimulatedMode(enabled: Boolean) {
        if (_uiState.value.status == TrackingStatus.IDLE) {
            _uiState.update { it.copy(isSimulated = enabled) }
        }
    }

    fun startRun() {
        if (_uiState.value.status != TrackingStatus.IDLE) return

        val now = System.currentTimeMillis()
        lastKmSplitDistance = 0.0
        lastKmSplitTimeSeconds = 0L
        maxRecordedSpeedKmh = 0.0

        _uiState.update {
            TrackerUiState(
                status = TrackingStatus.RUNNING,
                startTimeMillis = now,
                targetDistanceMeters = it.targetDistanceMeters,
                isSimulated = it.isSimulated,
                isGpsActive = !it.isSimulated && locationTracker.isGpsEnabled()
            )
        }

        locationTracker.start(simulated = _uiState.value.isSimulated)
        startTimer()
    }

    fun pauseRun() {
        if (_uiState.value.status == TrackingStatus.RUNNING) {
            timerJob?.cancel()
            _uiState.update { it.copy(status = TrackingStatus.PAUSED, currentSpeedKmh = 0.0) }
        }
    }

    fun resumeRun() {
        if (_uiState.value.status == TrackingStatus.PAUSED) {
            _uiState.update { it.copy(status = TrackingStatus.RUNNING) }
            startTimer()
        }
    }

    fun finishRun() {
        if (_uiState.value.status == TrackingStatus.IDLE) return

        timerJob?.cancel()
        locationTracker.stop()

        val state = _uiState.value
        val now = System.currentTimeMillis()

        // Generate final kilometer split if there is a remainder distance > 100m
        val finalSplits = state.splits.toMutableList()
        val remainderDistance = state.distanceMeters - lastKmSplitDistance
        if (remainderDistance >= 200) {
            val partialTime = state.durationSeconds - lastKmSplitTimeSeconds
            val partialPace = if (remainderDistance > 0) {
                ((partialTime / (remainderDistance / 1000.0))).toInt()
            } else 0
            finalSplits.add(
                KmSplit(
                    kilometer = finalSplits.size + 1,
                    durationSeconds = partialTime,
                    paceSeconds = partialPace
                )
            )
        }

        val completedRecord = RunRecord(
            title = generateRunTitle(state.startTimeMillis),
            startTime = state.startTimeMillis,
            endTime = now,
            distanceMeters = state.distanceMeters,
            durationSeconds = state.durationSeconds,
            calories = state.calories,
            avgPaceSeconds = state.avgPaceSeconds,
            maxSpeedKmh = maxRecordedSpeedKmh,
            targetDistanceMeters = state.targetDistanceMeters,
            pointsJson = RunRecord.encodePoints(state.points),
            splitsJson = RunRecord.encodeSplits(finalSplits)
        )

        _finishedRun.value = completedRecord
        _uiState.update { it.copy(status = TrackingStatus.FINISHED, splits = finalSplits) }
    }

    fun saveRun(title: String, mood: String, notes: String, onSaved: () -> Unit) {
        val record = _finishedRun.value ?: return
        val updated = record.copy(
            title = if (title.isNotBlank()) title else record.title,
            mood = mood,
            notes = notes
        )

        viewModelScope.launch {
            repository.insertRun(updated)
            discardFinishedRun()
            onSaved()
        }
    }

    fun discardFinishedRun() {
        _finishedRun.value = null
        _uiState.update {
            TrackerUiState(
                status = TrackingStatus.IDLE,
                targetDistanceMeters = it.targetDistanceMeters,
                isSimulated = it.isSimulated
            )
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000L)
                if (_uiState.value.status == TrackingStatus.RUNNING) {
                    val newDuration = _uiState.value.durationSeconds + 1

                    if (_uiState.value.isSimulated) {
                        locationTracker.simulateTick()
                    }

                    // Calculate estimated cadence when running (~150-175 spm)
                    val cadence = if (_uiState.value.currentSpeedKmh > 4.0) {
                        150 + ((_uiState.value.currentSpeedKmh - 4.0) * 3).toInt().coerceIn(0, 30)
                    } else 0

                    _uiState.update { current ->
                        val distanceKm = current.distanceMeters / 1000.0
                        val avgPace = if (distanceKm > 0.05) {
                            (newDuration / distanceKm).toInt()
                        } else 0

                        current.copy(
                            durationSeconds = newDuration,
                            avgPaceSeconds = avgPace,
                            estimatedCadenceSpm = cadence
                        )
                    }
                }
            }
        }
    }

    private fun processNewLocation(newPoint: RunPoint) {
        if (_uiState.value.status != TrackingStatus.RUNNING) return

        val state = _uiState.value
        val points = state.points.toMutableList()
        var addedDistance = 0.0

        if (points.isNotEmpty()) {
            val lastPoint = points.last()
            val dist = FloatArray(1)
            Location.distanceBetween(
                lastPoint.latitude, lastPoint.longitude,
                newPoint.latitude, newPoint.longitude,
                dist
            )
            // Filter jitter
            if (dist[0] in 0.5f..50.0f) {
                addedDistance = dist[0].toDouble()
            }
        } else {
            // First point
            addedDistance = 0.0
        }

        points.add(newPoint)
        val newTotalDistance = state.distanceMeters + addedDistance
        if (newPoint.speedKmh > maxRecordedSpeedKmh && newPoint.speedKmh < 35.0) {
            maxRecordedSpeedKmh = newPoint.speedKmh
        }

        // Calories calculated based on distance & pace (approx 62 kcal per km)
        val newCalories = ((newTotalDistance / 1000.0) * 62.0).toInt()

        // Calculate current pace in sec/km
        val currentPace = if (newPoint.speedKmh > 1.5) {
            ((3600.0 / newPoint.speedKmh)).toInt().coerceAtMost(1800)
        } else 0

        // Check if a full kilometer boundary has been crossed
        val splits = state.splits.toMutableList()
        val nextKmThreshold = (splits.size + 1) * 1000.0
        if (newTotalDistance >= nextKmThreshold) {
            val splitDuration = state.durationSeconds - lastKmSplitTimeSeconds
            val splitPace = splitDuration.toInt() // exactly 1 km
            splits.add(
                KmSplit(
                    kilometer = splits.size + 1,
                    durationSeconds = splitDuration,
                    paceSeconds = splitPace
                )
            )
            lastKmSplitDistance = nextKmThreshold
            lastKmSplitTimeSeconds = state.durationSeconds
        }

        // Check Target reached
        val target = state.targetDistanceMeters
        val reachedTarget = target != null && newTotalDistance >= target && !state.hasReachedTarget

        _uiState.update { current ->
            current.copy(
                distanceMeters = newTotalDistance,
                currentSpeedKmh = newPoint.speedKmh,
                currentPaceSeconds = currentPace,
                calories = newCalories,
                points = points,
                splits = splits,
                hasReachedTarget = current.hasReachedTarget || reachedTarget
            )
        }
    }

    private fun generateRunTitle(timeMillis: Long): String {
        val calendar = java.util.Calendar.getInstance().apply {
            timeInMillis = timeMillis
        }
        val hour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
        val timeOfDay = when (hour) {
            in 4..10 -> "Lari Pagi"
            in 11..14 -> "Lari Siang"
            in 15..18 -> "Lari Sore"
            else -> "Lari Malam"
        }
        val dateFormat = java.text.SimpleDateFormat("d MMM", java.util.Locale("id", "ID"))
        return "$timeOfDay • ${dateFormat.format(calendar.time)}"
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        locationTracker.stop()
    }
}
