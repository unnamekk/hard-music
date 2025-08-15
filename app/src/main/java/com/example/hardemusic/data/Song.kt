package com.example.hardemusic.data

import android.net.Uri
import java.time.LocalDateTime

data class Song(
    val title: String,
    val artist: String,
    val uri: Uri,
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val albumArtUri: Uri? = null,
    val trackNumber: Int? = null,
    val albumId: Long? = null,
    val albumArtist: String? = null,
    val year: Int? = null,
    val albumName: String? = null,
    val path: String? = null
)