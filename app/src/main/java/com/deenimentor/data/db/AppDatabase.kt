package com.deenimentor.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.deenimentor.data.model.*

@Database(
    entities = [DailyCheckIn::class, UserProfile::class, QuranSession::class, Dua::class, IslamicGoal::class, PrayerCompletion::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun checkInDao(): CheckInDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun quranDao(): QuranDao
    abstract fun duaDao(): DuaDao
    abstract fun islamicGoalDao(): IslamicGoalDao
    abstract fun prayerCompletionDao(): PrayerCompletionDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE user_profile ADD COLUMN sleepStartHour INTEGER NOT NULL DEFAULT 22")
                database.execSQL("ALTER TABLE user_profile ADD COLUMN sleepStartMinute INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE user_profile ADD COLUMN sleepEndHour INTEGER NOT NULL DEFAULT 6")
                database.execSQL("ALTER TABLE user_profile ADD COLUMN sleepEndMinute INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE user_profile ADD COLUMN sleepReminderEnabled INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE user_profile ADD COLUMN darkModeEnabled INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE user_profile ADD COLUMN namazNotifEnabled INTEGER NOT NULL DEFAULT 1")
                database.execSQL("ALTER TABLE user_profile ADD COLUMN createdAt INTEGER NOT NULL DEFAULT 0")
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS prayer_completion (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        userId TEXT NOT NULL DEFAULT '',
                        date TEXT NOT NULL DEFAULT '',
                        prayerName TEXT NOT NULL DEFAULT '',
                        completed INTEGER NOT NULL DEFAULT 0,
                        timestamp INTEGER NOT NULL DEFAULT 0
                    )
                """)
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "deeni_mentor_db")
                    .addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
