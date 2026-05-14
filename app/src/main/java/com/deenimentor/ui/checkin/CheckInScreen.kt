package com.deenimentor.ui.checkin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deenimentor.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckInScreen(
    viewModel: CheckInViewModel,
    onBack: () -> Unit,
    onSuccess: () -> Unit
) {
    val form by viewModel.formState.collectAsState()
    val saveState by viewModel.saveState.collectAsState()

    LaunchedEffect(saveState) {
        if (saveState is CheckInSaveState.Success) {
            onSuccess()
            viewModel.resetSaveState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Daily Check-In", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = GreenPrimary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = BackgroundLight
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Already done banner
            if (saveState is CheckInSaveState.AlreadyDone) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3CD)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF856404))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("You've already completed today's check-in!", color = Color(0xFF856404), fontSize = 13.sp)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Section 1: Salah
            SectionCard(title = "🕌  Salah", subtitle = "Track your prayers") {
                form.salah.forEachIndexed { index, salah ->
                    SalahRow(
                        name = salah.name,
                        done = salah.done,
                        jamaat = salah.jamaat,
                        onDoneChange = { viewModel.updateSalahDone(index, it) },
                        onJamaatChange = { viewModel.updateSalahJamaat(index, it) }
                    )
                    if (index < form.salah.size - 1) HorizontalDivider(thickness = 0.5.dp, color = Color(0xFFEEEEEE))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Section 2: Sleep
            SectionCard(title = "😴  Sleep", subtitle = "How many hours did you sleep?") {
                Text("${form.sleepHours} hours", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = GreenPrimary)
                Slider(
                    value = form.sleepHours,
                    onValueChange = { viewModel.updateSleep(Math.round(it * 2).toFloat() / 2f) },
                    valueRange = 2f..12f,
                    steps = 19,
                    colors = SliderDefaults.colors(thumbColor = GreenPrimary, activeTrackColor = GreenPrimary),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("2h", fontSize = 11.sp, color = TextMedium)
                    Text("12h", fontSize = 11.sp, color = TextMedium)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Section 3: Mood
            SectionCard(title = "😊  Mood", subtitle = "How are you feeling today?") {
                val emojis = listOf("😞", "😕", "😐", "🙂", "😄")
                val labels = listOf("Sad", "Low", "Neutral", "Good", "Great")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    emojis.forEachIndexed { index, emoji ->
                        val isSelected = form.mood == index + 1
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) GreenPrimary.copy(alpha = 0.15f) else Color.Transparent),
                                contentAlignment = Alignment.Center
                            ) {
                                TextButton(onClick = { viewModel.updateMood(index + 1) }, modifier = Modifier.fillMaxSize()) {
                                    Text(emoji, fontSize = if (isSelected) 26.sp else 22.sp)
                                }
                            }
                            Text(labels[index], fontSize = 10.sp, color = if (isSelected) GreenPrimary else TextMedium)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Section 4: Productivity
            SectionCard(title = "📈  Productivity", subtitle = "Rate your productivity today (1–5)") {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    (1..5).forEach { level ->
                        val isSelected = form.productivity == level
                        Button(
                            onClick = { viewModel.updateProductivity(level) },
                            modifier = Modifier.size(48.dp),
                            shape = CircleShape,
                            contentPadding = PaddingValues(0.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) GreenPrimary else Color(0xFFE8E8E8),
                                contentColor = if (isSelected) Color.White else TextDark
                            )
                        ) {
                            Text("$level", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Section 5: Good Deeds & Struggles
            SectionCard(title = "📝  Good Deeds & Struggles", subtitle = "Reflect on your day") {
                OutlinedTextField(
                    value = form.goodDeeds,
                    onValueChange = { viewModel.updateGoodDeeds(it) },
                    label = { Text("Good Deeds Today") },
                    placeholder = { Text("e.g., Prayed on time, helped someone...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 3,
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = form.struggles,
                    onValueChange = { viewModel.updateStruggles(it) },
                    label = { Text("Struggles / Challenges") },
                    placeholder = { Text("e.g., Missed Fajr, felt distracted...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 3,
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Submit Button
            Button(
                onClick = { viewModel.submitCheckIn() },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                enabled = saveState !is CheckInSaveState.Loading,
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
            ) {
                if (saveState is CheckInSaveState.Loading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Submit Check-In", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            if (saveState is CheckInSaveState.Error) {
                Spacer(modifier = Modifier.height(8.dp))
                Text((saveState as CheckInSaveState.Error).message, color = ErrorRed, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun SectionCard(title: String, subtitle: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextDark)
            Text(subtitle, fontSize = 12.sp, color = TextMedium)
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
fun SalahRow(
    name: String,
    done: Boolean,
    jamaat: Boolean,
    onDoneChange: (Boolean) -> Unit,
    onJamaatChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(name, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = TextDark, modifier = Modifier.width(80.dp))
        Spacer(modifier = Modifier.weight(1f))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Prayed", fontSize = 12.sp, color = TextMedium)
            Switch(
                checked = done,
                onCheckedChange = onDoneChange,
                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = GreenPrimary)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        if (done) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Jamaat", fontSize = 12.sp, color = TextMedium)
                Checkbox(
                    checked = jamaat,
                    onCheckedChange = onJamaatChange,
                    colors = CheckboxDefaults.colors(checkedColor = GreenPrimary)
                )
            }
        }
    }
}


