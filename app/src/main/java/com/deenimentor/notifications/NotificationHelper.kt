package com.deenimentor.notifications

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.deenimentor.MainActivity
import com.deenimentor.R
import java.util.Calendar

object NotificationHelper {

    const val CHANNEL_ID = "deeni_mentor_reminders"
    const val CHANNEL_NAMAZ_ID = "deeni_namaz_alerts"
    const val CHANNEL_SLEEP_ID = "deeni_sleep_alerts"

    const val CHECKIN_NOTIFICATION_ID = 1001
    const val ALARM_REQUEST_CODE = 2001

    // Namaz notification IDs
    const val FAJR_PRE_ID = 3001
    const val ZUHR_PRE_ID = 3002
    const val ASR_PRE_ID = 3003
    const val MAGHRIB_PRE_ID = 3004
    const val ISHA_PRE_ID = 3005
    const val FAJR_FOLLOWUP_ID = 4001
    const val ZUHR_FOLLOWUP_ID = 4002
    const val ASR_FOLLOWUP_ID = 4003
    const val MAGHRIB_FOLLOWUP_ID = 4004
    const val ISHA_FOLLOWUP_ID = 4005

    // Sleep notification ID
    const val SLEEP_NOTIF_ID = 5001

    // Islamabad Namaz Times (approximate — update every 15 days)
    // Format: Pair(hour, minute) in 24h
    data class PrayerTime(val name: String, val hour: Int, val minute: Int, val preNotifId: Int, val followupId: Int, val alarmRequestCode: Int)

    fun getIslamabadPrayerTimes(): List<PrayerTime> = listOf(
        PrayerTime("Fajr",     4, 30, FAJR_PRE_ID,    FAJR_FOLLOWUP_ID,    6001),
        PrayerTime("Zuhr",    12, 30, ZUHR_PRE_ID,    ZUHR_FOLLOWUP_ID,    6002),
        PrayerTime("Asr",     16, 15, ASR_PRE_ID,     ASR_FOLLOWUP_ID,     6003),
        PrayerTime("Maghrib", 19, 30, MAGHRIB_PRE_ID, MAGHRIB_FOLLOWUP_ID, 6004),
        PrayerTime("Isha",    21,  0, ISHA_PRE_ID,    ISHA_FOLLOWUP_ID,    6005)
    )

    fun createNotificationChannels(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java)

        val dailyChannel = NotificationChannel(CHANNEL_ID, "Daily Reminders", NotificationManager.IMPORTANCE_HIGH).apply {
            description = "Daily check-in reminders"
        }
        val namazChannel = NotificationChannel(CHANNEL_NAMAZ_ID, "Namaz Alerts", NotificationManager.IMPORTANCE_HIGH).apply {
            description = "Prayer time reminders"
        }
        val sleepChannel = NotificationChannel(CHANNEL_SLEEP_ID, "Sleep Reminders", NotificationManager.IMPORTANCE_DEFAULT).apply {
            description = "Sleep time screen usage reminder"
        }

