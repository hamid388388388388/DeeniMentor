package com.deenimentor.ui.dua

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.deenimentor.data.db.AppDatabase
import com.deenimentor.data.model.Dua
import com.deenimentor.data.repository.AppRepository
import com.google.firebase.ktx.Firebase
import com.google.firebase.auth.ktx.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DuaViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = AppRepository(AppDatabase.getDatabase(application))
    private val userId get() = Firebase.auth.currentUser?.uid ?: ""

    private val _duas = MutableStateFlow<List<Dua>>(emptyList())
    val duas: StateFlow<List<Dua>> = _duas.asStateFlow()

    private val _searchResults = MutableStateFlow<List<Dua>>(emptyList())
    val searchResults: StateFlow<List<Dua>> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    init {
        loadDuas()
    }

    private fun loadDuas() {
        viewModelScope.launch {
            val count = repo.getDuaCount(userId)
            if (count == 0) seedDuas()
            repo.getAllDuas(userId).collect { _duas.value = it }
        }
    }

    fun search(query: String) {
        viewModelScope.launch {
            if (query.isBlank()) {
                _isSearching.value = false
                _searchResults.value = emptyList()
            } else {
                _isSearching.value = true
                _searchResults.value = repo.searchDuas(userId, query)
            }
        }
    }

    fun toggleFavorite(dua: Dua) {
        viewModelScope.launch {
            repo.toggleFavorite(dua.id, !dua.isFavorite)
        }
    }

    fun setCategory(category: String) {
        _selectedCategory.value = category
    }

    private suspend fun seedDuas() {
        val seed = listOf(
            Dua(userId = userId, category = "Morning", title = "Morning Dua", arabicText = "اللَّهُمَّ بِكَ أَصْبَحْنَا", transliteration = "Allahumma bika asbahna", translation = "O Allah, by Your grace we have entered the morning", reference = "Abu Dawud"),
            Dua(userId = userId, category = "Morning", title = "Waking Up", arabicText = "الْحَمْدُ لِلَّهِ الَّذِي أَحْيَانَا بَعْدَ مَا أَمَاتَنَا", transliteration = "Alhamdu lillahil-ladhi ahyana ba'da ma amatana", translation = "All praise is for Allah who gave us life after death", reference = "Bukhari"),
            Dua(userId = userId, category = "Evening", title = "Evening Dua", arabicText = "اللَّهُمَّ بِكَ أَمْسَيْنَا", transliteration = "Allahumma bika amsayna", translation = "O Allah, by Your grace we have entered the evening", reference = "Abu Dawud"),
            Dua(userId = userId, category = "Prayer", title = "Before Prayer", arabicText = "سُبْحَانَكَ اللَّهُمَّ وَبِحَمْدِكَ", transliteration = "Subhanakal-lahumma wabihamdik", translation = "Glory and praise be to You, O Allah", reference = "Muslim"),
            Dua(userId = userId, category = "Prayer", title = "After Prayer", arabicText = "أَسْتَغْفِرُ اللَّهَ", transliteration = "Astaghfirullah", translation = "I seek forgiveness from Allah", reference = "Muslim"),
            Dua(userId = userId, category = "Eating", title = "Before Eating", arabicText = "بِسْمِ اللَّهِ", transliteration = "Bismillah", translation = "In the name of Allah", reference = "Bukhari"),
            Dua(userId = userId, category = "Eating", title = "After Eating", arabicText = "الْحَمْدُ لِلَّهِ الَّذِي أَطْعَمَنَا", transliteration = "Alhamdu lillahil-ladhi at'amana", translation = "All praise to Allah who fed us", reference = "Abu Dawud"),
            Dua(userId = userId, category = "Travel", title = "Before Travel", arabicText = "سُبْحَانَ الَّذِي سَخَّرَ لَنَا هَذَا", transliteration = "Subhanal-ladhi sakhkhara lana hadha", translation = "Glory to Him who subjected this to us", reference = "Muslim"),
            Dua(userId = userId, category = "Protection", title = "Morning Protection", arabicText = "بِسْمِ اللَّهِ الَّذِي لَا يَضُرُّ مَعَ اسْمِهِ شَيْءٌ", transliteration = "Bismillahil-ladhi la yadurru ma'asmihi shay", translation = "In the name of Allah with whose name nothing can cause harm", reference = "Abu Dawud"),
            Dua(userId = userId, category = "Forgiveness", title = "Seeking Forgiveness", arabicText = "رَبَّنَا ظَلَمْنَا أَنفُسَنَا", transliteration = "Rabbana dhalamna anfusana", translation = "Our Lord, we have wronged ourselves", reference = "Quran 7:23"),
            Dua(userId = userId, category = "Health", title = "Dua for Healing", arabicText = "اللَّهُمَّ رَبَّ النَّاسِ أَذْهِبِ الْبَأْسَ", transliteration = "Allahumma Rabban-nas adhhibil-ba's", translation = "O Allah, Lord of mankind, remove the harm", reference = "Bukhari"),
            Dua(userId = userId, category = "Knowledge", title = "Dua for Knowledge", arabicText = "رَبِّ زِدْنِي عِلْمًا", transliteration = "Rabbi zidni 'ilma", translation = "My Lord, increase me in knowledge", reference = "Quran 20:114")
        )
        repo.insertAllDuas(seed)
    }
}
