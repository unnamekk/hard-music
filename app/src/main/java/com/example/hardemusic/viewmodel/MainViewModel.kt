package com.example.hardemusic.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.app.RecoverableSecurityException
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.activity.result.IntentSenderRequest
import androidx.annotation.RequiresApi
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.hardemusic.MainActivity
import com.example.hardemusic.NotificationHelper
import com.example.hardemusic.data.AppText
import com.example.hardemusic.data.DayEntry
import com.example.hardemusic.data.HistoryStorage
import com.example.hardemusic.data.MonthEntry
import com.example.hardemusic.data.Song
import com.mpatric.mp3agic.ID3v24Tag
import com.mpatric.mp3agic.Mp3File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Month
import java.time.Year
import java.time.ZoneId


class MainViewModel(application: Application) : AndroidViewModel(application) {

    object ViewModelBridge {
        var mainViewModel: MainViewModel? = null

        @SuppressLint("StaticFieldLeak")
        var notificationHelper: NotificationHelper? = null
        var mediaSession: MediaSessionCompat? = null
    }

    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong

    private var mediaPlayer: MediaPlayer? = null
    private var currentIndex = -1

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    private val _currentDuration = MutableStateFlow(0)
    val currentDuration: StateFlow<Int> = _currentDuration

    private val _currentPosition = MutableStateFlow(0)
    val currentPosition: StateFlow<Int> = _currentPosition

    private val _allHistory = MutableStateFlow<List<Song>>(emptyList())
    val historyToday: StateFlow<List<Song>> = _allHistory
        .map { list ->
            val today = LocalDate.now()
            list
                .filter { it.timestamp.toLocalDate() == today }
                .sortedByDescending { it.timestamp }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.Companion.Eagerly,
            emptyList()
        )

    private val historyStorage = HistoryStorage(application)

    private val _downloadedSongsByMonth = MutableStateFlow<List<MonthEntry>>(emptyList())
    val downloadedSongsByMonth = _downloadedSongsByMonth.asStateFlow()

    private val _selectedSongsForDay = MutableStateFlow<List<Song>>(emptyList())
    val selectedSongsForDay = _selectedSongsForDay.asStateFlow()

    private val _selectedSongs = MutableStateFlow<List<Song>>(emptyList())
    val selectedSongs = _selectedSongs.asStateFlow()

    private val _isSelectionMode = MutableStateFlow(false)
    val isSelectionMode = _isSelectionMode.asStateFlow()

    private val _recentSongs = MutableStateFlow<List<Song>>(emptyList())
    val recentSongs: StateFlow<List<Song>> = _recentSongs

    val _songs = MutableStateFlow<List<Song>>(emptyList())

    private var currentAlbumSongs: List<Song> = emptyList()

    private val _shuffleMode = MutableStateFlow(false)
    val shuffleMode: StateFlow<Boolean> = _shuffleMode

    private var lastShuffleState = shuffleMode.value

    private val _repeatMode = MutableStateFlow(0)
    val repeatMode: StateFlow<Int> = _repeatMode

    private val preferences =
        getApplication<Application>().getSharedPreferences("player_prefs", Context.MODE_PRIVATE)

    private val _hasNowPlayingBarAppeared = MutableStateFlow(false)
    val hasNowPlayingBarAppeared: StateFlow<Boolean> = _hasNowPlayingBarAppeared

    private val _playbackQueue = MutableStateFlow<List<Song>>(emptyList())
    val playbackQueue: StateFlow<List<Song>> = _playbackQueue

    private val currentPlaybackQueue: List<Song>
        get() = _playbackQueue.value

    private val playbackHistory = mutableListOf<Int>()

    private val _currentIndex = MutableStateFlow(0)
    val currentIndexFlow: StateFlow<Int> = _currentIndex

    private var currentContextSongs: List<Song>? = null
    private var currentArtistName: String? = null

    private val _editingSongs = MutableStateFlow<List<Song>>(emptyList())
    val editingSongs: StateFlow<List<Song>> = _editingSongs

    private var shouldPlayNext = true

    private val _editingSong = MutableStateFlow<Song?>(null)
    val editingSong: StateFlow<Song?> = _editingSong

