@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.deenimentor.ui.dua

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deenimentor.data.model.Dua
import com.deenimentor.ui.theme.*

val DUA_CATEGORIES = listOf("All", "Morning", "Evening", "Prayer", "Eating", "Travel", "Protection", "Forgiveness", "Health", "Knowledge")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DuaScreen(
    viewModel: DuaViewModel,
    onBack: () -> Unit
) {
    val duas by viewModel.duas.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedDua by remember { mutableStateOf<Dua?>(null) }
    var showFavoritesOnly by remember { mutableStateOf(false) }

    val displayList = when {
        isSearching -> searchResults
        showFavoritesOnly -> duas.filter { it.isFavorite }
        selectedCategory == "All" -> duas
        else -> duas.filter { it.category == selectedCategory }
    }

    if (selectedDua != null) {
        DuaDetailSheet(
            dua = selectedDua!!,
            onDismiss = { selectedDua = null },
            onToggleFavorite = { viewModel.toggleFavorite(selectedDua!!) }
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dua Collection", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { showFavoritesOnly = !showFavoritesOnly }) {
                        Icon(
                            if (showFavoritesOnly) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorites",
                            tint = if (showFavoritesOnly) Color.Red else Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = GreenPrimary,
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = BackgroundLight
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it; viewModel.search(it) },
                placeholder = { Text("Search duas...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = ""; viewModel.search("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GreenPrimary,
                    unfocusedBorderColor = Color(0xFFDDDDDD)
                ),
                singleLine = true
            )

            // Category Chips
            if (!isSearching) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(DUA_CATEGORIES) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat && !showFavoritesOnly,
                            onClick = { viewModel.setCategory(cat); showFavoritesOnly = false },
                            label = { Text(cat, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GreenPrimary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Duas List
            if (displayList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🤲", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            if (showFavoritesOnly) "No favorites yet\nTap ♥ to save duas" else "No duas found",
                            textAlign = TextAlign.Center,
                            color = TextMedium
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(displayList, key = { it.id }) { dua ->
                        DuaCard(dua = dua, onClick = { selectedDua = dua }, onFavorite = { viewModel.toggleFavorite(dua) })
                    }
                }
            }
        }
    }
}

@Composable
fun DuaCard(dua: Dua, onClick: () -> Unit, onFavorite: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = RoundedCornerShape(8.dp), color = GreenPrimary.copy(alpha = 0.1f)) {
                        Text("  ${dua.category}  ", fontSize = 10.sp, color = GreenPrimary, modifier = Modifier.padding(vertical = 2.dp))
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(dua.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextDark)
                Spacer(modifier = Modifier.height(4.dp))
                Text(dua.arabicText, fontSize = 16.sp, color = GreenPrimary, textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(4.dp))
                Text(dua.translation, fontSize = 12.sp, color = TextMedium, maxLines = 2)
            }
            IconButton(onClick = onFavorite) {
                Icon(
                    if (dua.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (dua.isFavorite) Color.Red else Color(0xFFCCCCCC)
                )
            }
        }
    }
}

@Composable
fun DuaDetailSheet(dua: Dua, onDismiss: () -> Unit, onToggleFavorite: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(dua.title, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = onToggleFavorite) {
                        Icon(
                            if (dua.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (dua.isFavorite) Color.Red else Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = GreenPrimary, titleContentColor = Color.White)
            )
        },
        containerColor = BackgroundLight
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = SurfaceWhite), elevation = CardDefaults.cardElevation(4.dp)) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(dua.arabicText, fontSize = 24.sp, color = GreenPrimary, textAlign = TextAlign.Center, lineHeight = 40.sp, fontWeight = FontWeight.Medium)
                    Divider(modifier = Modifier.padding(vertical = 16.dp))
                    Text(dua.transliteration, fontSize = 14.sp, color = TextMedium, textAlign = TextAlign.Center, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(dua.translation, fontSize = 16.sp, color = TextDark, textAlign = TextAlign.Center, lineHeight = 24.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Surface(shape = RoundedCornerShape(8.dp), color = GreenPrimary.copy(alpha = 0.1f)) {
                        Text("  📖 ${dua.reference}  ", fontSize = 12.sp, color = GreenPrimary, modifier = Modifier.padding(vertical = 4.dp))
                    }
                }
            }
        }
    }
}
