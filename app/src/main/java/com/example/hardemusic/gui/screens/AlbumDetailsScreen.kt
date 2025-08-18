package com.example.hardemusic.gui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.hardemusic.data.Album
import com.example.hardemusic.data.AppText
import com.example.hardemusic.data.Song
import com.example.hardemusic.gui.SongOptionsMenu
import com.example.hardemusic.viewmodel.MainViewModel

@Composable
fun AlbumDetailScreen(
    album: Album,
    songs: List<Song>,
    onBack: () -> Unit,
    mainViewModel: MainViewModel,
    navController: NavHostController,
    hasNowPlayingBarAppeared: Boolean
) {
    val scrollState = rememberScrollState()
    val currentSong by mainViewModel.currentSong.collectAsState()


    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .background(Color.Black)
            .padding(16.dp)
    ) {

        IconButton(
            onClick = onBack,
            modifier = Modifier.align(Alignment.Start)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Volver",
                tint = Color.White
            )
        }

        Spacer(modifier = Modifier.height(8.dp))


        Image(
            painter = rememberAsyncImagePainter(album.albumArtUri),
            contentDescription = "Carátula del álbum",
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(16.dp))


        Text(album.name, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text(album.artist, color = Color.Gray, fontSize = 16.sp)

        Spacer(modifier = Modifier.height(16.dp))


        Button(
            onClick = {
                val alternatives = songs.filterNot {
                    val current = mainViewModel.currentSong.value
                    current != null && it.title == current.title && it.artist == current.artist
                }
                if (alternatives.isNotEmpty()) {
                    mainViewModel.playFrom(alternatives.random(), songs,contextType = "album")
                } else if (songs.isNotEmpty()) {
                    mainViewModel.playFrom(songs.first(), songs,contextType = "album")
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF58A6FF),
                contentColor = Color.White
            ),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Icon(imageVector = Icons.Default.Shuffle, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(AppText.shuffelAlbumButton)
        }

        Spacer(modifier = Modifier.height(24.dp))


        val orderedSongs = songs.sortedBy { it.trackNumber ?: Int.MAX_VALUE }


        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            orderedSongs.forEach { song ->
                val isSelected = currentSong?.uri == song.uri

                val backgroundColor by animateColorAsState(
                    if (isSelected) Color(0x502D5DFF) else Color.Transparent,
                    label = "song selection background"
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(backgroundColor)
                        .padding(vertical = 8.dp, horizontal = 12.dp)
                        .clickable {
                            mainViewModel.playFrom(song, songs,contextType = "album")
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        text = "${song.trackNumber ?: "-"}",
                        color = Color.White,
                        fontSize = 14.sp,
                        modifier = Modifier.width(32.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(song.title, color = Color.White, fontSize = 16.sp, maxLines = 1)
                        Text(song.artist, color = Color.Gray, fontSize = 14.sp, maxLines = 1)
                    }

                    var expanded by remember { mutableStateOf(false) }

                    IconButton(onClick = { expanded = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Más opciones",
                            tint = Color.White
                        )
                    }

                    SongOptionsMenu(
                        expanded = expanded,
                        onDismiss = { expanded = false },
                        song = song,
                        navController = navController,
                        mainViewModel = mainViewModel,
                        hasNowPlayingBarAppeared = hasNowPlayingBarAppeared
                    )

                }
            }
        }

        Spacer(modifier = Modifier.height(72.dp))
    }
}