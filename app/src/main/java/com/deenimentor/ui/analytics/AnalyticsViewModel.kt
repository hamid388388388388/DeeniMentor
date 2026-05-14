package com.deenimentor.ui.analytics

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.deenimentor.data.db.AppDatabase
import com.deenimentor.data.model.DailyCheckIn
import com.deenimentor.data.repository.AppRepository
import com.google.firebase.ktx.Firebase
import com.google.firebase.auth.ktx.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AnalyticsData(
    val checkIns: List<DailyCheckIn> = emptyList(),
    val avgSalah: Float = 0f,
    val avgMood: Float = 0f,
    val avgSleep: Float = 0f,
    val avgProductivity: Float = 0f,
    val totalPrayers: Int = 0,
    val streak: Int = 0
)

class AnalyticsViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = AppRepository(AppDatabase.getDatabase(application))
    private val userId get() = Firebase.auth.currentUser?.uid ?: ""

    private val _analytics = MutableStateFlow(AnalyticsData())
    val analytics: StateFlow<AnalyticsData> = _analytics.asStateFlow()

    private val _totalPages = MutableStateFlow(0)
    val totalPages: StateFlow<Int> = _totalPages.asStateFlow()

    init { loadAnalytics() }

    fun loadAnalytics() {
        viewModelScope.launch {
            val checkIns = repo.getLast30CheckIns(userId)
            if (checkIns.isEmpty()) return@launch

            val avgSalah = checkIns.map { ci ->
                listOf(ci.fajrDone, ci.zuhrDone, ci.asrDone, ci.maghribDone, ci.ishaDone)
                    .count { it }.toFloat() / 5f
            }.average().toFloat()

            val avgMood = checkIns.map { it.mood }.average().toFloat()
            val avgSleep = checkIns.map { it.sleepHours }.average().toFloat()
            val avgProd = checkIns.map { it.productivity }.average().toFloat()
            val totalPrayers = checkIns.sumOf { ci ->
                listOf(ci.fajrDone, ci.zuhrDone, ci.asrDone, ci.maghribDone, ci.ishaDone).count { it }
            }
            val streak = calculateStreak(checkIns)

            _analytics.value = AnalyticsData(
                checkIns = checkIns,
                avgSalah = avgSalah,
                avgMood = avgMood,
                avgSleep = avgSleep,
                avgProductivity = avgProd,
                totalPrayers = totalPrayers,
                streak = streak
            )
            _totalPages.value = repo.getTotalPagesRead(userId)
        }
    }

    private fun calculateStreak(checkIns: List<DailyCheckIn>): Int {
        if (checkIns.isEmpty()) return 0
        val sorted = checkIns.sortedByDescending { it.date }
        var streak = 1
        for (i in 0 until sorted.size - 1) {
            val diff = dateDiff(sorted[i].date, sorted[i + 1].date)
            if (diff == 1L) streak++ else break
        }
        return streak
    }

    private fun dateDiff(date1: String, date2: String): Long {
        return try {
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            val d1 = sdf.parse(date1)?.time ?: 0L
            val d2 = sdf.parse(date2)?.time ?: 0L
            (d1 - d2) / (1000 * 60 * 60 * 24)
        } catch (e: Exception) { 0L }
    }
}
