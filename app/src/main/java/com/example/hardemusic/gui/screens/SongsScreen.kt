package com.example.hardemusic.gui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.hardemusic.gui.AlphabetScrollbar
import com.example.hardemusic.gui.SongRow
import com.example.hardemusic.viewmodel.MainViewModel

@Composable
fun SongsScreen(viewModel: MainViewModel, navController: NavController) {
    val songs by viewModel.songsList.collectAsState()
    val sortedSongs = songs.sortedBy { it.title }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearSelection()
        }
    }

    val grouped = remember(sortedSongs) {
        sortedSongs.groupBy {
            val firstChar = it.title.firstOrNull()?.uppercaseChar()
            if (firstChar != null && firstChar.isLetter()) firstChar else firstChar ?: '#'
        }.toSortedMap()
    }

    val listState = rememberLazyListState()

    val indexMap = remember(grouped) {
        var index = 0
        buildMap<Char, Int> {
            grouped.forEach { (initial, songsByLetter) ->
                put(initial, index)
                index += 1 + songsByLetter.size
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 72.dp)
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(end = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            grouped.forEach { (initial, group) ->
                item {
                    Text(
                        text = initial.toString(),
                        color = Color.Cyan,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                    )
                }

                items(group) { song ->
                    SongRow(
                        song = song, onSongClick = { viewModel.playFrom(song) },
                        navController = navController, viewModel
                    )
                }
            }
        }

        AlphabetScrollbar(
            groupedItems = grouped,
            listState = listState,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(vertical = 16.dp),
            onScrollToIndex = { letter -> indexMap[letter] ?: 0 }
        )

        FloatingActionButton(
            onClick = { viewModel.playRandomSong() },
            containerColor = Color(0xFF58A6FF),
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
        ) {
            Icon(imageVector = Icons.Default.Shuffle, contentDescription = "Aleatorio")
        }
    }
}

