package com.deenimentor.ui.goals

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.deenimentor.data.db.AppDatabase
import com.deenimentor.data.model.IslamicGoal
import com.deenimentor.data.repository.AppRepository
import com.google.firebase.ktx.Firebase
import com.google.firebase.auth.ktx.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GoalsViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = AppRepository(AppDatabase.getDatabase(application))
    private val userId get() = Firebase.auth.currentUser?.uid ?: ""

    private val _goals = MutableStateFlow<List<IslamicGoal>>(emptyList())
    val goals: StateFlow<List<IslamicGoal>> = _goals.asStateFlow()

    private val _showAddDialog = MutableStateFlow(false)
    val showAddDialog: StateFlow<Boolean> = _showAddDialog.asStateFlow()

    init { loadGoals() }

    private fun loadGoals() {
        viewModelScope.launch {
            repo.getAllGoals(userId).collect { _goals.value = it }
        }
    }

    fun addGoal(title: String, description: String, category: String, targetDate: String) {
        viewModelScope.launch {
            repo.insertGoal(IslamicGoal(
                userId = userId, title = title,
                description = description, category = category, targetDate = targetDate
            ))
        }
    }

    fun toggleComplete(goal: IslamicGoal) {
        viewModelScope.launch { repo.setGoalCompleted(goal.id, !goal.isCompleted) }
    }

    fun deleteGoal(goal: IslamicGoal) {
        viewModelScope.launch { repo.deleteGoal(goal) }
    }

    fun showDialog() { _showAddDialog.value = true }
    fun hideDialog() { _showAddDialog.value = false }
}
