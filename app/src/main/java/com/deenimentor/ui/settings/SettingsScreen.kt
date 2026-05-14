package com.deenimentor.ui.settings

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deenimentor.notifications.NotificationHelper
import com.deenimentor.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit,
    isDarkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit
) {
    val context = LocalContext.current
    var notificationsEnabled by remember { mutableStateOf(true) }
    var namazNotifEnabled by remember { mutableStateOf(true) }
    var sleepReminderEnabled by remember { mutableStateOf(false) }
    var sleepStartHour by remember { mutableStateOf(22) }
    var sleepStartMinute by remember { mutableStateOf(0) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showSleepTimePicker by remember { mutableStateOf(false) }

    val isDark = isDarkMode
    val cardBg = if (isDark) DarkCard else SurfaceWhite
    val bgColor = if (isDark) DarkBackground else BackgroundLight

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = GreenPrimary, titleContentColor = Color.White)
            )
        },
        containerColor = bgColor
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // App Section
            SettingsSectionHeader("⚙️ App", isDark)
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = cardBg), elevation = CardDefaults.cardElevation(2.dp)) {
                Column {
                    SettingsToggleRow(
                        icon = Icons.Default.DarkMode, title = "Dark Mode",
                        subtitle = "Switch to dark theme",
                        checked = isDarkMode, onCheckedChange = onDarkModeChange, isDark = isDark
                    )
                    HorizontalDivider(color = if (isDark) Color(0xFF2A3F2E) else Color(0xFFEEEEEE))
                    SettingsInfoRow(icon = Icons.Default.Info, title = "App Version", value = "1.0.0", isDark = isDark)
                }
            }

            // Notifications
            SettingsSectionHeader("🔔 Notifications", isDark)
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = cardBg), elevation = CardDefaults.cardElevation(2.dp)) {
                Column {
                    SettingsToggleRow(
                        icon = Icons.Default.Notifications, title = "Daily Reminder",
                        subtitle = "Get notified to complete check-in",
                        checked = notificationsEnabled,
                        onCheckedChange = {
                            notificationsEnabled = it
                            if (it) NotificationHelper.scheduleDailyReminder(context, 20, 0)
                            else NotificationHelper.cancelDailyReminder(context)
                        }, isDark = isDark
                    )
                    HorizontalDivider(color = if (isDark) Color(0xFF2A3F2E) else Color(0xFFEEEEEE))
                    SettingsToggleRow(
                        icon = Icons.Default.Mosque, title = "Namaz Alerts",
                        subtitle = "10 min before each prayer & follow-up",
                        checked = namazNotifEnabled,
                        onCheckedChange = {
                            namazNotifEnabled = it
                            if (it) NotificationHelper.scheduleAllNamazNotifications(context)
                            else NotificationHelper.cancelAllNamazNotifications(context)
                        }, isDark = isDark
                    )
                    HorizontalDivider(color = if (isDark) Color(0xFF2A3F2E) else Color(0xFFEEEEEE))
                    SettingsToggleRow(
                        icon = Icons.Default.Bedtime, title = "Sleep Reminder",
                        subtitle = "Remind during sleep hours to put phone down",
                        checked = sleepReminderEnabled,
                        onCheckedChange = {
                            sleepReminderEnabled = it
                            if (it) {
                                NotificationHelper.scheduleSleepReminder(context, sleepStartHour, sleepStartMinute)
                            } else {
                                NotificationHelper.cancelSleepReminder(context)
                            }
                        }, isDark = isDark
                    )
                    if (sleepReminderEnabled) {
                        HorizontalDivider(color = if (isDark) Color(0xFF2A3F2E) else Color(0xFFEEEEEE))
                        SettingsClickableRow(
                            icon = Icons.Default.Schedule,
                            title = "Sleep Start Time",
                            value = "${sleepStartHour.toString().padStart(2,'0')}:${sleepStartMinute.toString().padStart(2,'0')}",
                            isDark = isDark
                        ) { showSleepTimePicker = true }
                    }
                }
            }

            // Namaz Times Info
            SettingsSectionHeader("🕌 Namaz Times", isDark)
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = cardBg), elevation = CardDefaults.cardElevation(2.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    val textColor = if (isDark) DarkTextPrimary else TextDark
                    val subColor = if (isDark) DarkTextSecondary else TextMedium
                    Text("Islamabad Approximate Times", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = textColor)
                    Spacer(modifier = Modifier.height(8.dp))
                    val times = listOf("Fajr" to "5:00 AM", "Zuhr" to "1:00 PM", "Asr" to "5:15 PM", "Maghrib" to "6:45 PM", "Isha" to "8:45 PM")
                    times.forEach { (name, time) ->
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(name, fontSize = 13.sp, color = textColor)
                            Text(time, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = GreenPrimary)
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("⚠️ Times are approximate for Islamabad. Updated periodically.", fontSize = 11.sp, color = subColor)
                }
            }

            // Account
            SettingsSectionHeader("👤 Account", isDark)
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = cardBg), elevation = CardDefaults.cardElevation(2.dp)) {
                Column {
                    SettingsClickRow(icon = Icons.Default.Logout, title = "Logout", tint = Color.Red, isDark = isDark) {
                        showLogoutDialog = true
                    }
                }
            }

            // About
            SettingsSectionHeader("ℹ️ About", isDark)
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = cardBg), elevation = CardDefaults.cardElevation(2.dp)) {
                Column {
                    SettingsInfoRow(icon = Icons.Default.Favorite, title = "Made with ❤️ for Muslims", value = "", isDark = isDark)
                    HorizontalDivider(color = if (isDark) Color(0xFF2A3F2E) else Color(0xFFEEEEEE))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showSleepTimePicker) {
        AlertDialog(
            onDismissRequest = { showSleepTimePicker = false },
            title = { Text("Set Sleep Start Time") },
            text = {
                Column {
                    Text("Hour (0-23):")
                    Slider(value = sleepStartHour.toFloat(), onValueChange = { sleepStartHour = it.toInt() }, valueRange = 18f..23f, steps = 4)
                    Text("${sleepStartHour}:00")
                }
            },
            confirmButton = {
                Button(onClick = {
                    showSleepTimePicker = false
                    if (sleepReminderEnabled) NotificationHelper.scheduleSleepReminder(context, sleepStartHour, 0)
                }, colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { showSleepTimePicker = false }) { Text("Cancel") } }
        )
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Logout", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to logout?") },
            confirmButton = {
                Button(onClick = { showLogoutDialog = false; onLogout() }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) { Text("Logout") }
            },
            dismissButton = { TextButton(onClick = { showLogoutDialog = false }) { Text("Cancel") } }
        )
    }
}

