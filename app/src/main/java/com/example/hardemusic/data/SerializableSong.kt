package com.example.hardemusic.data

import android.net.Uri
import java.time.LocalDateTime

data class SerializableSong(
    val title: String,
    val artist: String,
    val uriString: String,
    val timestampString: String,
    val albumArtUriString: String? = null
) {
    fun toSong(): Song = Song(
        title = title,
        artist = artist,
        uri = Uri.parse(uriString),
        timestamp = LocalDateTime.parse(timestampString),
        albumArtUri = albumArtUriString?.let { Uri.parse(it) }
    )

    companion object {
        fun from(song: Song): SerializableSong = SerializableSong(
            title = song.title,
            artist = song.artist,
            uriString = song.uri.toString(),
            timestampString = song.timestamp.toString(),
            albumArtUriString = song.albumArtUri?.toString()
        )
    }
}