package com.example.hardemusic.gui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.hardemusic.data.Song
import com.example.hardemusic.gui.rememberSongArtwork
import com.example.hardemusic.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun SongSelectionScreen(
    date: String,
    viewModel: MainViewModel,
    navController: NavController,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.loadSongs()
        viewModel.loadDownloadedSongsGroupedByDate(context)
    }

    val songs = viewModel.selectedSongsForDay.value
    val selectedSongs = remember { mutableStateListOf<Song>() }
    var isSelectionMode by remember { mutableStateOf(false) }


    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (isSelectionMode) {
                        Text("${selectedSongs.size} seleccionadas")
                    } else {
                        Text(text = date)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (isSelectionMode) {
                            selectedSongs.clear()
                            isSelectionMode = false
                        } else {
                            onBack()
                        }
                    }) {
                        Icon(
                            imageVector = if (isSelectionMode) Icons.Default.Close else Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back or Cancel",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    if (isSelectionMode) {
                        IconButton(onClick = {
                            if (selectedSongs.isNotEmpty()) {
                                viewModel.setEditingSongs(selectedSongs)
                                navController.navigate("edit_multiple_songs")
                            }
                        }) {
                            Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Color.White)
                        }

                        IconButton(onClick = {
                            selectedSongs.clear()
                            selectedSongs.addAll(songs)
                        }) {
                            Icon(Icons.Default.SelectAll, contentDescription = "Seleccionar todo", tint = Color.White)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black,
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = Color.Black
    ) { innerPadding ->
        LazyColumn(contentPadding = innerPadding) {
            items(songs) { song ->
                val isSelected = selectedSongs.contains(song)

                val artworkModel = rememberSongArtwork(context, song)

                val imageModel: Any? = when {
                    artworkModel != null -> artworkModel
                    song.albumArtUri != null -> song.albumArtUri
                    else -> null
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (isSelected) Color.DarkGray else Color.Transparent)
                        .combinedClickable(
                            onClick = {
                                if (isSelectionMode) {
                                    if (isSelected) selectedSongs.remove(song)
                                    else selectedSongs.add(song)

                                    if (selectedSongs.isEmpty()) {
                                        isSelectionMode = false
                                    }
                                } else {
                                    viewModel.setEditingSong(song)
                                    navController.navigate("edit_song")
                                }
                            },
                            onLongClick = {
                                if (!isSelectionMode) {
                                    isSelectionMode = true
                                    selectedSongs.add(song)
                                }
                            }
                        )
                        .padding(8.dp)
                ) {
                    if (imageModel != null) {
                        AsyncImage(
                            model = imageModel,
                            contentDescription = null,
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            modifier = Modifier.size(50.dp),
                            tint = Color.Gray
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Text(song.title, color = Color.White)
                        Text("${song.artist}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }
            }
        }
    }
}