        manager.createNotificationChannel(dailyChannel)
        manager.createNotificationChannel(namazChannel)
        manager.createNotificationChannel(sleepChannel)
    }

    // Keep old function for compatibility
    fun createNotificationChannel(context: Context) = createNotificationChannels(context)

    fun scheduleDailyReminder(context: Context, hour: Int, minute: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, CheckInReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, ALARM_REQUEST_CODE, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR, 1)
        }
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, AlarmManager.INTERVAL_DAY, pendingIntent)
    }

    fun cancelDailyReminder(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, CheckInReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(context, ALARM_REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        alarmManager.cancel(pendingIntent)
    }

    fun scheduleAllNamazNotifications(context: Context) {
        val prayers = getIslamabadPrayerTimes()
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        for (prayer in prayers) {
            // 10 min before athan
            val preCalendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, prayer.hour)
                set(Calendar.MINUTE, prayer.minute - 10)
                set(Calendar.SECOND, 0)
                if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR, 1)
            }
            val preIntent = Intent(context, NamazPreReceiver::class.java).apply {
                putExtra("prayer_name", prayer.name)
                putExtra("notif_id", prayer.preNotifId)
                putExtra("prayer_hour", prayer.hour)
                putExtra("prayer_minute", prayer.minute)
            }
            val prePending = PendingIntent.getBroadcast(context, prayer.alarmRequestCode, preIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, preCalendar.timeInMillis, AlarmManager.INTERVAL_DAY, prePending)

            // 25 min after athan (followup)
            val followCalendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, prayer.hour)
                set(Calendar.MINUTE, prayer.minute + 25)
                set(Calendar.SECOND, 0)
                if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR, 1)
            }
            val followIntent = Intent(context, NamazFollowupReceiver::class.java).apply {
                putExtra("prayer_name", prayer.name)
                putExtra("notif_id", prayer.followupId)
            }
            val followPending = PendingIntent.getBroadcast(context, prayer.alarmRequestCode + 100, followIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, followCalendar.timeInMillis, AlarmManager.INTERVAL_DAY, followPending)
        }
    }

    fun cancelAllNamazNotifications(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        for (prayer in getIslamabadPrayerTimes()) {
            val preIntent = Intent(context, NamazPreReceiver::class.java)
            val prePending = PendingIntent.getBroadcast(context, prayer.alarmRequestCode, preIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            alarmManager.cancel(prePending)
            val followIntent = Intent(context, NamazFollowupReceiver::class.java)
            val followPending = PendingIntent.getBroadcast(context, prayer.alarmRequestCode + 100, followIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            alarmManager.cancel(followPending)
        }
    }

    fun scheduleSleepReminder(context: Context, sleepStartHour: Int, sleepStartMinute: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, SleepReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(context, 7001, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, sleepStartHour)
            set(Calendar.MINUTE, sleepStartMinute)
            set(Calendar.SECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR, 1)
        }
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, AlarmManager.INTERVAL_DAY, pendingIntent)
    }

    fun cancelSleepReminder(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, SleepReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(context, 7001, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        alarmManager.cancel(pendingIntent)
    }

    fun showCheckInReminder(context: Context) {
        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(context, 0, tapIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Daily Check-In Reminder")
            .setContentText("Don't forget your daily check-in! Track your Salah and habits.")
            .setStyle(NotificationCompat.BigTextStyle().bigText("Assalamu Alaikum! It's time for your daily check-in. Track your Salah, sleep, mood, and good deeds to keep growing."))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(CHECKIN_NOTIFICATION_ID, notification)
    }

    fun showNamazPreNotification(context: Context, prayerName: String, notifId: Int) {
        val tapIntent = Intent(context, MainActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK }
        val pendingIntent = PendingIntent.getActivity(context, notifId, tapIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val notification = NotificationCompat.Builder(context, CHANNEL_NAMAZ_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("🕌 $prayerName Time Soon")
            .setContentText("$prayerName will begin in 10 minutes. Prepare for prayer.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(notifId, notification)
    }

    fun showNamazFollowupNotification(context: Context, prayerName: String, notifId: Int) {
        val tapIntent = Intent(context, MainActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK }
        val pendingIntent = PendingIntent.getActivity(context, notifId, tapIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val notification = NotificationCompat.Builder(context, CHANNEL_NAMAZ_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("✅ Did you pray $prayerName?")
            .setContentText("Tap to open app and mark $prayerName as completed in your check-in.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(notifId, notification)
    }

    fun showSleepReminder(context: Context) {
        val notification = NotificationCompat.Builder(context, CHANNEL_SLEEP_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("🌙 Sleep Time Reminder")
            .setContentText("It's your sleep time. Put down your phone and rest for Allah's sake.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(SLEEP_NOTIF_ID, notification)
    }
}

class CheckInReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        NotificationHelper.showCheckInReminder(context)
    }
}

class NamazPreReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val prayerName = intent.getStringExtra("prayer_name") ?: "Namaz"
        val notifId = intent.getIntExtra("notif_id", 3001)
        NotificationHelper.showNamazPreNotification(context, prayerName, notifId)
    }
}

class NamazFollowupReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val prayerName = intent.getStringExtra("prayer_name") ?: "Namaz"
        val notifId = intent.getIntExtra("notif_id", 4001)
        NotificationHelper.showNamazFollowupNotification(context, prayerName, notifId)
    }
}

class SleepReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        NotificationHelper.showSleepReminder(context)
    }
}

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            NotificationHelper.scheduleDailyReminder(context, 20, 0)
            NotificationHelper.scheduleAllNamazNotifications(context)
        }
    }
}
