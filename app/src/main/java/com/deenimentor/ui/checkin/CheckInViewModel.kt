package com.deenimentor.ui.checkin

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.deenimentor.data.db.AppDatabase
import com.deenimentor.data.model.DailyCheckIn
import com.deenimentor.data.repository.AppRepository
import com.google.firebase.ktx.Firebase
import com.google.firebase.auth.ktx.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class SalahEntry(
    val name: String,
    var done: Boolean = false,
    var jamaat: Boolean = false
)

data class CheckInFormState(
    val salah: List<SalahEntry> = listOf(
        SalahEntry("Fajr"), SalahEntry("Zuhr"), SalahEntry("Asr"),
        SalahEntry("Maghrib"), SalahEntry("Isha")
    ),
    val sleepHours: Float = 7f,
    val mood: Int = 3,
    val productivity: Int = 3,
    val goodDeeds: String = "",
    val struggles: String = ""
)

sealed class CheckInSaveState {
    object Idle : CheckInSaveState()
    object Loading : CheckInSaveState()
    object Success : CheckInSaveState()
    data class Error(val message: String) : CheckInSaveState()
    object AlreadyDone : CheckInSaveState()
}

class CheckInViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = AppRepository(AppDatabase.getDatabase(application))
    private val auth = Firebase.auth

    private val _formState = MutableStateFlow(CheckInFormState())
    val formState: StateFlow<CheckInFormState> = _formState

    private val _saveState = MutableStateFlow<CheckInSaveState>(CheckInSaveState.Idle)
    val saveState: StateFlow<CheckInSaveState> = _saveState

    fun updateSalahDone(index: Int, done: Boolean) {
        val updated = _formState.value.salah.toMutableList()
        updated[index] = updated[index].copy(done = done, jamaat = if (!done) false else updated[index].jamaat)
        _formState.value = _formState.value.copy(salah = updated)
    }

    fun updateSalahJamaat(index: Int, jamaat: Boolean) {
        val updated = _formState.value.salah.toMutableList()
        updated[index] = updated[index].copy(jamaat = jamaat)
        _formState.value = _formState.value.copy(salah = updated)
    }

    fun updateSleep(hours: Float) { _formState.value = _formState.value.copy(sleepHours = hours) }
    fun updateMood(mood: Int) { _formState.value = _formState.value.copy(mood = mood) }
    fun updateProductivity(p: Int) { _formState.value = _formState.value.copy(productivity = p) }
    fun updateGoodDeeds(text: String) { _formState.value = _formState.value.copy(goodDeeds = text) }
    fun updateStruggles(text: String) { _formState.value = _formState.value.copy(struggles = text) }

    fun submitCheckIn() {
        val uid = auth.currentUser?.uid ?: return
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val form = _formState.value

        _saveState.value = CheckInSaveState.Loading
        viewModelScope.launch {
            val existing = repo.getCheckInByDate(uid, today)
            if (existing != null) {
                _saveState.value = CheckInSaveState.AlreadyDone
                return@launch
            }
            val s = form.salah
            val checkIn = DailyCheckIn(
                date = today,
                userId = uid,
                fajrDone = s[0].done, fajrJamaat = s[0].jamaat,
                zuhrDone = s[1].done, zuhrJamaat = s[1].jamaat,
                asrDone = s[2].done, asrJamaat = s[2].jamaat,
                maghribDone = s[3].done, maghribJamaat = s[3].jamaat,
                ishaDone = s[4].done, ishaJamaat = s[4].jamaat,
                sleepHours = form.sleepHours,
                mood = form.mood,
                productivity = form.productivity,
                goodDeeds = form.goodDeeds,
                struggles = form.struggles
            )
            try {
                repo.saveCheckIn(checkIn)
                _saveState.value = CheckInSaveState.Success
            } catch (e: Exception) {
                _saveState.value = CheckInSaveState.Error(e.message ?: "Failed to save")
            }
        }
    }

    fun resetSaveState() { _saveState.value = CheckInSaveState.Idle }
}
