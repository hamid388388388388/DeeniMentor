@file:OptIn(ExperimentalMaterial3Api::class)
package com.deenimentor.ui.quran

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deenimentor.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONArray

// ─────────────────────────────────────────────────────────
// Data classes
// ─────────────────────────────────────────────────────────
data class SurahInfo(
    val number: Int,
    val nameRoman: String,
    val nameAr: String,
    val nameEn: String,
    val totalAyahs: Int
)

data class AyahInfo(
    val number: Int,
    val arabic: String,
    val english: String
)

// ─────────────────────────────────────────────────────────
// Thread-safe singleton cache.
// JSON (3 MB, 6 236 unique ayahs) is parsed ONCE on an IO
// thread, then kept in memory.  A Mutex prevents two
// coroutines from parsing simultaneously on first launch.
// ─────────────────────────────────────────────────────────
object QuranCache {
    private val mutex = Mutex()
    private var surahList: List<SurahInfo>? = null
    private var ayahMap: Map<Int, List<AyahInfo>>? = null

    suspend fun getSurahList(context: Context): List<SurahInfo> {
        surahList?.let { return it }
        load(context)
        return surahList ?: emptyList()
    }

    suspend fun getAyahs(context: Context, surahNo: Int): List<AyahInfo> {
        ayahMap?.let { return it[surahNo] ?: emptyList() }
        load(context)
        return ayahMap?.get(surahNo) ?: emptyList()
    }

    private suspend fun load(context: Context) = mutex.withLock {
        // Double-checked: another coroutine may have loaded while we waited
        if (surahList != null) return@withLock

        withContext(Dispatchers.IO) {
            try {
                val json = context.assets.open("quran_data.json")
                    .bufferedReader(Charsets.UTF_8).use { it.readText() }
                val arr = JSONArray(json)

                val surahs  = linkedMapOf<Int, SurahInfo>()
                val ayahs   = linkedMapOf<Int, MutableList<AyahInfo>>()

                for (i in 0 until arr.length()) {
                    val o   = arr.getJSONObject(i)
                    val sNo = o.getInt("surah_no")

                    if (!surahs.containsKey(sNo)) {
                        surahs[sNo] = SurahInfo(
                            number     = sNo,
                            nameRoman  = o.getString("surah_name_roman"),
                            nameAr     = o.getString("surah_name_ar"),
                            nameEn     = o.getString("surah_name_en"),
                            totalAyahs = o.getInt("total_ayah_surah")
                        )
                    }

                    ayahs.getOrPut(sNo) { mutableListOf() }.add(
                        AyahInfo(
                            number  = o.getInt("ayah_no_surah"),
                            arabic  = o.getString("ayah_ar"),
                            english = o.getString("ayah_en")
                        )
                    )
                }

                surahList = surahs.values.toList()
                ayahMap   = ayahs.mapValues { (_, v) -> v.sortedBy { it.number } }

            } catch (e: Exception) {
                e.printStackTrace()
                surahList = emptyList()
                ayahMap   = emptyMap()
            }
        }
    }
}

