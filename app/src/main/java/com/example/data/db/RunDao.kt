package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.RunRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface RunDao {
    @Query("SELECT * FROM runs ORDER BY startTime DESC")
    fun getAllRuns(): Flow<List<RunRecord>>

    @Query("SELECT * FROM runs WHERE id = :id")
    fun getRunById(id: Long): Flow<RunRecord?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRun(run: RunRecord): Long

    @Query("DELETE FROM runs WHERE id = :id")
    suspend fun deleteRunById(id: Long)

    @Query("SELECT SUM(distanceMeters) FROM runs")
    fun getTotalDistanceMeters(): Flow<Double?>

    @Query("SELECT SUM(durationSeconds) FROM runs")
    fun getTotalDurationSeconds(): Flow<Long?>

    @Query("SELECT SUM(calories) FROM runs")
    fun getTotalCalories(): Flow<Int?>

    @Query("SELECT COUNT(*) FROM runs")
    fun getTotalRunsCount(): Flow<Int>

    @Query("SELECT MAX(distanceMeters) FROM runs")
    fun getLongestDistanceMeters(): Flow<Double?>

    @Query("SELECT MIN(avgPaceSeconds) FROM runs WHERE avgPaceSeconds > 0 AND distanceMeters >= 1000")
    fun getFastestPaceSeconds(): Flow<Int?>
}
