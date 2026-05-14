package com.deenimentor.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deenimentor.ui.auth.AuthViewModel
import com.deenimentor.ui.theme.*
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel,
    authViewModel: AuthViewModel,
    onNavigateToCheckIn: () -> Unit,
    onNavigateToQuran: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToDua: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToGoals: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onLogout: () -> Unit
) {
    val profile by homeViewModel.profile.collectAsState()
    val isDark = MaterialTheme.colorScheme.background == DarkBackground

    // Live clock
    var currentTime by remember { mutableStateOf("") }
    var currentDate by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        while (true) {
            val now = Date()
            currentTime = SimpleDateFormat("hh:mm:ss a", Locale.getDefault()).format(now)
            currentDate = SimpleDateFormat("EEEE, dd MMM yyyy", Locale.getDefault()).format(now)
            delay(1000L)
        }
    }

    // Next prayer
    val nextPrayer = remember { getNextPrayer() }

    val bgColor = if (isDark) DarkBackground else BackgroundLight

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Deeni Mentor", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(Icons.Default.Person, contentDescription = "Profile", tint = Color.White)
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = GreenPrimary, titleContentColor = Color.White)
            )
        },
        containerColor = bgColor
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())
        ) {
            // Header with live clock
            Box(
                modifier = Modifier.fillMaxWidth()
                    .background(Brush.horizontalGradient(listOf(GreenPrimary, GreenSecondary)))
                    .padding(20.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column {
                            Text("السَّلَامُ عَلَيْكُمْ", fontSize = 20.sp, color = GoldAccent, fontWeight = FontWeight.Bold)
                            val name = profile?.displayName?.takeIf { it.isNotBlank() }
                            if (name != null) {
                                Text("Welcome, $name", fontSize = 18.sp, color = Color.White, fontWeight = FontWeight.Medium)
                            } else {
                                Text("Welcome, Muslims", fontSize = 18.sp, color = Color.White, fontWeight = FontWeight.Medium)
                            }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(currentTime, fontSize = 18.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            Text(currentDate, fontSize = 10.sp, color = Color.White.copy(alpha = 0.8f))
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Next prayer chip
                    Surface(shape = RoundedCornerShape(20.dp), color = Color.Black.copy(alpha = 0.25f)) {
                        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("🕌", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Next: ${nextPrayer.first} at ${nextPrayer.second}", fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.Medium)
                        }
                    }

                    profile?.growthPath?.let { path ->
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(shape = RoundedCornerShape(20.dp), color = GoldAccent.copy(alpha = 0.2f)) {
                            Text("  🌿 $path Path  ", fontSize = 12.sp, color = GoldAccent, fontWeight = FontWeight.Medium, modifier = Modifier.padding(vertical = 4.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Namaz Times Card
            NamazTimesCard(isDark)

            Spacer(modifier = Modifier.height(16.dp))

            // Daily Check-In Card
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).clickable { onNavigateToCheckIn() },
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = GreenPrimary),
                elevation = CardDefaults.cardElevation(6.dp)
            ) {
                Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Daily Check-In", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Log your Salah, sleep, mood & good deeds", fontSize = 13.sp, color = Color.White.copy(alpha = 0.85f))
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(shape = RoundedCornerShape(8.dp), color = GoldAccent) {
                            Text("  Start Check-In  →  ", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(vertical = 6.dp))
                        }
                    }
                    Icon(Icons.Default.EditNote, contentDescription = null, tint = Color.White.copy(alpha = 0.4f), modifier = Modifier.size(60.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Feature Grid
            val cardBg = if (isDark) DarkCard else SurfaceWhite
            Text("  Features", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = if (isDark) DarkTextSecondary else TextMedium, modifier = Modifier.padding(horizontal = 16.dp))
            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FeatureCard(Icons.Default.MenuBook, "Quran\nReader", "Read & track", Color(0xFF009688), cardBg, Modifier.weight(1f)) { onNavigateToQuran() }
                FeatureCard(Icons.Default.BarChart, "Analytics", "View progress", Color(0xFF2196F3), cardBg, Modifier.weight(1f)) { onNavigateToAnalytics() }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FeatureCard(Icons.Default.AutoAwesome, "Dua\nCollection", "Browse duas", Color(0xFF9C27B0), cardBg, Modifier.weight(1f)) { onNavigateToDua() }
                FeatureCard(Icons.Default.TrackChanges, "My Goals", "Islamic goals", Color(0xFFFF5722), cardBg, Modifier.weight(1f)) { onNavigateToGoals() }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FeatureCard(Icons.Default.EmojiEvents, "Profile &\nStreak", "Achievements", GoldAccent, cardBg, Modifier.weight(1f)) { onNavigateToProfile() }
                FeatureCard(Icons.Default.Settings, "Settings", "Preferences", Color(0xFF607D8B), cardBg, Modifier.weight(1f)) { onNavigateToSettings() }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Thought of the Day
            val thoughtBg = if (isDark) DarkCard else Color(0xFFF0F8F0)
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = thoughtBg)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("🌙  Thought of the Day", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = GreenPrimary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "\"The best of people is the one who benefits others the most.\"",
                        fontSize = 13.sp, color = if (isDark) DarkTextPrimary else TextDark,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                    Text("— Al-Mu'jam Al-Awsat", fontSize = 11.sp, color = if (isDark) DarkTextSecondary else TextMedium)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun NamazTimesCard(isDark: Boolean) {
    val prayers = listOf(
        Pair("Fajr",    "5:00 AM"),
        Pair("Zuhr",    "1:00 PM"),
        Pair("Asr",     "5:15 PM"),
        Pair("Maghrib", "6:45 PM"),
        Pair("Isha",    "8:45 PM")
    )
    val cardBg = if (isDark) DarkCard else SurfaceWhite

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🕌", fontSize = 16.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Namaz Times — Islamabad", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = GreenPrimary)
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                prayers.forEach { (name, time) ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(name, fontSize = 11.sp, color = if (isDark) DarkTextSecondary else TextMedium)
                        Text(time, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GreenPrimary)
                    }
                }
            }
        }
    }
}

fun getNextPrayer(): Pair<String, String> {
    val cal = Calendar.getInstance()
    val hour = cal.get(Calendar.HOUR_OF_DAY)
    val minute = cal.get(Calendar.MINUTE)
    val timeInMins = hour * 60 + minute

    val prayers = listOf(
        Pair("Fajr", 5 * 60 + 0),
        Pair("Zuhr", 13 * 60 + 0),
        Pair("Asr", 17 * 60 + 15),
        Pair("Maghrib", 18 * 60 + 45),
        Pair("Isha", 20 * 60 + 45)
    )

    val next = prayers.firstOrNull { it.second > timeInMins } ?: prayers.first()
    val h = next.second / 60
    val m = next.second % 60
    val ampm = if (h < 12) "AM" else "PM"
    val h12 = if (h == 0) 12 else if (h > 12) h - 12 else h
    return Pair(next.first, "${h12}:${m.toString().padStart(2, '0')} $ampm")
}

@Composable
fun FeatureCard(icon: ImageVector, title: String, description: String, color: Color, cardBg: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val isDark = MaterialTheme.colorScheme.background == DarkBackground
    Card(
        modifier = modifier.aspectRatio(1f).clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(14.dp), verticalArrangement = Arrangement.Center) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (isDark) DarkTextPrimary else TextDark, lineHeight = 16.sp)
            Text(description, fontSize = 10.sp, color = if (isDark) DarkTextSecondary else Color(0xFF888888))
        }
    }
}
