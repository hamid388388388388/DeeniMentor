package com.deenimentor.ui.goals

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deenimentor.data.model.IslamicGoal
import com.deenimentor.ui.theme.*

val GOAL_CATEGORIES = listOf("General", "Salah", "Quran", "Fasting", "Charity", "Knowledge", "Character")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsScreen(viewModel: GoalsViewModel, onBack: () -> Unit) {
    val goals by viewModel.goals.collectAsState()
    val showDialog by viewModel.showAddDialog.collectAsState()

    val completed = goals.count { it.isCompleted }
    val total = goals.size

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Islamic Goals", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = GreenPrimary, titleContentColor = Color.White)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.showDialog() },
                containerColor = GreenPrimary
            ) { Icon(Icons.Default.Add, contentDescription = "Add Goal", tint = Color.White) }
        },
        containerColor = BackgroundLight
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Progress Header
            if (total > 0) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = GreenPrimary)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Goals Progress", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                                Text("$completed of $total completed", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
                            }
                            Text("${if (total > 0) (completed * 100 / total) else 0}%", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = GoldAccent)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { if (total > 0) completed.toFloat() / total else 0f },
                            modifier = Modifier.fillMaxWidth().height(8.dp),
                            color = GoldAccent,
                            trackColor = Color.White.copy(alpha = 0.3f)
                        )
                    }
                }
            }

            if (goals.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🎯", fontSize = 56.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No goals yet!", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextDark)
                        Text("Tap + to set your first Islamic goal", color = TextMedium, textAlign = TextAlign.Center)
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(goals, key = { it.id }) { goal ->
                        GoalCard(goal = goal, onToggle = { viewModel.toggleComplete(goal) }, onDelete = { viewModel.deleteGoal(goal) })
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }

    if (showDialog) {
        AddGoalDialog(onDismiss = { viewModel.hideDialog() }, onAdd = { title, desc, cat, date ->
            viewModel.addGoal(title, desc, cat, date)
            viewModel.hideDialog()
        })
    }
}

@Composable
fun GoalCard(goal: IslamicGoal, onToggle: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (goal.isCompleted) Color(0xFFF1F8E9) else SurfaceWhite),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = goal.isCompleted,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(checkedColor = GreenPrimary)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(shape = RoundedCornerShape(6.dp), color = GreenPrimary.copy(alpha = 0.1f)) {
                        Text("  ${goal.category}  ", fontSize = 10.sp, color = GreenPrimary, modifier = Modifier.padding(vertical = 2.dp))
                    }
                    if (goal.targetDate.isNotBlank()) {
                        Text("📅 ${goal.targetDate}", fontSize = 10.sp, color = TextMedium)
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    goal.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = if (goal.isCompleted) TextMedium else TextDark,
                    textDecoration = if (goal.isCompleted) TextDecoration.LineThrough else null
                )
                if (goal.description.isNotBlank()) {
                    Text(goal.description, fontSize = 12.sp, color = TextMedium, maxLines = 2)
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFE57373))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGoalDialog(onDismiss: () -> Unit, onAdd: (String, String, String, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("General") }
    var targetDate by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Islamic Goal", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title, onValueChange = { title = it },
                    label = { Text("Goal Title *") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GreenPrimary),
                    singleLine = true
                )
                OutlinedTextField(
                    value = description, onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GreenPrimary),
                    maxLines = 3
                )
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(
                        value = selectedCategory, onValueChange = {}, readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GreenPrimary)
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        GOAL_CATEGORIES.forEach { cat ->
                            DropdownMenuItem(text = { Text(cat) }, onClick = { selectedCategory = cat; expanded = false })
                        }
                    }
                }
                OutlinedTextField(
                    value = targetDate, onValueChange = { targetDate = it },
                    label = { Text("Target Date (e.g. 2026-06-01)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GreenPrimary),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (title.isNotBlank()) onAdd(title, description, selectedCategory, targetDate) },
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
            ) { Text("Add Goal") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextMedium) }
        }
    )
}
