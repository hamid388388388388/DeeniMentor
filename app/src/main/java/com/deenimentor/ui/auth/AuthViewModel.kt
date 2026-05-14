package com.deenimentor.ui.auth

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

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val userId: String) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val auth = Firebase.auth
    private val repo = AppRepository(AppDatabase.getDatabase(application))

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    val currentUserId: String? get() = auth.currentUser?.uid
    val isLoggedIn: Boolean get() = auth.currentUser != null

    fun register(name: String, email: String, password: String) {
        _authState.value = AuthState.Loading
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val uid = result.user!!.uid
                // Create minimal profile; growth path selected in onboarding
                viewModelScope.launch {
                    repo.saveProfile(UserProfile(userId = uid, displayName = name, email = email, growthPath = ""))
                }
                _authState.value = AuthState.Success(uid)
            }
            .addOnFailureListener { e ->
                _authState.value = AuthState.Error(e.message ?: "Registration failed")
            }
    }

    fun login(email: String, password: String) {
        _authState.value = AuthState.Loading
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                _authState.value = AuthState.Success(result.user!!.uid)
            }
            .addOnFailureListener { e ->
                _authState.value = AuthState.Error(e.message ?: "Login failed")
            }
    }

    fun logout() {
        auth.signOut()
        _authState.value = AuthState.Idle
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }

    suspend fun hasCompletedOnboarding(): Boolean {
        return try {
            val uid = currentUserId ?: return false
            val profile = repo.getProfile(uid)
            profile != null && profile.growthPath.isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }

    suspend fun saveGrowthPath(path: String) {
        val uid = currentUserId ?: return
        val existing = repo.getProfile(uid)
        val updated = existing?.copy(growthPath = path)
            ?: UserProfile(userId = uid, displayName = "", email = "", growthPath = path)
        repo.saveProfile(updated)
    }
}