// ─────────────────────────────────────────────────────────
// QuranScreen — top-level
// ─────────────────────────────────────────────────────────
@Composable
fun QuranScreen(viewModel: QuranViewModel, onBack: () -> Unit) {
    val totalPages  by viewModel.totalPages.collectAsState()
    val lastSession by viewModel.lastSession.collectAsState()
    val saveSuccess by viewModel.saveSuccess.collectAsState()
    val allSessions by viewModel.allSessions.collectAsState()

    var selectedTab   by remember { mutableStateOf(0) }
    var selectedSurah by remember { mutableStateOf<SurahInfo?>(null) }

    LaunchedEffect(saveSuccess) { if (saveSuccess) viewModel.resetSaveSuccess() }

    if (selectedSurah != null) {
        SurahReadingScreen(surah = selectedSurah!!, onBack = { selectedSurah = null })
        return
    }

    val isDark = MaterialTheme.colorScheme.background == DarkBackground

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Holy Quran", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = GreenPrimary, titleContentColor = Color.White
                )
            )
        },
        containerColor = if (isDark) DarkBackground else BackgroundLight
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            TabRow(selectedTabIndex = selectedTab, containerColor = GreenPrimary, contentColor = Color.White) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 },
                    text = { Text("📖 Read", fontSize = 13.sp) })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 },
                    text = { Text("📊 Tracker", fontSize = 13.sp) })
                Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 },
                    text = { Text("📜 History", fontSize = 13.sp) })
            }
            when (selectedTab) {
                0 -> QuranReadTab(onSurahSelected = { selectedSurah = it }, isDark = isDark)
                1 -> QuranTrackerTab(viewModel = viewModel, totalPages = totalPages,
                        lastSession = lastSession, isDark = isDark)
                2 -> QuranHistoryTab(sessions = allSessions, isDark = isDark)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────
// Surah List Tab
// ─────────────────────────────────────────────────────────
@Composable
fun QuranReadTab(onSurahSelected: (SurahInfo) -> Unit, isDark: Boolean) {
    val context   = LocalContext.current
    val cardBg    = if (isDark) DarkCard else SurfaceWhite
    val textColor = if (isDark) DarkTextPrimary else TextDark
    val subColor  = if (isDark) DarkTextSecondary else TextMedium

    var searchQuery by remember { mutableStateOf("") }
    var surahList   by remember { mutableStateOf<List<SurahInfo>>(emptyList()) }
    var isLoading   by remember { mutableStateOf(true) }
    var loadError   by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isLoading = true
        loadError = false
        try {
            surahList = QuranCache.getSurahList(context)
            loadError = surahList.isEmpty()
        } catch (e: Exception) {
            loadError = true
        } finally {
            isLoading = false
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search Surah...") },
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = GreenPrimary) },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GreenPrimary)
        )

        when {
            isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = GreenPrimary)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Loading Quran...", color = subColor)
                    }
                }
            }
            loadError -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("⚠️", fontSize = 40.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Failed to load Quran data", color = textColor, fontWeight = FontWeight.Bold)
                        Text("Please restart the app", color = subColor, fontSize = 13.sp)
                    }
                }
            }
            else -> {
                val q        = searchQuery.trim()
                val filtered = if (q.isEmpty()) surahList else surahList.filter {
                    it.nameRoman.contains(q, ignoreCase = true) ||
                    it.nameEn.contains(q, ignoreCase = true) ||
                    it.number.toString() == q
                }

                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(filtered, key = { it.number }) { surah ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                                .clickable { onSurahSelected(surah) },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = cardBg),
                            elevation = CardDefaults.cardElevation(1.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(GreenPrimary.copy(alpha = 0.12f), RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("${surah.number}", fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold, color = GreenPrimary)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(surah.nameRoman, fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold, color = textColor)
                                    Text(surah.nameEn, fontSize = 11.sp, color = subColor)
                                    Text("${surah.totalAyahs} ayahs", fontSize = 11.sp, color = subColor)
                                }
                                Text(surah.nameAr, fontSize = 17.sp,
                                    color = GreenPrimary, fontWeight = FontWeight.Medium)
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(Icons.Default.ChevronRight, contentDescription = null,
                                    tint = subColor, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                    item { Spacer(modifier = Modifier.height(24.dp)) }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────
// Surah Reading Screen
// ─────────────────────────────────────────────────────────
@Composable
fun SurahReadingScreen(surah: SurahInfo, onBack: () -> Unit) {
    val context  = LocalContext.current
    val isDark   = MaterialTheme.colorScheme.background == DarkBackground
    val subColor = if (isDark) DarkTextSecondary else TextMedium

    var ayahs     by remember { mutableStateOf<List<AyahInfo>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf(false) }

    LaunchedEffect(surah.number) {
        isLoading = true
        loadError = false
        try {
            ayahs     = QuranCache.getAyahs(context, surah.number)
            loadError = ayahs.isEmpty()
        } catch (e: Exception) {
            loadError = true
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("${surah.number}. ${surah.nameRoman}", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = GreenPrimary, titleContentColor = Color.White
                )
            )
        },
        containerColor = if (isDark) DarkBackground else BackgroundLight
    ) { padding ->
        when {
            isLoading -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = GreenPrimary)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Loading ${surah.nameRoman}...", color = subColor)
                    }
                }
            }
            loadError -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("⚠️", fontSize = 40.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Could not load ayahs", color = if (isDark) DarkTextPrimary else TextDark,
                            fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = onBack,
                            colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)) {
                            Text("Go Back")
                        }
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 16.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = GreenPrimary)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp).fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(surah.nameAr, fontSize = 26.sp,
                                    fontWeight = FontWeight.Bold, color = Color.White)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(surah.nameRoman, fontSize = 17.sp,
                                    color = Color.White.copy(alpha = 0.9f))
                                Text(surah.nameEn, fontSize = 13.sp,
                                    color = Color.White.copy(alpha = 0.7f))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("${surah.totalAyahs} Ayahs", fontSize = 13.sp,
                                    color = Color.White.copy(alpha = 0.8f))
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    items(ayahs, key = { it.number }) { ayah ->
                        val cardBg = if (isDark) DarkCard else SurfaceWhite
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = cardBg),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .background(GreenPrimary, RoundedCornerShape(14.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("${ayah.number}", fontSize = 11.sp,
                                        color = Color.White, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = ayah.arabic,
                                    fontSize = 22.sp,
                                    textAlign = TextAlign.End,
                                    modifier = Modifier.fillMaxWidth(),
                                    color = if (isDark) DarkTextPrimary else TextDark,
                                    lineHeight = 36.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                HorizontalDivider(color = GreenPrimary.copy(alpha = 0.15f))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = ayah.english,
                                    fontSize = 13.sp,
                                    color = if (isDark) DarkTextSecondary else TextMedium,
                                    lineHeight = 20.sp
                                )
                            }
                        }
                    }
                    item { Spacer(modifier = Modifier.height(24.dp)) }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────
