package com.example.data.repository

import com.example.data.db.RunDao
import com.example.data.model.RunRecord
import kotlinx.coroutines.flow.Flow

class RunRepository(private val runDao: RunDao) {
    val allRuns: Flow<List<RunRecord>> = runDao.getAllRuns()
    val totalDistanceMeters: Flow<Double?> = runDao.getTotalDistanceMeters()
    val totalDurationSeconds: Flow<Long?> = runDao.getTotalDurationSeconds()
    val totalCalories: Flow<Int?> = runDao.getTotalCalories()
    val totalRunsCount: Flow<Int> = runDao.getTotalRunsCount()
    val longestDistanceMeters: Flow<Double?> = runDao.getLongestDistanceMeters()
    val fastestPaceSeconds: Flow<Int?> = runDao.getFastestPaceSeconds()

    fun getRunById(id: Long): Flow<RunRecord?> = runDao.getRunById(id)

    suspend fun insertRun(run: RunRecord): Long = runDao.insertRun(run)

    suspend fun deleteRunById(id: Long) = runDao.deleteRunById(id)
}
