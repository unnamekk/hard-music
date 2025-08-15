package com.example.hardemusic.gui.screens

import android.annotation.SuppressLint
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.example.hardemusic.gui.ScrollingText
import com.example.hardemusic.gui.SongOptionsMenu
import com.example.hardemusic.gui.rememberSongArtwork
import com.example.hardemusic.viewmodel.MainViewModel

@SuppressLint("DefaultLocale", "StateFlowValueCalledInComposition")
@Composable
fun PlayerScreen(viewModel: MainViewModel,navController: NavHostController, onBack: () -> Unit) {
    val currentSong by viewModel.currentSong.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val durationMs by viewModel.currentDuration.collectAsState()
    val currentPosition by viewModel.currentPosition.collectAsState()
    val shuffleMode by viewModel.shuffleMode.collectAsState()
    val repeatMode by viewModel.repeatMode.collectAsState()

    LaunchedEffect(currentSong) {
        if (currentSong == null) {
            navController.popBackStack()
            viewModel.markNowPlayingBarAsNotAppeared()
        }
    }

    val context = LocalContext.current
    val artworkModel = rememberSongArtwork(context, currentSong)

    currentSong?.let { song ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (artworkModel != null) {
                    AsyncImage(
                        model = artworkModel,
                        contentDescription = "Carátula",
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(0.9f)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                }else {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        modifier = Modifier.size(250.dp),
                        tint = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                ScrollingText(
                    text = song.title,
                    modifier = Modifier.fillMaxWidth(),
                    fontSize = 20.sp
                )
                Text(
                    text = song.artist,
                    color = Color.Gray,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(16.dp))

                Column {
                    val minutesCurrent = (currentPosition / 1000) / 60
                    val secondsCurrent = (currentPosition / 1000) % 60
                    val currentFormatted = String.format("%d:%02d", minutesCurrent, secondsCurrent)

                    val minutesTotal = (durationMs / 1000) / 60
                    val secondsTotal = (durationMs / 1000) % 60
                    val durationFormatted = String.format("%d:%02d", minutesTotal, secondsTotal)

                    Slider(
                        value = currentPosition.toFloat(),
                        onValueChange = { viewModel.seekTo(it.toInt()) },
                        valueRange = 0f..durationMs.toFloat(),
                        colors = SliderDefaults.colors(
                            thumbColor = Color.White,
                            activeTrackColor = Color(0xFF58A6FF),
                            inactiveTrackColor = Color.Gray
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(currentFormatted, color = Color.White, fontSize = 12.sp)
                        Text(durationFormatted, color = Color.White, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = when (repeatMode) {
                            1 -> Icons.Filled.Repeat
                            2 -> Icons.Filled.RepeatOne
                            else -> Icons.Outlined.Repeat
                        },
                        contentDescription = "Repetir",
                        tint = if (repeatMode == 0) Color.White else Color(0xFF58A6FF),
                        modifier = Modifier
                            .size(28.dp)
                            .clickable { viewModel.toggleRepeatMode() }
                    )
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Anterior",
                        tint = Color.White,
                        modifier = Modifier
                            .size(40.dp)
                            .clickable { viewModel.playPrevious() }
                    )
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = Color.White,
                        modifier = Modifier
                            .size(56.dp)
                            .clickable { viewModel.togglePlayPause() }
                    )
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Siguiente",
                        tint = Color.White,
                        modifier = Modifier
                            .size(40.dp)
                            .clickable { viewModel.playNext() }
                    )
                    Icon(
                        imageVector = Icons.Filled.Shuffle,
                        contentDescription = "Aleatorio",
                        tint = if (shuffleMode) Color(0xFF58A6FF) else Color.White,
                        modifier = Modifier
                            .size(28.dp)
                            .clickable { viewModel.toggleShuffleMode() }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

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
                        mainViewModel = viewModel,
                        hasNowPlayingBarAppeared = viewModel.hasNowPlayingBarAppeared.value
                    )

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                        contentDescription = "Modo de reproducción",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                        .clickable {
                            navController.navigate("playback_queue")
                        }
                    )
                }
            }
        }
    }
}
