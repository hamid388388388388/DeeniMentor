package com.deenimentor.data.db

import androidx.room.*
import com.deenimentor.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CheckInDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCheckIn(checkIn: DailyCheckIn)

    @Query("SELECT * FROM daily_checkin WHERE userId = :userId ORDER BY date DESC")
    fun getAllCheckIns(userId: String): Flow<List<DailyCheckIn>>

    @Query("SELECT * FROM daily_checkin WHERE userId = :userId AND date = :date LIMIT 1")
    suspend fun getCheckInByDate(userId: String, date: String): DailyCheckIn?

    @Query("SELECT * FROM daily_checkin WHERE userId = :userId ORDER BY date DESC LIMIT 7")
    suspend fun getLastSevenCheckIns(userId: String): List<DailyCheckIn>

    @Query("SELECT * FROM daily_checkin WHERE userId = :userId ORDER BY date DESC LIMIT 30")
    suspend fun getLast30Days(userId: String): List<DailyCheckIn>
}

@Dao
interface UserProfileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(profile: UserProfile)

    @Query("SELECT * FROM user_profile WHERE userId = :userId LIMIT 1")
    suspend fun getProfile(userId: String): UserProfile?

    @Query("SELECT * FROM user_profile WHERE userId = :userId LIMIT 1")
    fun getProfileFlow(userId: String): Flow<UserProfile?>
}

@Dao
interface QuranDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: QuranSession)

    @Query("SELECT * FROM quran_sessions WHERE userId = :userId ORDER BY date DESC")
    fun getAllSessions(userId: String): Flow<List<QuranSession>>

    @Query("SELECT * FROM quran_sessions WHERE userId = :userId ORDER BY date DESC LIMIT 1")
    suspend fun getLastSession(userId: String): QuranSession?

    @Query("SELECT SUM(pagesRead) FROM quran_sessions WHERE userId = :userId")
    suspend fun getTotalPagesRead(userId: String): Int?

    @Query("SELECT * FROM quran_sessions WHERE userId = :userId ORDER BY date DESC LIMIT 30")
    suspend fun getLast30Sessions(userId: String): List<QuranSession>
}

@Dao
interface DuaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDua(dua: Dua)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllDuas(duas: List<Dua>)

    @Query("SELECT * FROM duas WHERE userId = :userId ORDER BY category ASC")
    fun getAllDuas(userId: String): Flow<List<Dua>>

    @Query("SELECT * FROM duas WHERE userId = :userId AND isFavorite = 1")
    fun getFavoriteDuas(userId: String): Flow<List<Dua>>

    @Query("SELECT * FROM duas WHERE userId = :userId AND (title LIKE '%' || :query || '%' OR translation LIKE '%' || :query || '%')")
    suspend fun searchDuas(userId: String, query: String): List<Dua>

    @Query("UPDATE duas SET isFavorite = :isFavorite WHERE id = :duaId")
    suspend fun toggleFavorite(duaId: Int, isFavorite: Boolean)

    @Query("SELECT COUNT(*) FROM duas WHERE userId = :userId")
    suspend fun getDuaCount(userId: String): Int
}

@Dao
interface IslamicGoalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: IslamicGoal)

    @Update
    suspend fun updateGoal(goal: IslamicGoal)

    @Delete
    suspend fun deleteGoal(goal: IslamicGoal)

    @Query("SELECT * FROM islamic_goals WHERE userId = :userId ORDER BY createdAt DESC")
    fun getAllGoals(userId: String): Flow<List<IslamicGoal>>

    @Query("SELECT * FROM islamic_goals WHERE userId = :userId AND isCompleted = 0 ORDER BY createdAt DESC")
    fun getActiveGoals(userId: String): Flow<List<IslamicGoal>>

    @Query("UPDATE islamic_goals SET isCompleted = :completed WHERE id = :goalId")
    suspend fun setCompleted(goalId: Int, completed: Boolean)
}

@Dao
interface PrayerCompletionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(pc: com.deenimentor.data.model.PrayerCompletion)

    @Query("SELECT * FROM prayer_completion WHERE userId = :userId AND date = :date")
    suspend fun getForDate(userId: String, date: String): List<com.deenimentor.data.model.PrayerCompletion>
}
