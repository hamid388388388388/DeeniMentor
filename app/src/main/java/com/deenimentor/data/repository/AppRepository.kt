package com.deenimentor.data.repository

import com.deenimentor.data.db.AppDatabase
import com.deenimentor.data.model.*
import kotlinx.coroutines.flow.Flow

class AppRepository(private val db: AppDatabase) {

    // ── Check-In ──────────────────────────────────────────
    suspend fun insertCheckIn(checkIn: DailyCheckIn) = db.checkInDao().insertCheckIn(checkIn)
    suspend fun saveCheckIn(checkIn: DailyCheckIn) = insertCheckIn(checkIn)
    fun getAllCheckIns(userId: String): Flow<List<DailyCheckIn>> = db.checkInDao().getAllCheckIns(userId)
    suspend fun getCheckInByDate(userId: String, date: String) = db.checkInDao().getCheckInByDate(userId, date)
    suspend fun getLast7CheckIns(userId: String) = db.checkInDao().getLastSevenCheckIns(userId)
    suspend fun getLast30CheckIns(userId: String) = db.checkInDao().getLast30Days(userId)

    // ── User Profile ──────────────────────────────────────
    suspend fun insertOrUpdateProfile(profile: UserProfile) = db.userProfileDao().insertOrUpdate(profile)
    suspend fun saveProfile(profile: UserProfile) = insertOrUpdateProfile(profile)
    suspend fun getProfile(userId: String) = db.userProfileDao().getProfile(userId)
    fun getProfileFlow(userId: String): Flow<UserProfile?> = db.userProfileDao().getProfileFlow(userId)

    // ── Quran Sessions ────────────────────────────────────
    suspend fun insertQuranSession(session: QuranSession) = db.quranDao().insertSession(session)
    fun getAllQuranSessions(userId: String): Flow<List<QuranSession>> = db.quranDao().getAllSessions(userId)
    suspend fun getLastQuranSession(userId: String) = db.quranDao().getLastSession(userId)
    suspend fun getTotalPagesRead(userId: String) = db.quranDao().getTotalPagesRead(userId) ?: 0
    suspend fun getLast30QuranSessions(userId: String) = db.quranDao().getLast30Sessions(userId)

    // ── Duas ──────────────────────────────────────────────
    suspend fun insertDua(dua: Dua) = db.duaDao().insertDua(dua)
    suspend fun insertAllDuas(duas: List<Dua>) = db.duaDao().insertAllDuas(duas)
    fun getAllDuas(userId: String): Flow<List<Dua>> = db.duaDao().getAllDuas(userId)
    fun getFavoriteDuas(userId: String): Flow<List<Dua>> = db.duaDao().getFavoriteDuas(userId)
    suspend fun searchDuas(userId: String, query: String) = db.duaDao().searchDuas(userId, query)
    suspend fun toggleFavorite(duaId: Int, isFavorite: Boolean) = db.duaDao().toggleFavorite(duaId, isFavorite)
    suspend fun getDuaCount(userId: String) = db.duaDao().getDuaCount(userId)

    // ── Islamic Goals ─────────────────────────────────────
    suspend fun insertGoal(goal: IslamicGoal) = db.islamicGoalDao().insertGoal(goal)
    suspend fun updateGoal(goal: IslamicGoal) = db.islamicGoalDao().updateGoal(goal)
    suspend fun deleteGoal(goal: IslamicGoal) = db.islamicGoalDao().deleteGoal(goal)
    fun getAllGoals(userId: String): Flow<List<IslamicGoal>> = db.islamicGoalDao().getAllGoals(userId)
    fun getActiveGoals(userId: String): Flow<List<IslamicGoal>> = db.islamicGoalDao().getActiveGoals(userId)
    suspend fun setGoalCompleted(goalId: Int, completed: Boolean) = db.islamicGoalDao().setCompleted(goalId, completed)
}