    private var currentPlaylistName: String? = null

    private val _excludeWhatsApp = MutableStateFlow(
        preferences.getBoolean("exclude_whatsapp", false)
    )

    val excludeWhatsApp: StateFlow<Boolean> = _excludeWhatsApp

    private val _language = MutableStateFlow("Es")
    val language: StateFlow<String> = _language

    val songsList: StateFlow<List<Song>> = _songs
        .map { list -> list.sortedBy { it.title.lowercase() } }
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            emptyList()
        )

    init {
        loadSongs()
        loadHistoryFromStorage()
        ViewModelBridge.mainViewModel = this
        _shuffleMode.value = preferences.getBoolean("shuffle_mode", false)
        _repeatMode.value = preferences.getInt("repeat_mode", 0)

        val savedLang = preferences.getString("language", "Es") ?: "Es"
        _language.value = savedLang
        AppText.language = savedLang

        viewModelScope.launch {
            delay(100)
            restoreLastPlaybackState()
        }

        viewModelScope.launch {
            shuffleMode.collect { current ->
                if (current != lastShuffleState) {
                    lastShuffleState = current
                    reorderPlaybackQueueAfterShuffleToggle()
                }
            }
        }
    }

    fun markNowPlayingBarAsAppeared() {
        _hasNowPlayingBarAppeared.value = true
    }

    fun markNowPlayingBarAsNotAppeared() {
        _hasNowPlayingBarAppeared.value = false
    }

    fun setExcludeWhatsApp(exclude: Boolean) {
        _excludeWhatsApp.value = exclude
        preferences.edit().putBoolean("exclude_whatsapp", exclude).apply()
        loadSongs()
    }

    fun setLanguage(langCode: String) {
        _language.value = langCode
        AppText.language = langCode
        preferences.edit().putString("language", langCode).apply()
    }

    fun toggleSelection(song: Song) {
        val current = _selectedSongs.value.toMutableList()
        if (current.contains(song)) {
            current.remove(song)
        } else {
            current.add(song)
        }
        _selectedSongs.value = current
        _isSelectionMode.value = current.isNotEmpty()
    }

    fun startSelection(song: Song) {
        if (!_isSelectionMode.value) {
            _isSelectionMode.value = true
            _selectedSongs.value = listOf(song)
        }
    }

    fun clearSelection() {
        _selectedSongs.value = emptyList()
        _isSelectionMode.value = false
    }

    fun selectAll(songs: List<Song>) {
        _selectedSongs.value = songs
        _isSelectionMode.value = songs.isNotEmpty()
    }

    fun loadSongs() {
        val resolver = getApplication<Application>().contentResolver
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val cursor = resolver.query(uri, null, null, null, null)

        val loadedSongs = mutableListOf<Song>()

        cursor?.use {
            val titleColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val idColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val albumIdColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val dateAddedColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
            val trackColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK)
            val albumArtistColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ARTIST)
            val yearColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR)
            val albumColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)

            while (it.moveToNext()) {
                val id = it.getLong(idColumn)
                val songUri = Uri.withAppendedPath(uri, id.toString())
                val title = it.getString(titleColumn)
                val artist = it.getString(artistColumn)
                val rawTrackNumber = it.getInt(trackColumn)
                val albumArtist = it.getString(albumArtistColumn)
                val year = it.getInt(yearColumn)
                val trackNumber = rawTrackNumber % 1000
                val albumName = it.getString(albumColumn)
                val pathColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
                val path = it.getString(pathColumn)

                val dateAddedSeconds = it.getLong(dateAddedColumn)
                val timestamp = LocalDateTime.ofInstant(
                    Instant.ofEpochSecond(dateAddedSeconds),
                    ZoneId.systemDefault()
                )

                val albumId = it.getLong(albumIdColumn)

                if (_excludeWhatsApp.value && path.contains("WhatsApp/Media/WhatsApp Audio")) {
                    continue
                }

                val albumArtUri = "content://media/external/audio/albumart/$albumId".toUri()
                loadedSongs.add(
                    Song(
                        title,
                        artist,
                        songUri,
                        timestamp,
                        albumArtUri,
                        trackNumber = trackNumber,
                        albumId = albumId,
                        albumArtist = albumArtist,
                        year = year,
                        albumName = albumName,
                        path=path
                    )
                )
            }
        }
        _songs.value = loadedSongs
    }

    fun togglePlayPause() {
        mediaPlayer?.let {
            val nowPlaying = if (it.isPlaying) {
                it.pause()
                false
            } else {
                it.start()
                true
            }

            _isPlaying.value = nowPlaying

            currentSong.value?.let { song ->
                ViewModelBridge.mediaSession?.setPlaybackState(
                    PlaybackStateCompat.Builder()
                        .setActions(
                            PlaybackStateCompat.ACTION_PLAY or
                                    PlaybackStateCompat.ACTION_PAUSE or
                                    PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                                    PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                        )
                        .setState(
                            if (nowPlaying) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED,
                            mediaPlayer?.currentPosition?.toLong() ?: 0L,
                            1.0f
                        )
                        .build()
                )

                ViewModelBridge.notificationHelper?.showNotification(song, nowPlaying)
            }
        }
    }

    fun setEditingSong(song: Song) {
        val fullSong = _songs.value.find { it.uri == song.uri } ?: song
        _editingSong.value = fullSong
    }

    private fun reorderPlaybackQueueAfterShuffleToggle() {
        val current = _currentSong.value ?: return
        val isShuffleEnabled = shuffleMode.value

        val sourceList = currentContextSongs ?: songsList.value

        val reordered = when {
            isShuffleEnabled -> sourceList.shuffled()
            currentArtistName != null -> sourceList.sortedBy { it.title.lowercase() }
            currentAlbumSongs.isNotEmpty() && sourceList.all { it.trackNumber != null } -> sourceList.sortedBy { it.trackNumber!! }
            else -> sourceList.sortedBy { it.title.lowercase() }
        }

        _playbackQueue.value = reordered.toList()

        currentIndex = reordered.indexOfFirst {
            it.title == current.title && it.artist == current.artist
        }.coerceAtLeast(0)

        _currentIndex.value = currentIndex
    }

    fun stopPlaybackCompletely() {
        shouldPlayNext = false

        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null

        _currentSong.value = null
        _isPlaying.value = false
        _currentIndex.value = -1

        _playbackQueue.value = emptyList()

        currentContextSongs = null
        currentAlbumSongs = emptyList()
        currentArtistName = null

        preferences.edit().clear().apply()
    }


    fun playRandomSong() {
        val availableSongs = songsList.value
        if (availableSongs.isNotEmpty()) {
            _playbackQueue.value = availableSongs.shuffled()

            currentIndex = 0
            _currentIndex.value = currentIndex

            val song = currentPlaybackQueue[currentIndex]

            currentAlbumSongs = emptyList()
            _currentSong.value = song

            playMedia()
        }
    }

    fun playPrevious() {
        if (currentPlaybackQueue.isEmpty()) return

        if (currentIndex > 0) {
            currentIndex--
            _currentIndex.value = currentIndex
            _currentSong.value = currentPlaybackQueue[currentIndex]
            playMedia()
        }
    }

    fun playNext() {
        if (!shouldPlayNext) return
        if (currentPlaybackQueue.isEmpty()) return

        if (repeatMode.value == 0 && currentIndex == currentPlaybackQueue.lastIndex) {
            stopPlaybackCompletely()
            return
        }

        when (repeatMode.value) {
            2 -> {
                _currentSong.value = currentPlaybackQueue[currentIndex]
                playMedia()
            }

            else -> {
                val nextIndex = currentIndex + 1
                if (nextIndex < currentPlaybackQueue.size) {
                    currentIndex = nextIndex
                    _currentIndex.value = currentIndex
                    _currentSong.value = currentPlaybackQueue[currentIndex]
                    playMedia()
                } else {
                    if (repeatMode.value == 1) {
                        currentIndex = 0
                        _currentIndex.value = 0
                        _currentSong.value = currentPlaybackQueue[0]
                        playMedia()
                    } else {
                        stopPlaybackCompletely()
                    }
                }
            }
        }
    }

    private fun playMedia() {
        shouldPlayNext = true

        mediaPlayer?.release()
        currentSong.value?.let { song ->
            mediaPlayer = MediaPlayer.create(getApplication(), song.uri)
            mediaPlayer?.start()
            ViewModelBridge.mediaSession?.apply {
                setPlaybackState(
                    PlaybackStateCompat.Builder()
                        .setActions(
                            PlaybackStateCompat.ACTION_PLAY or
                                    PlaybackStateCompat.ACTION_PAUSE or
                                    PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                                    PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                        )
                        .setState(
                            PlaybackStateCompat.STATE_PLAYING,
                            mediaPlayer?.currentPosition?.toLong() ?: 0L,
                            1.0f
                        )
                        .build()
                )

                setMetadata(
                    MediaMetadataCompat.Builder()
                        .putString(MediaMetadataCompat.METADATA_KEY_TITLE, song.title)
                        .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, song.artist)
                        .putBitmap(
                            MediaMetadataCompat.METADATA_KEY_ALBUM_ART,
                            loadAlbumArt(song.albumArtUri)
                        )
                        .build()
                )
            }


            ViewModelBridge.notificationHelper?.showNotification(song, true)


            mediaPlayer?.setOnCompletionListener {
                if (shouldPlayNext) {
                    playNext()
                }
            }

            if (_allHistory.value.none { it.title == song.title && it.artist == song.artist && it.timestamp.toLocalDate() == LocalDate.now() }) {
                val songForHistory = song.copy(
                    timestamp = LocalDateTime.now(),
                    albumArtUri = song.albumArtUri
                )

                val updatedHistory = _allHistory.value + songForHistory
                _allHistory.value = updatedHistory

                viewModelScope.launch(Dispatchers.IO) {
                    historyStorage.saveHistory(updatedHistory)
                }
            }

            _isPlaying.value = true
            _currentDuration.value = mediaPlayer?.duration ?: 0
        }

        viewModelScope.launch {
            while (mediaPlayer != null && mediaPlayer!!.isPlaying) {
                _currentPosition.value = mediaPlayer?.currentPosition ?: 0
                delay(1000)
            }
        }
        saveCurrentPlaybackState()
    }

    fun playFrom(
        song: Song,
        albumSongs: List<Song>? = null,
        artistName: String? = null,
        contextType: String? = null,
        playlistName: String? = null
    ) {
        val isShuffleEnabled = shuffleMode.value

        currentArtistName = artistName

        currentPlaylistName = when (contextType) {
            "playlist" -> playlistName
            else -> null
        }

        if (albumSongs != null) {
            currentContextSongs = albumSongs
            currentAlbumSongs = albumSongs

            val baseList = when {
                isShuffleEnabled -> albumSongs.shuffled()
                artistName != null -> albumSongs.sortedBy { it.title.lowercase() }
                albumSongs.all { it.trackNumber != null } -> albumSongs.sortedBy { it.trackNumber!! }
                else -> albumSongs.sortedBy { it.title.lowercase() }
            }

            _playbackQueue.value = baseList

            currentIndex = baseList.indexOfFirst {
                it.title == song.title && it.artist == song.artist
            }.coerceAtLeast(0)

            _currentIndex.value = currentIndex
            _currentSong.value = baseList[currentIndex]
        } else {
            currentContextSongs = null
            currentAlbumSongs = emptyList()
            currentArtistName = null

            val allSongs = songsList.value
            val baseList = if (isShuffleEnabled) allSongs.shuffled()
            else allSongs.sortedBy { it.title.lowercase() }

            _playbackQueue.value = baseList.toList()

            currentIndex = baseList.indexOfFirst {
                it.title == song.title && it.artist == song.artist
            }.coerceAtLeast(0)

            _currentIndex.value = currentIndex
            _currentSong.value = baseList[currentIndex]
        }

        preferences.edit().apply {
            putString("last_song_uri", song.uri.toString())
            putString("last_song_title", song.title)
            putString("last_song_artist", song.artist)
            putString("last_song_album_art_uri", song.albumArtUri?.toString())

            when (contextType) {
                "album" -> {
                    putString("last_context", "album")
                    putLong("last_album_id", song.albumId ?: -1L)
                }

                "artist" -> {
                    putString("last_context", "artist")
                    putString("last_artist_name", artistName)
                }

                "playlist" -> {
                    putString("last_context", "playlist")
                    putString("last_playlist_name", playlistName)
                }

                else -> {
                    putString("last_context", "none")
                }
            }
        }.apply()

        playbackHistory.clear()
        playMedia()
    }

    fun playFromQueue(song: Song) {
        val index = currentPlaybackQueue.indexOfFirst {
            it.title == song.title && it.artist == song.artist
        }
        if (index != -1) {
            currentIndex = index
            _currentIndex.value = index
            _currentSong.value = currentPlaybackQueue[currentIndex]
            playMedia()
        }
    }

    fun enqueueNext(song: Song) {
        val toast = MainActivity()
        val queue = _playbackQueue.value.toMutableList()
        val current = _currentIndex.value

        val insertIndex = (current + 1).coerceAtMost(queue.size)
        queue.add(insertIndex, song)
        _playbackQueue.value = queue
        toast.showCustomToast(getApplication(), AppText.songQueueToast, true)
    }

    fun toggleShuffleMode() {
        val newValue = !_shuffleMode.value
        _shuffleMode.value = newValue
        preferences.edit { putBoolean("shuffle_mode", newValue) }
    }

    fun toggleRepeatMode() {
        val newValue = (_repeatMode.value + 1) % 3
        _repeatMode.value = newValue
        preferences.edit { putInt("repeat_mode", newValue) }
    }

    fun seekTo(position: Int) {
        mediaPlayer?.seekTo(position)
        _currentPosition.value = position
    }


    private fun saveCurrentPlaybackState() {
        val song = _currentSong.value ?: return
        val position = mediaPlayer?.currentPosition ?: 0

        preferences.edit().apply {
            putString("last_song_title", song.title)
            putString("last_song_artist", song.artist)
            putString("last_song_uri", song.uri.toString())
            putString("last_song_album_art_uri", song.albumArtUri?.toString())
            putInt("last_position", position)

            putLong("last_album_id", song.albumId ?: -1L)
            putString(
                "last_context", when {
                    currentPlaylistName != null -> "playlist"
                    currentArtistName != null -> "artist"
                    !currentAlbumSongs.isNullOrEmpty() -> "album"
                    else -> "none"
                }
            )
            putString("last_artist_name", currentArtistName)
            putString("last_playlist_name", currentPlaylistName)

            apply()
        }
    }

    private fun restoreLastPlaybackState() {
        val uriString = preferences.getString("last_song_uri", null) ?: return
        val title = preferences.getString("last_song_title", null) ?: return
        val artist = preferences.getString("last_song_artist", null) ?: return
        val albumArtUriString = preferences.getString("last_song_album_art_uri", null)
        val contextType = preferences.getString("last_context", "none")
        val savedArtistName = preferences.getString("last_artist_name", null)
        val albumId = preferences.getLong("last_album_id", -1L).takeIf { it != -1L }

        val song = Song(
            title = title,
            artist = artist,
            uri = Uri.parse(uriString),
            albumArtUri = albumArtUriString?.let { Uri.parse(it) },
            albumId = albumId
        )

        val playlistName = preferences.getString("last_playlist_name", null)
        val playlistViewModel = PlaylistViewModel(getApplication())

        val playlistSongs: List<Song>? = if (contextType == "playlist") {
            playlistViewModel.playlists.value.find { it.name == playlistName }?.songs
        } else null

        val albumSongs: List<Song>? = when (contextType) {
            "album" -> albumId?.let { id ->
                songsList.value.filter { it.albumId == id }
            }

            "artist" -> songsList.value.filter { it.artist == song.artist }
            else -> null
        }

        val songsToPlayFrom = playlistSongs ?: albumSongs
        val artistName: String? = if (contextType == "artist") savedArtistName else null

        playFrom(song, songsToPlayFrom, artistName, playlistName, contextType)
        togglePlayPause()
    }

    private fun loadAlbumArt(uri: Uri?): Bitmap? {
        return try {
            uri?.let {
                val inputStream = getApplication<Application>().contentResolver.openInputStream(it)
                BitmapFactory.decodeStream(inputStream)
            }
        } catch (e: Exception) {
            null
        }
    }

    fun loadRecentlyAddedSongs(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val contentResolver = context.contentResolver
            val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

            val projection = arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.IS_MUSIC,
                MediaStore.Audio.Media.DATE_ADDED,
                MediaStore.Audio.Media.ALBUM_ID
            )

            val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
            val sortOrder = "${MediaStore.Audio.Media.DATE_ADDED} DESC"

            val cursor = contentResolver.query(
                uri,
                projection,
                selection,
                null,
                sortOrder
            )

            val songs = mutableListOf<Song>()

            cursor?.use {
                val idColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val dateAddedColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)


                var count = 0
                while (it.moveToNext() && count < 30) {
                    val id = it.getLong(idColumn)
                    val title = it.getString(titleColumn) ?: "Desconocido"
                    val artist = it.getString(artistColumn) ?: "Artista desconocido"

                    val dateAddedSeconds = it.getLong(dateAddedColumn)
                    val timestamp = LocalDateTime.ofInstant(
                        Instant.ofEpochSecond(dateAddedSeconds),
                        ZoneId.systemDefault()
                    )

                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        id
                    )

                    val albumIdColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                    val albumId = it.getLong(albumIdColumn)
                    val albumArtUri = ContentUris.withAppendedId(
                        "content://media/external/audio/albumart".toUri(),
                        albumId
                    )

                    songs.add(Song(title, artist, contentUri, timestamp, albumArtUri))
                    count++
                }
            }

            _recentSongs.value = songs
        }
    }

    private fun loadHistoryFromStorage() {
        viewModelScope.launch(Dispatchers.IO) {
            val loaded = historyStorage.loadHistory()
            val today = LocalDate.now()
            val todayOnly = loaded.filter { it.timestamp.toLocalDate() == today }

            historyStorage.saveHistory(todayOnly)

            _allHistory.value = todayOnly
        }
    }

    fun getSongsByAlbumArtUri(albumArtUri: Uri): List<Song> {
        return songsList.value.filter { it.albumArtUri == albumArtUri }
    }

    fun getDuration(uri: Uri): Int {
        return try {
            val mmr = MediaMetadataRetriever()
            mmr.setDataSource(getApplication(), uri)
            val durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            durationStr?.toIntOrNull() ?: 0
        } catch (e: Exception) {
            0
        }
    }

    suspend fun getTotalDuration(songs: List<Song>): Int {
        return withContext(Dispatchers.IO) {
            songs.sumOf { song ->
                song.uri.let { getDuration(it) }
            }
        }
    }

    fun loadDownloadedSongsGroupedByDate(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val contentResolver = context.contentResolver
            val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

            val projection = arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.ALBUM_ARTIST,
                MediaStore.Audio.Media.YEAR,
                MediaStore.Audio.Media.TRACK,
                MediaStore.Audio.Media.IS_MUSIC,
                MediaStore.Audio.Media.DATE_ADDED,
                MediaStore.Audio.Media.ALBUM_ID
            )


            val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
            val sortOrder = "${MediaStore.Audio.Media.DATE_ADDED} DESC"

            val cursor = contentResolver.query(uri, projection, selection, null, sortOrder)

            val allSongs = mutableListOf<Song>()

            cursor?.use {
                val titleColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val idColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val albumIdColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                val dateAddedColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
                val trackColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK)
                val albumArtistColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ARTIST)
                val yearColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR)
                val albumColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)

                while (it.moveToNext()) {
                    val id = it.getLong(idColumn)
                    val songUri = Uri.withAppendedPath(uri, id.toString())
                    val title = it.getString(titleColumn)
                    val artist = it.getString(artistColumn)
                    val rawTrackNumber = it.getInt(trackColumn)
                    val albumArtist = it.getString(albumArtistColumn)
                    val year = it.getInt(yearColumn)
                    val trackNumber = rawTrackNumber % 1000
                    val albumName = it.getString(albumColumn)

                    val dateAddedSeconds = it.getLong(dateAddedColumn)
                    val timestamp = LocalDateTime.ofInstant(
                        Instant.ofEpochSecond(dateAddedSeconds),
                        ZoneId.systemDefault()
                    )

                    val albumId = it.getLong(albumIdColumn)
                    val albumArtUri = ContentUris.withAppendedId(
                        "content://media/external/audio/albumart".toUri(),
                        albumId
                    )

                    allSongs.add(
                        Song(
                            title,
                            artist,
                            songUri,
                            timestamp,
                            albumArtUri,
                            trackNumber = trackNumber,
                            albumId = albumId,
                            albumArtist = albumArtist,
                            year = year,
                            albumName = albumName
                        )
                    )
                }
            }

            val groupedByDay = allSongs.groupBy { it.timestamp.toLocalDate() }

            val months = groupedByDay
                .keys
                .map { it.withDayOfMonth(1) }
                .distinct()

            val fullMonthEntries = months.map { month ->
                val year = month.year
                val monthValue = month.monthValue
                val daysInMonth = Month.of(monthValue).length(Year.of(year).isLeap)

                val dayEntries = (1..daysInMonth).map { day ->
                    val date = LocalDate.of(year, monthValue, day)
                    val songsForDay = groupedByDay[date] ?: emptyList()
                    DayEntry(day = day, songs = songsForDay)
                }

                MonthEntry(year = year, month = monthValue, days = dayEntries)
            }.sortedByDescending { it.year * 100 + it.month }

            _downloadedSongsByMonth.value = fullMonthEntries
        }
    }

    fun setSelectedSongsForDay(songs: List<Song>) {
        _selectedSongsForDay.value = songs
    }

    fun setEditingSongs(songs: List<Song>) {
        _editingSongs.value = songs
    }

    @RequiresApi(Build.VERSION_CODES.R)
    fun updateSong(context: Context, updated: Song) {
        val uri = updated.uri

        try {
            val fileDescriptor = context.contentResolver.openFileDescriptor(uri, "rw") ?: return
            val filePath = "/proc/self/fd/${fileDescriptor.fd}"
            val extension = context.contentResolver.getType(uri)?.substringAfterLast('/')?.lowercase()
                ?: uri.toString().substringAfterLast('.').lowercase()

            if (extension.contains("mpeg") || uri.toString().endsWith(".mp3")) {
                val mp3 = Mp3File(filePath)

                val newTag = if (mp3.hasId3v2Tag()) mp3.id3v2Tag else ID3v24Tag()

                newTag.title = updated.title
                newTag.artist = updated.artist
                newTag.album = updated.albumName ?: ""
                newTag.albumArtist = updated.albumArtist ?: ""
                newTag.track = updated.trackNumber?.toString()
                newTag.year = updated.year?.toString()

                updated.albumArtUri?.let { artUri ->
                    val imageBytes = context.contentResolver.openInputStream(artUri)?.use { it.readBytes() }
                    if (imageBytes != null) {
                        val mimeType = context.contentResolver.getType(artUri) ?: "image/jpeg"
                        newTag.setAlbumImage(imageBytes, mimeType)
                    }
                }

                mp3.id3v2Tag = newTag

                val tempFile = File.createTempFile("edited", ".mp3", context.cacheDir)
                mp3.save(tempFile.absolutePath)

                context.contentResolver.openOutputStream(uri)?.use { output ->
                    tempFile.inputStream().use { input ->
                        input.copyTo(output)
                    }
                }

                tempFile.delete()

                _songs.value = _songs.value.map { if (it.uri == updated.uri) updated else it }
                _selectedSongsForDay.value = _selectedSongsForDay.value.map { if (it.uri == updated.uri) updated else it }

                loadSongs()
                loadDownloadedSongsGroupedByDate(context)

                if (context is MainActivity) {
                    context.showCustomToast(context, AppText.songSuccessToast, true)
                }
            } else {
                if (context is MainActivity) {
                    context.showCustomToast(context, AppText.notSupportedToast, false)
                }
            }

        } catch (e: RecoverableSecurityException) {
            if (context is MainActivity) {
                context.showCustomToast(context, AppText.requiredPermissionToast, false)
                context.requestEditPermission(updated)
            }
        } catch (e: Exception) {
            if (context is MainActivity) {
                context.showCustomToast(context, AppText.errorUpdateToast, false)
                context.showCustomToast(context, "${AppText.errorUpdateToast}: ${e.localizedMessage}", false)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    fun updateSongs(context: Context, updatedSongs: List<Song>) {
        try {
            if (context is MainActivity) {
                val urisWithoutPermission = updatedSongs.map { it.uri }
                    .filter { uri ->
                        context.checkUriPermission(uri, android.os.Process.myPid(), android.os.Process.myUid(), Intent.FLAG_GRANT_WRITE_URI_PERMISSION) != PackageManager.PERMISSION_GRANTED
                    }

                if (urisWithoutPermission.isNotEmpty()) {
                    val pendingIntent = MediaStore.createWriteRequest(context.contentResolver, urisWithoutPermission)
                    val request = IntentSenderRequest.Builder(pendingIntent.intentSender).build()
                    context.pendingEditedSongs = updatedSongs
                    context.editMultipleSongsLauncher.launch(request)
                    return
                }
            }

            updatedSongs.forEach { updated ->
                val uri = updated.uri

                try {
                    val fileDescriptor = context.contentResolver.openFileDescriptor(uri, "rw") ?: return@forEach
                    val filePath = "/proc/self/fd/${fileDescriptor.fd}"
                    val extension = context.contentResolver.getType(uri)?.substringAfterLast('/')?.lowercase()
                        ?: uri.toString().substringAfterLast('.').lowercase()

                    if (extension.contains("mpeg") || uri.toString().endsWith(".mp3")) {
                        val mp3 = Mp3File(filePath)

                        val newTag = if (mp3.hasId3v2Tag()) mp3.id3v2Tag else ID3v24Tag()

                        newTag.title = updated.title.toString()
                        newTag.artist = updated.artist.toString()
                        newTag.album = updated.albumName?.toString() ?: ""
                        newTag.albumArtist = updated.albumArtist?.toString() ?: ""
                        newTag.track = updated.trackNumber?.toString() ?: ""
                        newTag.year = updated.year?.toString() ?: ""

                        updated.albumArtUri?.let { artUri ->
                            val imageBytes = context.contentResolver.openInputStream(artUri)?.use { it.readBytes() }
                            if (imageBytes != null) {
                                val mimeType = context.contentResolver.getType(artUri) ?: "image/jpeg"
                                newTag.setAlbumImage(imageBytes, mimeType)
                            }
                        }

                        mp3.id3v2Tag = newTag

                        val tempFile = File.createTempFile("edited", ".mp3", context.cacheDir)
                        mp3.save(tempFile.absolutePath)

                        context.contentResolver.openOutputStream(uri)?.use { output ->
                            tempFile.inputStream().use { input ->
                                input.copyTo(output)
                            }
                        }

                        tempFile.delete()

                        _songs.value = _songs.value.map { if (it.uri == updated.uri) updated else it }
                        _selectedSongsForDay.value = _selectedSongsForDay.value.map { if (it.uri == updated.uri) updated else it }

                    }

                } catch (e: RecoverableSecurityException) {
                    if (context is MainActivity) {
                        context.showCustomToast(context, AppText.requiredPermissionToast, false)
                        context.requestEditPermission(updated)
                    }
                } catch (e: Exception) {
                    if (context is MainActivity) {
                        context.showCustomToast(context, AppText.errorUpdateToast, false)
                    }
                }
            }

            loadSongs()
            loadDownloadedSongsGroupedByDate(context)

            if (context is MainActivity) {
                context.showCustomToast(context, AppText.SuccessSongsToast, true)
            }

        } catch (e: Exception) {
            if (context is MainActivity) {
                context.showCustomToast(context, AppText.errorUpdatesToast, false)
            }
        }
    }

}
