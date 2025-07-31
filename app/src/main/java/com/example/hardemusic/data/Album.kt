package com.example.hardemusic.data

import android.net.Uri

data class Album(
    val id: Long,
    val name: String,
    val artist: String,
    val albumArtUri: Uri
)