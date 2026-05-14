package com.deenimentor.ui.quran

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.deenimentor.data.db.AppDatabase
import com.deenimentor.data.model.QuranSession
import com.deenimentor.data.repository.AppRepository
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class QuranViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = AppRepository(AppDatabase.getDatabase(application))

    private val _totalPages = MutableStateFlow(0)
    val totalPages: StateFlow<Int> = _totalPages

    private val _lastSession = MutableStateFlow<QuranSession?>(null)
    val lastSession: StateFlow<QuranSession?> = _lastSession

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess

    private val _allSessions = MutableStateFlow<List<QuranSession>>(emptyList())
    val allSessions: StateFlow<List<QuranSession>> = _allSessions

    init { loadData() }

    private suspend fun getUid(): String {
        var uid = Firebase.auth.currentUser?.uid
        var tries = 0
        while (uid == null && tries < 10) { delay(300); uid = Firebase.auth.currentUser?.uid; tries++ }
        return uid ?: ""
    }

    private fun loadData() {
        viewModelScope.launch {
            val uid = getUid()
            if (uid.isBlank()) return@launch
            repo.getAllQuranSessions(uid).collectLatest { sessions ->
                _allSessions.value = sessions
                _totalPages.value = sessions.sumOf { it.pagesRead }
                _lastSession.value = sessions.firstOrNull()
            }
        }
    }

    fun saveSession(surahNumber: Int, surahName: String, ayahNumber: Int, pagesRead: Int) {
        viewModelScope.launch {
            val uid = getUid()
            if (uid.isBlank()) return@launch
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            repo.insertQuranSession(
                QuranSession(
                    userId = uid,
                    date = today,
                    surahNumber = surahNumber,
                    surahName = surahName,
                    ayahNumber = ayahNumber,
                    pagesRead = pagesRead
                )
            )
            _saveSuccess.value = true
        }
    }

    fun resetSaveSuccess() { _saveSuccess.value = false }
}
