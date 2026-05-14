package com.deenimentor.ui.onboarding

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deenimentor.ui.auth.AuthViewModel
import com.deenimentor.ui.theme.*
import kotlinx.coroutines.launch

data class GrowthPathOption(
    val name: String,
    val arabicTitle: String,
    val description: String,
    val details: String,
    val icon: ImageVector,
    val color: Color
)

val growthPaths = listOf(
    GrowthPathOption(
        name = "Starter",
        arabicTitle = "المسلم البسيط",
        description = "Simple Muslim",
        details = "Begin your journey with basic daily tracking. 3 essential habits, gentle reminders, and foundational Islamic guidance.",
        icon = Icons.Default.Star,
        color = Color(0xFF4CAF50)
    ),
    GrowthPathOption(
        name = "Pro",
        arabicTitle = "مسلم النمو",
        description = "Growth Muslim",
        details = "Track all 5 Salah, monitor sleep & mood patterns, receive personalised weekly analysis and deeper Islamic insights.",
        icon = Icons.Default.TrendingUp,
        color = Color(0xFF2196F3)
    ),
    GrowthPathOption(
        name = "Ihsan",
        arabicTitle = "مسلم الإحسان",
        description = "Excellence Muslim",
        details = "The complete experience. Advanced habit tracking, Jamaat monitoring, deep Quran & Hadith integration, and milestone challenges.",
        icon = Icons.Default.EmojiEvents,
        color = Color(0xFFC9A84C)
    )
)

@Composable
fun OnboardingScreen(
    viewModel: AuthViewModel,
    onOnboardingComplete: () -> Unit
) {
    var selectedPath by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(GreenPrimary, BackgroundLight)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("بِسْمِ اللهِ", fontSize = 24.sp, color = GoldAccent, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Choose Your Growth Path", fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = Color.White, textAlign = TextAlign.Center)
            Text("Select the level that best describes your current practice", fontSize = 13.sp, color = Color.White.copy(alpha = 0.8f), textAlign = TextAlign.Center)

            Spacer(modifier = Modifier.height(28.dp))

            growthPaths.forEach { path ->
                val isSelected = selectedPath == path.name
                val borderColor by animateColorAsState(
                    targetValue = if (isSelected) path.color else Color.Transparent,
                    animationSpec = tween(300),
                    label = "border"
                )
                val bgColor by animateColorAsState(
                    targetValue = if (isSelected) path.color.copy(alpha = 0.08f) else SurfaceWhite,
                    animationSpec = tween(300),
                    label = "bg"
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clickable { selectedPath = path.name },
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(if (isSelected) 2.dp else 1.dp, if (isSelected) borderColor else Color(0xFFDDDDDD)),
                    colors = CardDefaults.cardColors(containerColor = bgColor),
                    elevation = CardDefaults.cardElevation(if (isSelected) 6.dp else 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(path.color.copy(alpha = 0.15f), RoundedCornerShape(14.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(path.icon, contentDescription = null, tint = path.color, modifier = Modifier.size(32.dp))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(path.name, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextDark)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("· ${path.description}", fontSize = 12.sp, color = path.color, fontWeight = FontWeight.Medium)
                            }
                            Text(path.arabicTitle, fontSize = 12.sp, color = TextMedium)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(path.details, fontSize = 12.sp, color = TextMedium, lineHeight = 16.sp)
                        }
                        if (isSelected) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.Default.CheckCircle, contentDescription = "Selected", tint = path.color, modifier = Modifier.size(24.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = {
                    selectedPath?.let { path ->
                        isLoading = true
                        scope.launch {
                            viewModel.saveGrowthPath(path)
                            isLoading = false
                            onOnboardingComplete()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                enabled = selectedPath != null && !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text("Get Started  →", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
