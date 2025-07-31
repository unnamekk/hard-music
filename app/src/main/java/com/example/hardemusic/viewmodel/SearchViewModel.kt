package com.example.hardemusic.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hardemusic.data.Album
import com.example.hardemusic.data.Artist
import com.example.hardemusic.data.Song
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

enum class SearchFilterType {
    SONGS, ARTISTS, ALBUMS, ALL
}

class SearchViewModel(
    songsFlow: StateFlow<List<Song>>,
    artistsFlow: StateFlow<List<Artist>>,
    albumsFlow: StateFlow<List<Album>>
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query

    private val _filterType = MutableStateFlow(SearchFilterType.ALL)
    val filterType: StateFlow<SearchFilterType> = _filterType

    fun updateQuery(newValue: String) {
        _query.value = newValue
    }

    fun clearQuery() {
        _query.value = ""
    }

    fun updateFilterType(type: SearchFilterType) {
        _filterType.value = type
    }

    val filteredSongs: StateFlow<List<Song>> = _query
        .combine(songsFlow) { q, songs ->
            val term = q.trim().lowercase()
            if (term.isBlank()) emptyList()
            else songs.filter {
                it.title.lowercase().contains(term) || it.artist.lowercase().contains(term)
            }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val filteredArtists: StateFlow<List<Artist>> = _query
        .combine(artistsFlow) { q, artists ->
            val term = q.trim().lowercase()
            if (term.isBlank()) emptyList()
            else artists.filter { it.name.lowercase().contains(term) }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val filteredAlbums: StateFlow<List<Album>> = _query
        .combine(albumsFlow) { q, albums ->
            val term = q.trim().lowercase()
            if (term.isBlank()) emptyList()
            else albums.filter {
                it.name.lowercase().contains(term) || it.artist.lowercase().contains(term)
            }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
}