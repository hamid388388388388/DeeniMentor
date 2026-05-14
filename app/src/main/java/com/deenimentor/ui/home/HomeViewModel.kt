package com.deenimentor.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.deenimentor.data.db.AppDatabase
import com.deenimentor.data.model.UserProfile
import com.deenimentor.data.repository.AppRepository
import com.google.firebase.ktx.Firebase
import com.google.firebase.auth.ktx.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = AppRepository(AppDatabase.getDatabase(application))
    private val auth = Firebase.auth

    private val _profile = MutableStateFlow<UserProfile?>(null)
    val profile: StateFlow<UserProfile?> = _profile

    init {
        loadProfile()
    }

    private fun loadProfile() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                val p = repo.getProfile(uid)
                _profile.value = p
            } catch (e: Exception) {
                // DB error — silently ignore, profile stays null
            }
        }
    }
}
