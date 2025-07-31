package com.example.hardemusic.viewmodel

import android.app.Application
import android.content.ContentUris
import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.hardemusic.data.Album
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AlbumsViewModel(application: Application) : AndroidViewModel(application) {

    private val _albums = MutableStateFlow<List<Album>>(emptyList())
    val albums: StateFlow<List<Album>> = _albums


    init {
        loadAlbums()
    }

    private fun loadAlbums() {
        viewModelScope.launch {
            val resolver = getApplication<Application>().contentResolver
            val uri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI
            val projection = arrayOf(
                MediaStore.Audio.Albums._ID,
                MediaStore.Audio.Albums.ALBUM,
                MediaStore.Audio.Albums.ARTIST,
                MediaStore.Audio.Albums.ALBUM_ART
            )

            val cursor = resolver.query(uri, projection, null, null, null)
            val albumsList = mutableListOf<Album>()

            cursor?.use {
                val idColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Albums._ID)
                val nameColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM)
                val artistColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Albums.ARTIST)
                val artColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM_ART)

                while (it.moveToNext()) {
                    val id = it.getLong(idColumn)
                    val name = it.getString(nameColumn) ?: "Álbum desconocido"
                    val artist = it.getString(artistColumn) ?: "Artista desconocido"
                    val artPath = it.getString(artColumn)
                    val artUri = if (artPath != null) Uri.parse(artPath) else ContentUris.withAppendedId(
                        Uri.parse("content://media/external/audio/albumart"), id
                    )

                    albumsList.add(Album(id, name, artist, artUri))

                }
            }

            _albums.value = albumsList.sortedBy { it.name.lowercase() }
        }
    }
}

