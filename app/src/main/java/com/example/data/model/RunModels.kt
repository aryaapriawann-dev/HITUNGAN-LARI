package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.json.JSONArray
import org.json.JSONObject

data class RunPoint(
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long = System.currentTimeMillis(),
    val speedKmh: Double = 0.0
)

data class KmSplit(
    val kilometer: Int,
    val durationSeconds: Long,
    val paceSeconds: Int
)

@Entity(tableName = "runs")
data class RunRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val startTime: Long,
    val endTime: Long,
    val distanceMeters: Double,
    val durationSeconds: Long,
    val calories: Int,
    val avgPaceSeconds: Int, // seconds per km
    val maxSpeedKmh: Double,
    val targetDistanceMeters: Double? = null,
    val pointsJson: String = "[]",
    val splitsJson: String = "[]",
    val mood: String = "Bagus",
    val notes: String = ""
) {
    fun getPoints(): List<RunPoint> {
        return try {
            val list = mutableListOf<RunPoint>()
            val array = JSONArray(pointsJson)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    RunPoint(
                        latitude = obj.getDouble("lat"),
                        longitude = obj.getDouble("lng"),
                        timestamp = obj.optLong("time", 0L),
                        speedKmh = obj.optDouble("spd", 0.0)
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun getSplits(): List<KmSplit> {
        return try {
            val list = mutableListOf<KmSplit>()
            val array = JSONArray(splitsJson)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    KmSplit(
                        kilometer = obj.getInt("km"),
                        durationSeconds = obj.getLong("dur"),
                        paceSeconds = obj.getInt("pace")
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    companion object {
        fun encodePoints(points: List<RunPoint>): String {
            val array = JSONArray()
            points.forEach { pt ->
                val obj = JSONObject().apply {
                    put("lat", pt.latitude)
                    put("lng", pt.longitude)
                    put("time", pt.timestamp)
                    put("spd", pt.speedKmh)
                }
                array.put(obj)
            }
            return array.toString()
        }

        fun encodeSplits(splits: List<KmSplit>): String {
            val array = JSONArray()
            splits.forEach { sp ->
                val obj = JSONObject().apply {
                    put("km", sp.kilometer)
                    put("dur", sp.durationSeconds)
                    put("pace", sp.paceSeconds)
                }
                array.put(obj)
            }
            return array.toString()
        }
    }
}
