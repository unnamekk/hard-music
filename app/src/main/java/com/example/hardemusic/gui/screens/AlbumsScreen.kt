package com.example.hardemusic.gui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.hardemusic.data.Album
import com.example.hardemusic.gui.AlphabetScrollbar
import com.example.hardemusic.viewmodel.AlbumsViewModel

@Composable
fun AlbumsScreen(viewModel: AlbumsViewModel = viewModel(),onAlbumClick: (Album) -> Unit) {
    val albums by viewModel.albums.collectAsState()
    val sortedAlbums = albums.sortedBy { it.name }

    val grouped = remember(sortedAlbums) {
        sortedAlbums.groupBy {
            val firstChar = it.name.firstOrNull()?.uppercaseChar()
            if (firstChar != null && firstChar.isLetter()) firstChar else '#'
        }.toSortedMap()
    }

    val listState = rememberLazyListState()

    val indexMap = remember(grouped) {
        var index = 0
        buildMap<Char, Int> {
            grouped.forEach { (letter, albumsGroup) ->
                put(letter, index)
                index++
                index += (albumsGroup.size + 1) / 2
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(bottom = 72.dp)
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(end = 32.dp)
        ) {
            grouped.forEach { (letter, albumsGroup) ->
                item {
                    Text(
                        text = letter.toString(),
                        color = Color.Cyan,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .padding(start = 16.dp, top = 20.dp, bottom = 8.dp)
                    )
                }

                items((albumsGroup.chunked(2))) { rowAlbums ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        rowAlbums.forEach { album ->
                            AlbumItem(
                                album = album,
                                modifier = Modifier
                                    .weight(1f),
                                onClick = { onAlbumClick(album) }
                            )
                        }
                        if (rowAlbums.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }

        // Barra lateral
        AlphabetScrollbar(
            groupedItems = grouped,
            listState = listState,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(vertical = 16.dp),
            onScrollToIndex = { letter -> indexMap[letter] ?: 0 }
        )
    }
}

@Composable
fun AlbumItem(album: Album, modifier: Modifier = Modifier,onClick: () -> Unit) {
    Column(
        modifier = modifier
            .clickable { onClick() }
    ) {
        Image(
            painter = rememberAsyncImagePainter(album.albumArtUri),
            contentDescription = "Carátula del álbum",
            modifier = Modifier
                .aspectRatio(1f)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = album.name,
            color = Color.White,
            fontSize = 14.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = album.artist,
            color = Color.Gray,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}