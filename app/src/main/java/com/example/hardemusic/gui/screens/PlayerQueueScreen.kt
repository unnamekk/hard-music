package com.example.hardemusic.gui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.hardemusic.data.AppText
import com.example.hardemusic.data.Song
import com.example.hardemusic.viewmodel.MainViewModel

@Composable
fun PlayerQueueScreen(
    mainViewModel: MainViewModel,
    onBack: () -> Unit
) {
    val queue by mainViewModel.playbackQueue.collectAsState()
    val currentSong by mainViewModel.currentSong.collectAsState()
    val currentIndex by mainViewModel.currentIndexFlow.collectAsState()

    val pageSize = 20
    val currentPage = currentIndex / pageSize
    val start = currentPage * pageSize
    val end = (start + pageSize).coerceAtMost(queue.size)
    val visibleQueue = queue.subList(start, end)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = Color.White
                )
            }
            Text(
                text = AppText.queueTitle,
                color = Color.White,
                fontSize = 20.sp,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))


        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            itemsIndexed(visibleQueue) { index, song ->
                PlaybackQueueItem(
                    song = song,
                    isCurrent = song.title == currentSong?.title && song.artist == currentSong?.artist,
                    onClick = { mainViewModel.playFromQueue(song) }
                )
            }
        }
    }
}

@Composable
fun PlaybackQueueItem(
    song: Song,
    isCurrent: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isCurrent) Color(0xFF1A1A1A) else Color.Transparent

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .clickable { onClick() }
            .padding(vertical = 8.dp, horizontal = 12.dp)
    ) {

        song.albumArtUri?.let { uri ->
            Image(
                painter = rememberAsyncImagePainter(uri),
                contentDescription = null,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(6.dp)),
                contentScale = ContentScale.Crop
            )
        } ?: Box(
            modifier = Modifier
                .size(48.dp)
                .background(Color.DarkGray, RoundedCornerShape(6.dp))
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(text = song.title, color = Color.White, fontSize = 16.sp)
            Text(text = song.artist, color = Color.Gray, fontSize = 14.sp)
        }

        if (isCurrent) {
            Icon(
                imageVector = Icons.Default.Equalizer,
                contentDescription = "Reproduciendo",
                tint = Color(0xFF9F6D5C),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}