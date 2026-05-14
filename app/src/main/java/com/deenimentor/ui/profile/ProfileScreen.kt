package com.deenimentor.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deenimentor.ui.analytics.AnalyticsViewModel
import com.deenimentor.ui.theme.*
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    analyticsViewModel: AnalyticsViewModel,
    onBack: () -> Unit
) {
    val analytics by analyticsViewModel.analytics.collectAsState()
    val totalPages by analyticsViewModel.totalPages.collectAsState()
    val userEmail = FirebaseAuth.getInstance().currentUser?.email ?: "Muslim User"

    val streak = analytics.streak
    val badges = listOf(
        Triple("3-Day Streak", "🌱", streak >= 3),
        Triple("7-Day Streak", "⭐", streak >= 7),
        Triple("21-Day Streak", "🏆", streak >= 21),
        Triple("40-Day Streak", "💎", streak >= 40),
        Triple("First Prayer", "🕌", analytics.totalPrayers > 0),
        Triple("Quran Reader", "📖", totalPages > 0)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Profile", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = GreenPrimary, titleContentColor = Color.White
                )
            )
        },
        containerColor = BackgroundLight
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Header
            Box(
                modifier = Modifier.fillMaxWidth()
                    .background(Brush.horizontalGradient(listOf(GreenPrimary, GreenSecondary)))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier.size(80.dp).background(GoldAccent, CircleShape),
                        contentAlignment = Alignment.Center
                    ) { Text("☪", fontSize = 36.sp) }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(userEmail, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(shape = RoundedCornerShape(20.dp), color = GoldAccent.copy(alpha = 0.2f)) {
                        Text("  🌿 Growth Journey  ", fontSize = 12.sp, color = GoldAccent, modifier = Modifier.padding(vertical = 4.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Streak Card
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("🔥", fontSize = 40.sp)
                    Text("$streak", fontSize = 48.sp, fontWeight = FontWeight.Bold, color = GoldAccent)
                    Text("Day Streak", fontSize = 16.sp, color = TextMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        when {
                            streak == 0 -> "Start your journey today! 🌟"
                            streak < 7 -> "Keep going! 7-day badge awaits 💪"
                            streak < 21 -> "Amazing! 21-day badge is close 🏆"
                            streak < 40 -> "Incredible! Diamond badge incoming 💎"
                            else -> "Masha'Allah! You are a champion! 🌟"
                        },
                        fontSize = 13.sp, color = GreenPrimary, textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Stats Row
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ProfileStatCard("Check-ins", "${analytics.checkIns.size}", "📋", GreenPrimary, Modifier.weight(1f))
                ProfileStatCard("Prayers", "${analytics.totalPrayers}", "🕌", GoldAccent, Modifier.weight(1f))
                ProfileStatCard("Pages", "$totalPages", "📖", Color(0xFF2196F3), Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Badges
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = GoldAccent)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Achievements", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextDark)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    badges.chunked(3).forEach { row ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            row.forEach { (name, emoji, unlocked) ->
                                BadgeItem(name, emoji, unlocked, Modifier.weight(1f))
                            }
                            repeat(3 - row.size) { Spacer(modifier = Modifier.weight(1f)) }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun ProfileStatCard(label: String, value: String, emoji: String, color: Color, modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = SurfaceWhite), elevation = CardDefaults.cardElevation(2.dp)) {
        Column(modifier = Modifier.padding(12.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(emoji, fontSize = 24.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = color)
            Text(label, fontSize = 9.sp, color = TextMedium, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun BadgeItem(name: String, emoji: String, unlocked: Boolean, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier.size(52.dp).background(
                if (unlocked) GoldAccent.copy(alpha = 0.15f) else Color(0xFFF0F0F0), RoundedCornerShape(12.dp)
            ), contentAlignment = Alignment.Center
        ) { Text(if (unlocked) emoji else "🔒", fontSize = 24.sp) }
        Spacer(modifier = Modifier.height(4.dp))
        Text(name, fontSize = 9.sp, color = if (unlocked) TextDark else TextMedium, textAlign = TextAlign.Center)
    }
}