// Tracker Tab
// ─────────────────────────────────────────────────────────
@Composable
fun QuranTrackerTab(
    viewModel: QuranViewModel,
    totalPages: Int,
    lastSession: com.deenimentor.data.model.QuranSession?,
    isDark: Boolean
) {
    val context  = LocalContext.current
    val cardBg   = if (isDark) DarkCard else SurfaceWhite

    var surahList          by remember { mutableStateOf<List<SurahInfo>>(emptyList()) }
    var selectedSurahIndex by remember { mutableStateOf(0) }
    var ayahNumber         by remember { mutableStateOf("1") }
    var expanded           by remember { mutableStateOf(false) }
    val snackbarHostState  = remember { SnackbarHostState() }
    val saveSuccess        by viewModel.saveSuccess.collectAsState()

    LaunchedEffect(Unit) { surahList = QuranCache.getSurahList(context) }

    LaunchedEffect(saveSuccess) {
        if (saveSuccess) {
            snackbarHostState.showSnackbar("✅ Saved successfully!")
            viewModel.resetSaveSuccess()
        }
    }

    val totalQuranAyahs = 6236f
    val progress = (totalPages / totalQuranAyahs).coerceIn(0f, 1f)

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Quran Progress", fontWeight = FontWeight.Bold, fontSize = 18.sp,
                        color = if (isDark) DarkTextPrimary else TextDark)
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.size(130.dp),
                            strokeWidth = 12.dp,
                            color = GreenPrimary,
                            trackColor = Color(0xFFE8F5E9)
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("${(progress * 100).toInt()}%", fontSize = 28.sp,
                                fontWeight = FontWeight.Bold, color = GreenPrimary)
                            Text("Complete", fontSize = 11.sp,
                                color = if (isDark) DarkTextSecondary else TextMedium)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("$totalPages / 6236 ayahs read", fontSize = 14.sp,
                        color = if (isDark) DarkTextSecondary else TextMedium)
                    lastSession?.let {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Last: ${it.surahName}, Ayah ${it.ayahNumber}",
                            fontSize = 12.sp, color = GreenPrimary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (surahList.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoStories, contentDescription = null, tint = GreenPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Log Today's Reading", fontWeight = FontWeight.Bold,
                                fontSize = 16.sp, color = if (isDark) DarkTextPrimary else TextDark)
                        }
                        Spacer(modifier = Modifier.height(16.dp))

                        Text("Select Surah", fontSize = 12.sp, fontWeight = FontWeight.Medium,
                            color = if (isDark) DarkTextSecondary else TextMedium)
                        Spacer(modifier = Modifier.height(4.dp))

                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded }
                        ) {
                            val sel = surahList.getOrNull(selectedSurahIndex)
                            OutlinedTextField(
                                value = if (sel != null) "${sel.number}. ${sel.nameRoman}" else "",
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GreenPrimary)
                            )
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false },
                                modifier = Modifier.height(300.dp)
                            ) {
                                surahList.forEachIndexed { index, surah ->
                                    DropdownMenuItem(
                                        text = { Text("${surah.number}. ${surah.nameRoman}", fontSize = 13.sp) },
                                        onClick = { selectedSurahIndex = index; expanded = false }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Last Ayah Read", fontSize = 12.sp, fontWeight = FontWeight.Medium,
                            color = if (isDark) DarkTextSecondary else TextMedium)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = ayahNumber,
                            onValueChange = { ayahNumber = it.filter { c -> c.isDigit() } },
                            label = { Text("Ayah Number") },
                            placeholder = { Text("e.g. 25") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GreenPrimary)
                        )

                        Spacer(modifier = Modifier.height(20.dp))
                        val sel = surahList.getOrNull(selectedSurahIndex)
                        Button(
                            onClick = {
                                if (sel != null && ayahNumber.isNotBlank()) {
                                    viewModel.saveSession(
                                        surahNumber = sel.number,
                                        surahName   = sel.nameRoman,
                                        ayahNumber  = ayahNumber.toIntOrNull() ?: 1,
                                        pagesRead   = ayahNumber.toIntOrNull() ?: 1
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Save Reading", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

// ─────────────────────────────────────────────────────────
// History Tab
// ─────────────────────────────────────────────────────────
@Composable
fun QuranHistoryTab(sessions: List<com.deenimentor.data.model.QuranSession>, isDark: Boolean) {
    val cardBg    = if (isDark) DarkCard else SurfaceWhite
    val textColor = if (isDark) DarkTextPrimary else TextDark
    val subColor  = if (isDark) DarkTextSecondary else TextMedium

    if (sessions.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("📖", fontSize = 48.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Text("No reading history yet", fontSize = 18.sp,
                    fontWeight = FontWeight.Bold, color = textColor)
                Text("Start reading and log your sessions", color = subColor)
            }
        }
        return
    }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp)) {
        item { Spacer(modifier = Modifier.height(12.dp)) }
        items(sessions, key = { it.id }) { session ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(GreenPrimary.copy(alpha = 0.1f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) { Text("📖", fontSize = 18.sp) }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(session.surahName, fontWeight = FontWeight.Medium, color = textColor)
                        Text("Ayah ${session.ayahNumber}", fontSize = 12.sp, color = subColor)
                        Text(session.date, fontSize = 11.sp, color = GreenPrimary)
                    }
                    Text("Ayah ${session.ayahNumber}", fontWeight = FontWeight.Bold,
                        color = GreenPrimary, fontSize = 13.sp)
                }
            }
        }
        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}
