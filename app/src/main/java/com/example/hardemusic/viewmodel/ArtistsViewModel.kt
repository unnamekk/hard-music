package com.example.hardemusic.viewmodel

import androidx.lifecycle.ViewModel
import com.example.hardemusic.data.Artist
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


class ArtistsViewModel(mainViewModel: MainViewModel) : ViewModel() {

    private val _artists = MutableStateFlow<List<Artist>>(emptyList())
    val artists: StateFlow<List<Artist>> = _artists

    init {
        val songs = mainViewModel.songsList.value
        val uniqueArtists = mutableMapOf<String, String?>()

        for (song in songs) {
            val rawName = song.artist

            val names = splitArtistNames(rawName)

            for (name in names) {
                if (name !in uniqueArtists) {
                    uniqueArtists[name] = song.albumArtUri.toString()
                }
            }
        }

        val artistList = uniqueArtists.map { (name, uri) ->
            Artist(name = name, albumArtUri = uri)
        }.sortedBy { it.name.lowercase() }

        _artists.value = artistList
    }

    private fun splitArtistNames(raw: String): List<String> {
        return if (raw.trim() == "Tyler, The Creator") {
            listOf("Tyler, The Creator")
        } else {
            raw.split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
        }
    }
}