@Composable
fun SettingsSectionHeader(title: String, isDark: Boolean) {
    Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (isDark) DarkTextSecondary else TextMedium, modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp))
}

@Composable
fun SettingsToggleRow(icon: ImageVector, title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit, isDark: Boolean) {
    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = if (isDark) DarkTextPrimary else TextDark)
            Text(subtitle, fontSize = 12.sp, color = if (isDark) DarkTextSecondary else TextMedium)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = GreenPrimary))
    }
}

@Composable
fun SettingsInfoRow(icon: ImageVector, title: String, value: String, isDark: Boolean) {
    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(title, fontSize = 15.sp, color = if (isDark) DarkTextPrimary else TextDark, modifier = Modifier.weight(1f))
        if (value.isNotBlank()) Text(value, fontSize = 13.sp, color = if (isDark) DarkTextSecondary else TextMedium)
    }
}

@Composable
fun SettingsClickableRow(icon: ImageVector, title: String, value: String, isDark: Boolean, onClick: () -> Unit) {
    TextButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(title, fontSize = 15.sp, color = if (isDark) DarkTextPrimary else TextDark, modifier = Modifier.weight(1f))
            Text(value, fontSize = 13.sp, color = GreenPrimary, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun SettingsClickRow(icon: ImageVector, title: String, tint: Color = GreenPrimary, isDark: Boolean, onClick: () -> Unit) {
    TextButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(title, fontSize = 15.sp, color = tint, fontWeight = FontWeight.Medium)
        }
    }
}
