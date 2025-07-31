package com.example.hardemusic.gui.screens

import android.net.Uri
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.hardemusic.gui.AlphabetScrollbar
import com.example.hardemusic.viewmodel.ArtistsViewModel

@Composable
fun ArtistsScreen(
    artistsViewModel: ArtistsViewModel,
    navController: NavHostController
) {
    val artists by artistsViewModel.artists.collectAsState()
    val listState = rememberLazyListState()

    val grouped = artists
        .sortedBy { it.name.lowercase() }
        .groupBy { it.name.firstOrNull()?.uppercaseChar() ?: '#' }

    val indexMap = mutableMapOf<Char, Int>()
    var indexCounter = 0
    grouped.forEach { (char, artistsInGroup) ->
        indexMap[char] = indexCounter
        indexCounter += 1 + (artistsInGroup.size + 2) / 3
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(end = 24.dp)
        ) {
            grouped.forEach { (char, artistsInGroup) ->
                item {
                    Text(
                        text = char.toString(),
                        color = Color.Cyan,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 8.dp)
                    )
                }

                items(artistsInGroup.chunked(3)) { rowGroup ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        rowGroup.forEach { artist ->
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        navController.navigate("artist_detail/${Uri.encode(artist.name)}")
                                    },
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Image(
                                    painter = rememberAsyncImagePainter(artist.albumArtUri),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(72.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = artist.name,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                )
                            }
                        }

                        repeat(3 - rowGroup.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        AlphabetScrollbar(
            groupedItems = grouped,
            listState = listState,
            modifier = Modifier
                .align(Alignment.CenterEnd),
            onScrollToIndex = { letter -> indexMap[letter] ?: 0 }
        )
    }
}