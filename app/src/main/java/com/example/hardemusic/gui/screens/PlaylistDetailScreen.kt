package com.example.hardemusic.gui.screens


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.hardemusic.data.AppText
import com.example.hardemusic.gui.AlphabetScrollbar
import com.example.hardemusic.gui.SongRow
import com.example.hardemusic.viewmodel.MainViewModel
import com.example.hardemusic.viewmodel.PlaylistViewModel



@Composable
fun PlaylistDetailScreen(
    playlistName: String,
    playlistViewModel: PlaylistViewModel,
    onBack: () -> Unit,
    mainViewModel: MainViewModel,
    navController: NavController
) {
    val playlists by playlistViewModel.playlists.collectAsState()
    val playlist = remember(playlists, playlistName) {
        playlists.firstOrNull { it.name == playlistName }
    } ?: return


    if (playlist.songs.size == 0) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = AppText.addSongstoPlaylistTitle,
                color = Color.Gray,
                fontSize = 16.sp
            )
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .padding(16.dp)
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Volver",
                tint = Color.White
            )
        }

        Text(
            text = playlist.name,
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
        )

        val groupedSongs = groupSongsByTitleFirstLetter(playlist.songs)
        val listState = rememberLazyListState()

        Row(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                state = listState,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 4.dp)
            ) {
                items(playlist.songs) { song ->
                    SongRow(
                        song = song,
                        onSongClick = { clicked ->
                            mainViewModel.playFrom(clicked, playlist.songs, playlist.name,contextType = "playlist",playlistName=playlist.name)
                        },
                        navController = navController,
                        mainViewModel = mainViewModel
                    )
                }
            }

            AlphabetScrollbar(
                groupedItems = groupedSongs,
                listState = listState,
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .width(20.dp),
                onScrollToIndex = { letter ->
                    playlist.songs.indexOfFirst { it.title.startsWith(letter, ignoreCase = true) }
                        .takeIf { it != -1 } ?: 0
                }
            )
        }
    }
}