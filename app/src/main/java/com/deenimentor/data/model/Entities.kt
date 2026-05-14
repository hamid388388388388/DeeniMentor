package com.deenimentor.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_checkin")
data class DailyCheckIn(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String,
    val userId: String,
    val fajrDone: Boolean = false,
    val fajrJamaat: Boolean = false,
    val zuhrDone: Boolean = false,
    val zuhrJamaat: Boolean = false,
    val asrDone: Boolean = false,
    val asrJamaat: Boolean = false,
    val maghribDone: Boolean = false,
    val maghribJamaat: Boolean = false,
    val ishaDone: Boolean = false,
    val ishaJamaat: Boolean = false,
    val sleepHours: Float = 0f,
    val mood: Int = 3,
    val productivity: Int = 3,
    val goodDeeds: String = "",
    val struggles: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val userId: String,
    val displayName: String,
    val email: String,
    val growthPath: String,
    val reminderHour: Int = 20,
    val reminderMinute: Int = 0,
    val sleepStartHour: Int = 22,
    val sleepStartMinute: Int = 0,
    val sleepEndHour: Int = 6,
    val sleepEndMinute: Int = 0,
    val sleepReminderEnabled: Boolean = false,
    val darkModeEnabled: Boolean = false,
    val namazNotifEnabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "quran_sessions")
data class QuranSession(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String = "",
    val date: String = "",
    val surahNumber: Int = 1,
    val surahName: String = "Al-Fatiha",
    val ayahNumber: Int = 1,
    val pagesRead: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "duas")
data class Dua(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String = "",
    val category: String = "",
    val title: String = "",
    val arabicText: String = "",
    val transliteration: String = "",
    val translation: String = "",
    val reference: String = "",
    val isFavorite: Boolean = false
)

@Entity(tableName = "islamic_goals")
data class IslamicGoal(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String = "",
    val title: String = "",
    val description: String = "",
    val targetDate: String = "",
    val isCompleted: Boolean = false,
    val category: String = "General",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "prayer_completion")
data class PrayerCompletion(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String = "",
    val date: String = "",
    val prayerName: String = "",
    val completed: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
