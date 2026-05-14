package com.deenimentor.ui.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deenimentor.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(viewModel: AnalyticsViewModel, onBack: () -> Unit) {
    val analytics by viewModel.analytics.collectAsState()
    val totalPages by viewModel.totalPages.collectAsState()
    val isDark = MaterialTheme.colorScheme.background == DarkBackground
    val bgColor = if (isDark) DarkBackground else BackgroundLight

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Analytics", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = GreenPrimary, titleContentColor = Color.White)
            )
        },
        containerColor = bgColor
    ) { padding ->
        if (analytics.checkIns.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📊", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("No data yet!", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = if (isDark) DarkTextPrimary else TextDark)
                    Text("Complete daily check-ins\nto see your analytics", textAlign = TextAlign.Center, color = if (isDark) DarkTextSecondary else TextMedium)
                }
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val cardBg = if (isDark) DarkCard else SurfaceWhite

            // Stats Row
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("🔥 Streak", "${analytics.streak} days", GoldAccent, cardBg, Modifier.weight(1f))
                StatCard("🕌 Prayers", "${analytics.totalPrayers}", GreenPrimary, cardBg, Modifier.weight(1f))
                StatCard("📖 Pages", "$totalPages", Color(0xFF2196F3), cardBg, Modifier.weight(1f))
            }

            // Salah Breakdown
            AnalyticsCard("🕌 Salah Completion", cardBg, isDark) {
                val prayers = listOf(
                    Triple("Fajr", analytics.checkIns.count { it.fajrDone }, analytics.checkIns.size),
                    Triple("Zuhr", analytics.checkIns.count { it.zuhrDone }, analytics.checkIns.size),
                    Triple("Asr", analytics.checkIns.count { it.asrDone }, analytics.checkIns.size),
                    Triple("Maghrib", analytics.checkIns.count { it.maghribDone }, analytics.checkIns.size),
                    Triple("Isha", analytics.checkIns.count { it.ishaDone }, analytics.checkIns.size)
                )
                Column {
                    Text(
                        "${(analytics.avgSalah * 100).toInt()}% overall average",
                        fontSize = 24.sp, fontWeight = FontWeight.Bold, color = GreenPrimary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    prayers.forEach { (name, done, total) ->
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(name, fontSize = 13.sp, color = if (isDark) DarkTextPrimary else TextDark, modifier = Modifier.width(60.dp))
                            LinearProgressIndicator(
                                progress = { if (total > 0) done.toFloat() / total else 0f },
                                modifier = Modifier.weight(1f).height(8.dp),
                                color = GreenPrimary,
                                trackColor = GreenPrimary.copy(alpha = 0.15f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("$done/$total", fontSize = 11.sp, color = if (isDark) DarkTextSecondary else TextMedium, modifier = Modifier.width(36.dp))
                        }
                    }
                }
            }

            // Mood & Sleep
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MoodCard("😊 Mood", analytics.avgMood, 5f, GoldAccent, cardBg, isDark, Modifier.weight(1f))
                MoodCard("😴 Sleep", analytics.avgSleep, 10f, Color(0xFF673AB7), cardBg, isDark, Modifier.weight(1f))
            }

            // Productivity
            AnalyticsCard("📈 Productivity", cardBg, isDark) {
                Text(
                    "${(analytics.avgProductivity * 10).toInt()}%",
                    fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF009688)
                )
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { analytics.avgProductivity / 5f },
                    modifier = Modifier.fillMaxWidth().height(12.dp),
                    color = Color(0xFF009688),
                    trackColor = Color(0xFFE0F2F1)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Avg: ${String.format("%.1f", analytics.avgProductivity)}/5", fontSize = 12.sp, color = if (isDark) DarkTextSecondary else TextMedium)
            }

            // Quran Progress
            AnalyticsCard("📖 Quran Reading Progress", cardBg, isDark) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            progress = { (totalPages / 604f).coerceIn(0f, 1f) },
                            modifier = Modifier.size(80.dp),
                            strokeWidth = 8.dp,
                            color = Color(0xFF2196F3),
                            trackColor = Color(0xFFE3F2FD)
                        )
                        Text("${((totalPages / 604f) * 100).toInt()}%", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2196F3))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("$totalPages pages read", fontWeight = FontWeight.Bold, color = if (isDark) DarkTextPrimary else TextDark)
                        Text("out of 604 total pages", fontSize = 12.sp, color = if (isDark) DarkTextSecondary else TextMedium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("${604 - totalPages} pages remaining", fontSize = 12.sp, color = Color(0xFF2196F3))
                    }
                }
            }

            // Good Deeds Summary
            val goodDeedsDays = analytics.checkIns.count { it.goodDeeds.isNotBlank() }
            AnalyticsCard("✨ Good Deeds", cardBg, isDark) {
                Text("$goodDeedsDays", fontSize = 36.sp, fontWeight = FontWeight.Bold, color = GoldAccent)
                Text("days with recorded good deeds", fontSize = 13.sp, color = if (isDark) DarkTextSecondary else TextMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text("out of ${analytics.checkIns.size} total check-ins", fontSize = 12.sp, color = if (isDark) DarkTextSecondary else TextMedium)
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun StatCard(label: String, value: String, color: Color, cardBg: Color, modifier: Modifier = Modifier) {
    val isDark = MaterialTheme.colorScheme.background == DarkBackground
    Card(modifier = modifier, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = cardBg), elevation = CardDefaults.cardElevation(2.dp)) {
        Column(modifier = Modifier.padding(12.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = color)
            Spacer(modifier = Modifier.height(4.dp))
            Text(label, fontSize = 10.sp, color = if (isDark) DarkTextSecondary else TextMedium, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun MoodCard(title: String, value: Float, max: Float, color: Color, cardBg: Color, isDark: Boolean, modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = cardBg), elevation = CardDefaults.cardElevation(2.dp)) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (isDark) DarkTextPrimary else TextDark)
            Spacer(modifier = Modifier.height(8.dp))
            Text(String.format("%.1f", value), fontSize = 26.sp, fontWeight = FontWeight.Bold, color = color)
            Text("/ $max avg", fontSize = 11.sp, color = if (isDark) DarkTextSecondary else TextMedium)
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(progress = { value / max }, modifier = Modifier.fillMaxWidth().height(8.dp), color = color, trackColor = color.copy(alpha = 0.1f))
        }
    }
}

@Composable
fun AnalyticsCard(title: String, cardBg: Color, isDark: Boolean, content: @Composable () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = cardBg), elevation = CardDefaults.cardElevation(2.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = if (isDark) DarkTextPrimary else TextDark)
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}
