package com.example.hardemusic.gui.screens

import android.annotation.SuppressLint
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.hardemusic.gui.AlphabetScrollbar
import com.example.hardemusic.gui.DetailTopBar
import com.example.hardemusic.gui.ScrollingText
import com.example.hardemusic.gui.SongRow
import com.example.hardemusic.viewmodel.AlbumsViewModel
import com.example.hardemusic.viewmodel.MainViewModel

fun groupSongsByTitleFirstLetter(songs: List<Song>): Map<Char, List<Song>> {
    return songs.groupBy { song ->
        song.title.firstOrNull()?.uppercaseChar() ?: '#'
    }.toSortedMap()
}

@SuppressLint("DefaultLocale")
@Composable
fun ArtistDetailScreen(
    artistName: String,
    mainViewModel: MainViewModel,
    albumsViewModel: AlbumsViewModel,
    navController: NavHostController,
    onBack: () -> Unit
) {

    DisposableEffect(Unit) {
        onDispose {
            mainViewModel.clearSelection()
        }
    }

    val allSongs by mainViewModel.songsList.collectAsState()
    val allAlbums by albumsViewModel.albums.collectAsState()

    val songs by remember(allSongs, artistName) {
        mutableStateOf(
            allSongs.filter { song ->
                val normalizedArtist = song.artist?.trim()?.lowercase()
                val target = artistName.trim().lowercase()

                if (song.artist.equals("Tyler, The Creator", ignoreCase = true)) {
                    normalizedArtist == target
                } else {
                    normalizedArtist
                        ?.split(",")
                        ?.map { it.trim() }
                        ?.any { it.equals(target, ignoreCase = true) } ?: false
                }
            }
        )
    }

    val albumArtUriSet = songs.map { it.albumArtUri }.toSet()

    val artistAlbums = allAlbums
        .filter { it.albumArtUri in albumArtUriSet }
        .distinctBy { it.name.lowercase() to it.artist.lowercase() }

    var totalDuration by remember { mutableStateOf(0) }

    LaunchedEffect(songs) {
            totalDuration = mainViewModel.getTotalDuration(songs)
    }

    val formattedDuration = String.format(
        "%02d:%02d:%02d",
        totalDuration / 3600000,
        (totalDuration % 3600000) / 60000,
        (totalDuration % 60000) / 1000
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .padding(16.dp)
    ) {
        DetailTopBar(
            title = artistName,
            navController = navController,
            mainViewModel = mainViewModel,
            songs = songs,
            onBack = onBack
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = artistName,
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "${artistAlbums.size} "+ AppText.albumsTitle+ "•  ${songs.size} " + AppText.songsTitle +"• $formattedDuration",
            color = Color.Gray,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = {
                    val current = mainViewModel.currentSong.value
                    val alternatives = songs.filterNot {
                        current != null && it.title == current.title && it.artist == current.artist
                    }
                    if (alternatives.isNotEmpty()) {
                        mainViewModel.playFrom(alternatives.random(), songs, artistName = artistName,contextType = "artist")
                    } else if (songs.isNotEmpty()) {
                        mainViewModel.playFrom(songs.first(), songs,contextType = "artist")
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF58A6FF),
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
            ) {
                Icon(Icons.Default.Shuffle, contentDescription = null, tint = Color.White,  modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(AppText.shuffelAlbumButton, color = Color.White,fontSize = 14.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = AppText.albumsTitle,
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(6.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(artistAlbums) { album ->
                Column(
                    modifier = Modifier
                        .width(70.dp)
                        .clickable {
                            navController.navigate("album_group_detail/${Uri.encode(album.name)}/${album.artist}")
                        }
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(album.albumArtUri),
                        contentDescription = null,
                        modifier = Modifier
                            .size(70.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    ScrollingText(
                        text = album.name,
                        fontSize = 14.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = AppText.songsTitle,
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        val groupedSongs = groupSongsByTitleFirstLetter(songs)
        val listState = rememberLazyListState()
        val indexedList = remember(groupedSongs) {
            groupedSongs.flatMap { (letter, songsForLetter) ->
                listOf(SongListItem.Header(letter)) +
                        songsForLetter.map { SongListItem.Entry(it) }
            }
        }


        Row(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                state = listState,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 4.dp)
            ) {
                itemsIndexed(indexedList) { index, item ->
                    when (item) {
                        is SongListItem.Header -> {
                            Text(
                                text = item.letter.toString(),
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .padding(vertical = 4.dp, horizontal = 8.dp)
                            )
                        }

                        is SongListItem.Entry -> {
                            SongRow(
                                song = item.song,
                                onSongClick = { clicked ->
                                    mainViewModel.playFrom(clicked, songs, artistName = artistName, contextType = "artist")
                                },
                                navController = navController,
                                mainViewModel = mainViewModel
                            )
                        }
                    }
                }
            }

            AlphabetScrollbar(
                groupedItems = groupedSongs,
                listState = listState,
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .width(20.dp),
                onScrollToIndex = { letter ->
                    indexedList.indexOfFirst { item ->
                        item is SongListItem.Header && item.letter.equals(letter, ignoreCase = true)
                    }.takeIf { it != -1 } ?: 0
                }
            )
        }

        Spacer(modifier = Modifier.height(72.dp))
    }
}

@Composable
fun AlbumGroupDetailScreen(
    albumName: String,
    artistName: String,
    mainViewModel: MainViewModel,
    albumsViewModel: AlbumsViewModel,
    navController: NavHostController,
    onBack: () -> Unit,
    hasNowPlayingBarAppeared: Boolean
) {
    val allSongs by mainViewModel.songsList.collectAsState()
    val allAlbums by albumsViewModel.albums.collectAsState()

    val matchingAlbumIds = allAlbums
        .filter {
            it.name.equals(albumName, ignoreCase = true) &&
                    it.artist.equals(artistName, ignoreCase = true)
        }
        .map { it.id }

    val songs = remember(allSongs, matchingAlbumIds) {
        allSongs.filter { it.albumId in matchingAlbumIds }
    }

    if (songs.isEmpty()) {
        Text("No se encontraron canciones", color = Color.White)
        return
    }

    val representativeAlbumArt = songs.first().albumArtUri ?: Uri.EMPTY
    val album = Album(
        id = -1,
        name = albumName,
        artist = artistName,
        albumArtUri = representativeAlbumArt
    )

    AlbumDetailScreen(
        album = album,
        songs = songs,
        onBack = onBack,
        mainViewModel = mainViewModel,
        navController = navController,
        hasNowPlayingBarAppeared = hasNowPlayingBarAppeared
    )
}

sealed class SongListItem {
    data class Header(val letter: Char) : SongListItem()
    data class Entry(val song: Song) : SongListItem()
}
