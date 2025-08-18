package com.example.hardemusic.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.navigation.NavController
import com.example.hardemusic.data.Playlist
import com.example.hardemusic.data.Song
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.lang.reflect.Type
import java.time.LocalDateTime

class PlaylistViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("playlists_prefs", Context.MODE_PRIVATE)

    private val gson = GsonBuilder()
        .registerTypeAdapter(Uri::class.java, UriTypeAdapter())
        .registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeTypeAdapter())
        .create()

    private val _playlists = MutableStateFlow<List<Playlist>>(emptyList())
    val playlists: StateFlow<List<Playlist>> = _playlists.asStateFlow()

    init {
        loadPlaylists()
    }

    fun createPlaylist(name: String, imageUri: String?) {
        val newPlaylist = Playlist(name = name, songs = emptyList(), imageUri = imageUri)
        _playlists.value = _playlists.value + newPlaylist
        savePlaylists()
    }

    fun addSongToPlaylist(
        playlistName: String,
        song: Song,
        onResult: (message: String, isSuccess: Boolean) -> Unit
    ) {
        val updatedPlaylists = _playlists.value.map { playlist ->
            if (playlist.name == playlistName) {
                if (playlist.songs.contains(song)) {
                    onResult("La canción ya está en la playlist", false)
                    playlist
                } else {
                    onResult("Canción agregada a ${playlist.name}", true)
                    playlist.copy(songs = playlist.songs + song)
                }
            } else playlist
        }

        _playlists.value = updatedPlaylists
        savePlaylists()
    }


    fun editPlaylist(originalName: String, newName: String, newImageUri: String?) {
        val updatedPlaylists = _playlists.value.map { playlist ->
            if (playlist.name == originalName) {
                playlist.copy(name = newName, imageUri = newImageUri)
            } else playlist
        }
        _playlists.value = updatedPlaylists
        savePlaylists()
    }

    fun removeSongFromPlaylist(playlistName: String, song: Song) {
        val updatedPlaylists = _playlists.value.map { playlist ->
            if (playlist.name == playlistName) {
                playlist.copy(songs = playlist.songs.filterNot { it == song })
            } else playlist
        }
        _playlists.value = updatedPlaylists
        savePlaylists()
    }

    fun reloadPlaylistDetails(navController: NavController, playlistName: String) {
        navController.popBackStack()
        navController.navigate("playlist_detail/${Uri.encode(playlistName)}") {
            launchSingleTop = true
        }
    }

    fun deletePlaylist(playlistName: String) {
        _playlists.value = _playlists.value.filter { it.name != playlistName }
        savePlaylists()
    }

    private fun loadPlaylists() {
        val json = prefs.getString("playlists_data", null)
        if (json != null) {
            val type = object : TypeToken<List<Playlist>>() {}.type
            _playlists.value = gson.fromJson(json, type)
        }
    }

    fun forceReload() {
        loadPlaylists()
    }

    private fun savePlaylists() {
        val json = gson.toJson(_playlists.value)
        prefs.edit().putString("playlists_data", json).apply()
    }

    private class UriTypeAdapter : JsonSerializer<Uri>, JsonDeserializer<Uri> {
        override fun serialize(
            src: Uri?,
            typeOfSrc: Type?,
            context: JsonSerializationContext?
        ): JsonElement {
            return JsonPrimitive(src?.toString())
        }

        override fun deserialize(
            json: JsonElement?,
            typeOfT: Type?,
            context: JsonDeserializationContext?
        ): Uri {
            return Uri.parse(json?.asString)
        }
    }

    private class LocalDateTimeTypeAdapter : JsonSerializer<LocalDateTime>,
        JsonDeserializer<LocalDateTime> {
        override fun serialize(
            src: LocalDateTime?,
            typeOfSrc: Type?,
            context: JsonSerializationContext?
        ): JsonElement {
            return JsonPrimitive(src?.toString())
        }

        override fun deserialize(
            json: JsonElement?,
            typeOfT: Type?,
            context: JsonDeserializationContext?
        ): LocalDateTime {
            return LocalDateTime.parse(json?.asString)
        }
    }

}