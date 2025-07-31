package com.example.hardemusic.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

class HistoryStorage(private val context: Context) {
    private val gson = Gson()
    private val fileName = "song_history.json"

    fun saveHistory(songs: List<Song>) {
        val serializableSongs = songs.map { SerializableSong.from(it) }
        val json = gson.toJson(serializableSongs)
        File(context.filesDir, fileName).writeText(json)
    }

    fun loadHistory(): List<Song> {
        val file = File(context.filesDir, fileName)
        if (!file.exists()) return emptyList()

        val json = file.readText()
        val type = object : TypeToken<List<SerializableSong>>() {}.type
        val serializableSongs: List<SerializableSong> = gson.fromJson(json, type)
        return serializableSongs.map { it.toSong() }
    }
}