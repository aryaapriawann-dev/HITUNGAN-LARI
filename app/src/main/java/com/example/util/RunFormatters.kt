package com.example.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object RunFormatters {
    fun formatDistanceKm(meters: Double): String {
        val km = meters / 1000.0
        return String.format(Locale.US, "%.2f", km)
    }

    fun formatDuration(totalSeconds: Long): String {
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return if (hours > 0) {
            String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format(Locale.US, "%02d:%02d", minutes, seconds)
        }
    }

    fun formatPace(paceSeconds: Int): String {
        if (paceSeconds <= 0 || paceSeconds > 3600) return "--'--\""
        val minutes = paceSeconds / 60
        val seconds = paceSeconds % 60
        return String.format(Locale.US, "%d'%02d\"", minutes, seconds)
    }

    fun formatSpeed(speedKmh: Double): String {
        return String.format(Locale.US, "%.1f", speedKmh)
    }

    fun formatDateTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("EEEE, d MMMM yyyy • HH:mm", Locale("id", "ID"))
        return sdf.format(Date(timestamp))
    }

    fun formatShortDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("d MMM yyyy", Locale("id", "ID"))
        return sdf.format(Date(timestamp))
    }
}
