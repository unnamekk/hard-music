package com.example.hardemusic.gui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.hardemusic.data.Album
import com.example.hardemusic.data.AppText
import com.example.hardemusic.data.Artist
import com.example.hardemusic.data.Song
import com.example.hardemusic.viewmodel.MainViewModel
import com.example.hardemusic.viewmodel.SearchFilterType
import com.example.hardemusic.viewmodel.SearchViewModel


@Composable
fun SearchScreen(
    navController: NavController,
    mainViewModel: MainViewModel,
    searchViewModel: SearchViewModel
) {
    val query by searchViewModel.query.collectAsState()
    val filterType by searchViewModel.filterType.collectAsState()

    val filteredSongs by searchViewModel.filteredSongs.collectAsState()
    val filteredArtists by searchViewModel.filteredArtists.collectAsState()
    val filteredAlbums by searchViewModel.filteredAlbums.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = { searchViewModel.updateQuery(it) },
            label = { Text(AppText.searchPlaceholder) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { searchViewModel.clearQuery() }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Borrar búsqueda"
                        )
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            FilterIconButton(
                icon = Icons.Default.GridView,
                contentDescription = "Todo",
                selected = filterType == SearchFilterType.ALL,
                onClick = { searchViewModel.updateFilterType(SearchFilterType.ALL) }
            )
            Spacer(modifier = Modifier.width(8.dp))
            FilterIconButton(
                icon = Icons.Default.MusicNote,
                contentDescription = "Canciones",
                selected = filterType == SearchFilterType.SONGS,
                onClick = { searchViewModel.updateFilterType(SearchFilterType.SONGS) }
            )
            Spacer(modifier = Modifier.width(8.dp))
            FilterIconButton(
                icon = Icons.Default.Album,
                contentDescription = "Álbumes",
                selected = filterType == SearchFilterType.ALBUMS,
                onClick = { searchViewModel.updateFilterType(SearchFilterType.ALBUMS) }
            )
            Spacer(modifier = Modifier.width(8.dp))
            FilterIconButton(
                icon = Icons.Default.Person,
                contentDescription = "Artistas",
                selected = filterType == SearchFilterType.ARTISTS,
                onClick = { searchViewModel.updateFilterType(SearchFilterType.ARTISTS) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            if (filterType == SearchFilterType.ALL || filterType == SearchFilterType.SONGS) {
                if (filteredSongs.isNotEmpty()) {
                    item { Text(AppText.songsTitle, fontWeight = FontWeight.Bold, color = Color(0xFF00BFFF)) }
                    items(filteredSongs) { song ->
                        SongItem(song = song, onClick = {
                            mainViewModel.playFrom(song)
                        })
                    }
                }
            }

            if (filterType == SearchFilterType.ALL || filterType == SearchFilterType.ARTISTS) {
                if (filteredArtists.isNotEmpty()) {
                    item { Text(AppText.artistsTitle, fontWeight = FontWeight.Bold, color = Color(0xFF00BFFF)) }
                    items(filteredArtists) { artist ->
                        ArtistItem(artist = artist, onClick = {
                            navController.navigate("artist_detail/${artist.name}")
                        })
                    }
                }
            }

            if (filterType == SearchFilterType.ALL || filterType == SearchFilterType.ALBUMS) {
                if (filteredAlbums.isNotEmpty()) {
                    item { Text(AppText.albumsTitle, fontWeight = FontWeight.Bold, color = Color(0xFF00BFFF)) }
                    items(filteredAlbums) { album ->
                        AlbumItem(album = album, onClick = {
                            navController.navigate("album_detail/${album.id}")
                        })
                    }
                }
            }
        }
    }
}

@Composable
fun SongItem(song: Song, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        song.albumArtUri?.let {
            Image(
                painter = rememberAsyncImagePainter(it),
                contentDescription = null,
                modifier = Modifier.size(48.dp)
            )
        } ?: Box(
            modifier = Modifier.size(48.dp).background(Color.Gray)
        )
        Spacer(Modifier.width(12.dp))
        Column {
            Text(song.title, color = Color.White)
            Text(song.artist, color = Color.Gray)
        }
    }
}

@Composable
fun ArtistItem(artist: Artist, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        artist.albumArtUri?.let {
            Image(
                painter = rememberAsyncImagePainter(it),
                contentDescription = null,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
            )
        } ?: Box(
            Modifier.size(48.dp).background(Color.Gray, CircleShape)
        )
        Spacer(Modifier.width(12.dp))
        Text(artist.name, color = Color.White, fontSize = 16.sp)
    }
}

@Composable
fun AlbumItem(album: Album, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = rememberAsyncImagePainter(album.albumArtUri),
            contentDescription = null,
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(6.dp))
        )
        Spacer(Modifier.width(12.dp))
        Column {
            Text(album.name, color = Color.White, fontSize = 16.sp)
            Text(album.artist, color = Color.Gray, fontSize = 14.sp)
        }
    }
}

@Composable
fun FilterIconButton(
    icon: ImageVector,
    contentDescription: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (selected) Color(0xFF00BFFF) else Color.LightGray
    val iconColor = if (selected) Color.White else Color.Black

    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(20.dp),
        contentPadding = PaddingValues(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = iconColor,
            modifier = Modifier.size(20.dp)
        )
    }
}
