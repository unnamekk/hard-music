package com.example.hardemusic.data

data class Playlist(
    val name: String,
    val songs: List<Song>,
    val imageUri: String? = null
)
