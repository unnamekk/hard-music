package com.example.hardemusic.gui.screens

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.example.hardemusic.data.Album
import com.example.hardemusic.data.AppText
import com.example.hardemusic.data.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import kotlin.random.Random
import androidx.compose.foundation.lazy.items
import com.example.hardemusic.MainActivity


@RequiresApi(Build.VERSION_CODES.R)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditSongScreen(
    song: Song,
    albums: List<Album>,
    onSave: (Song) -> Unit,
    navController: NavController,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    BackHandler {
        navController.navigate("calendar") {
            popUpTo("calendar") { inclusive = false }
            launchSingleTop = true
        }
    }

    var title by remember { mutableStateOf(song.title) }
    var artists by remember { mutableStateOf(song.artist.split(", ").toMutableList()) }

    var albumArtUri by remember { mutableStateOf<Uri?>(song.albumArtUri) }


    val artistAlbums = remember(song, albums) {
        albums.filter { it.artist.equals(song.artist, ignoreCase = true) }
    }

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { albumArtUri = it }
    }

    var selectedAlbum by remember {
        mutableStateOf(albums.find { it.id == song.albumId })
    }

    var albumFieldText by remember { mutableStateOf(selectedAlbum?.name ?: "") }
    var showAlbumMenu by remember { mutableStateOf(false) }

    var albumArtistField by remember { mutableStateOf(song.albumArtist ?: "") }
    var albumYearField by remember { mutableStateOf(song.year?.toString() ?: "") }
    var trackNumber by remember { mutableStateOf(song.trackNumber?.toString() ?: "") }


    val embeddedBytes by produceState<ByteArray?>(initialValue = null, key1 = song.uri) {
        value = loadEmbeddedPictureBytes(context, song.uri)
    }

    var searchResults by remember { mutableStateOf<List<String>>(emptyList()) }
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(AppText.editLabelScreen, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val updatedSong = song.copy(
                            title = title,
                            artist = artists.joinToString(", "),
                            albumId = selectedAlbum?.id ?: Random.nextLong(),
                            albumName = albumFieldText,
                            albumArtist = albumArtistField,
                            year = albumYearField.toIntOrNull(),
                            trackNumber = trackNumber.toIntOrNull(),
                            albumArtUri = albumArtUri
                        )
                        onSave(updatedSong)
                    }) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Guardar",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black,
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = Color.Black
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            item {
                Text(AppText.fileRouteTitle, color = Color.Cyan)
                Text(song.path.toString(), color = Color.White, fontSize = 12.sp)

                var useAlbumArt by remember { mutableStateOf(false) }

                Spacer(Modifier.height(16.dp))
                Text(
                    AppText.coverTitle,
                    color = Color.Cyan,
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(Modifier.height(8.dp))

                val imageModel: Any? = when {
                    albumArtUri != null -> albumArtUri
                    useAlbumArt -> selectedAlbum?.albumArtUri
                    else -> embeddedBytes
                }

                val scope = rememberCoroutineScope()
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (imageModel != null) {
                        AsyncImage(
                            model = imageModel,
                            contentDescription = null,
                            modifier = Modifier
                                .size(150.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, Color.Gray, RoundedCornerShape(12.dp))
                                .clickable { pickImageLauncher.launch("image/*") },
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(150.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, Color.Gray, RoundedCornerShape(12.dp))
                                .clickable { pickImageLauncher.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.MusicNote,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(64.dp)
                            )
                        }
                    }
                    val toast = MainActivity()
                    Button(
                        onClick = {

                            if (!isInternetAvailable(context)) {
                                toast.showCustomToast(context, "Se requiere conexión a Internet", false)
                                return@Button
                            }

                            scope.launch {
                                val results = CoverArtService.searchCovers(
                                    artist = artists.firstOrNull() ?: "",
                                    album = albumFieldText
                                )
                                searchResults = results
                                showDialog = results.isNotEmpty()
                            }
                        },
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1B5E20),
                            contentColor = Color.White
                        ),
                        modifier = Modifier.wrapContentWidth()
                    ) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Buscar carátula",
                            tint = Color.White
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(AppText.searchOnlineButton)
                    }
                }

                if (showDialog) {
                    AlertDialog(
                        onDismissRequest = { showDialog = false },
                        title = { Text("Selecciona una carátula") },
                        text = {
                            LazyColumn {
                                items(searchResults) { url ->
                                    AsyncImage(
                                        model = url,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(200.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable {
                                                scope.launch {
                                                    val tempUri =
                                                        saveImageFromUrlToCache(context, url)
                                                    if (tempUri != null) {
                                                        albumArtUri = tempUri
                                                    }
                                                    showDialog = false
                                                }
                                            },
                                        contentScale = ContentScale.Crop
                                    )
                                    Spacer(Modifier.height(12.dp))
                                }
                            }
                        },
                        confirmButton = {}
                    )
                }

                Spacer(Modifier.height(12.dp))

                if (selectedAlbum?.albumArtUri != null) {
                    Button(
                        onClick = {
                            useAlbumArt = true
                            albumArtUri = selectedAlbum?.albumArtUri
                        },
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF0D47A1),
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Icon(
                            Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(AppText.useCoverButton)
                    }
                }

                Spacer(Modifier.height(16.dp))
                Text(AppText.songTitleTitle, color = Color.Cyan)
                TextField(
                    value = title,
                    onValueChange = { title = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = textFieldColors()
                )



                Spacer(Modifier.height(16.dp))
                Text(AppText.artistsLabelTitle, color = Color.Cyan)
            }

            itemsIndexed(artists) { index, artist ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextField(
                        value = artist,
                        onValueChange = { newValue ->
                            artists = artists.toMutableList().also { it[index] = newValue }
                        },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = textFieldColors()
                    )


                    if (artists.size > 1) {
                        IconButton(onClick = {
                            artists = artists.toMutableList().also { it.removeAt(index) }
                        }) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Eliminar artista",
                                tint = Color.Red
                            )
                        }
                    }

                    if (index == artists.lastIndex) {
                        IconButton(onClick = {
                            artists = artists.toMutableList().apply { add("") }
                        }) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Agregar artista",
                                tint = Color.White
                            )
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            item {
                Spacer(Modifier.height(8.dp))
                Text(AppText.albumLabelTitle, color = Color.Cyan)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextField(
                        value = albumFieldText,
                        onValueChange = { newText ->
                            albumFieldText = newText; selectedAlbum = null
                        },
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp),
                        singleLine = true,
                        colors = textFieldColors()
                    )

                    if (artistAlbums.isNotEmpty()) {
                        IconButton(onClick = { showAlbumMenu = true }) {
                            Icon(
                                Icons.Default.Album,
                                contentDescription = "Elegir álbum",
                                tint = Color.White
                            )
                        }

                        DropdownMenu(
                            expanded = showAlbumMenu,
                            onDismissRequest = { showAlbumMenu = false }) {
                            artistAlbums.forEach { album ->
                                DropdownMenuItem(
                                    text = { Text(album.name) },
                                    onClick = {
                                        selectedAlbum = album
                                        albumFieldText = album.name
                                        showAlbumMenu = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
                Text(AppText.artistAlbumLabelTitle, color = Color.Cyan)
                TextField(
                    value = albumArtistField,
                    onValueChange = { albumArtistField = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = textFieldColors()
                )

                Spacer(Modifier.height(16.dp))
                Text(AppText.yearLabelTitle, color = Color.Cyan)
                TextField(
                    value = albumYearField,
                    onValueChange = { albumYearField = it.filter { it.isDigit() } },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = textFieldColors(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                Spacer(Modifier.height(16.dp))
                Text(AppText.trackNumberLabelTitle, color = Color.Cyan)
                TextField(
                    value = trackNumber,
                    onValueChange = { trackNumber = it.filter { it.isDigit() } },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = textFieldColors(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        }
    }
}

@Composable
private fun textFieldColors() = TextFieldDefaults.colors(
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedContainerColor = Color.DarkGray,
    unfocusedContainerColor = Color.DarkGray,
    cursorColor = Color.Cyan,
    focusedIndicatorColor = Color.Cyan,
    unfocusedIndicatorColor = Color.Gray
)

fun isInternetAvailable(context: Context): Boolean {
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork ?: return false
    val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}

enum class EditMode { NO_CHANGE, NEW }

data class MultiEditField<T>(
    var mode: EditMode,
    var value: T? = null
)

private fun <T> MultiEditField<T>.orKeep(current: T): T {
    return if (mode == EditMode.NEW) (value ?: current) else current
}

private fun <T> MultiEditField<T>.orKeepNullable(current: T?): T? {
    return if (mode == EditMode.NEW) (value ?: current) else current
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditMultipleSongsScreen(
    songs: List<Song>,
    albums: List<Album>,
    onSave: (List<Song>) -> Unit,
    onBack: () -> Unit
) {

    fun <T> initField(): MultiEditField<T> {
        return MultiEditField(EditMode.NO_CHANGE, null)
    }

    var titleField by remember { mutableStateOf(initField<String>()) }
    var artistField by remember { mutableStateOf(initField<String>()) }
    var albumField by remember { mutableStateOf(initField<String>()) }
    var albumArtistField by remember { mutableStateOf(initField<String>()) }
    var yearField by remember { mutableStateOf(initField<Int>()) }
    var trackNumberField by remember { mutableStateOf(initField<Int>()) }

    var albumArtField by remember { mutableStateOf(initField<Uri>()) }


    val allArtists = remember(songs) {
        songs.flatMap { it.artist.split(", ") }
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()
    }

    val artistAlbumNames = remember(songs, albums) {
        albums.filter { album ->
            allArtists.any { artistName ->
                album.artist.equals(artistName, ignoreCase = true)
            }
        }.mapNotNull { it.name }
    }

    val songAlbumNames = remember(songs) { songs.mapNotNull { it.albumName } }

    val existingAlbumNames = remember(songAlbumNames, artistAlbumNames) {
        (songAlbumNames + artistAlbumNames).distinct()
    }

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            albumArtField = albumArtField.copy(mode = EditMode.NEW, value = it)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(AppText.mutipleEditionTitle, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val updated = songs.map { song ->
                            song.copy(
                                title = titleField.orKeep(song.title),
                                artist = artistField.orKeep(song.artist),
                                albumName = albumField.orKeepNullable(song.albumName),
                                albumArtist = albumArtistField.orKeepNullable(song.albumArtist),
                                year = yearField.orKeepNullable(song.year),
                                trackNumber = trackNumberField.orKeepNullable(song.trackNumber),
                                albumArtUri = albumArtField.orKeepNullable(song.albumArtUri)
                            )
                        }
                        onSave(updated)
                    }) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Guardar",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
            )
        },
        containerColor = Color.Black
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            item {

                val songsForPreview = remember(songs) { songs.map { it.title to it.uri } }

                val albumArtUris = remember(songs) { songs.mapNotNull { it.albumArtUri } }

                ImageFieldWithModeSelector(
                    label = AppText.coverTitle,
                    field = albumArtField,
                    onChange = { m, v -> albumArtField = albumArtField.copy(mode = m, value = v) },
                    albumArtUris = albumArtUris,
                    songsForPreview = songsForPreview,
                    onPickNewImage = { pickImageLauncher.launch("image/*") }
                )

                FieldWithModeSelector(
                    label = AppText.songTitleTitle,
                    field = titleField,
                    onChange = { m, v -> titleField = titleField.copy(mode = m, value = v) },
                    existingValues = songs.map { it.title },
                    parse = { it }
                )

                FieldWithModeSelector(
                    label = AppText.artistsLabelTitle,
                    field = artistField,
                    onChange = { m, v -> artistField = artistField.copy(mode = m, value = v) },
                    existingValues = songs.map { it.artist },
                    parse = { it }
                )

                FieldWithModeSelector(
                    label = AppText.albumLabelTitle,
                    field = albumField,
                    onChange = { m, v -> albumField = albumField.copy(mode = m, value = v) },
                    existingValues = existingAlbumNames,
                    parse = { it }
                )

                FieldWithModeSelector(
                    label = AppText.artistAlbumLabelTitle,
                    field = albumArtistField,
                    onChange = { m, v ->
                        albumArtistField = albumArtistField.copy(mode = m, value = v)
                    },
                    existingValues = songs.map { it.albumArtist },
                    parse = { it }
                )

                FieldWithModeSelector(
                    label = AppText.yearLabelTitle,
                    field = yearField,
                    onChange = { m, v -> yearField = yearField.copy(mode = m, value = v) },
                    existingValues = songs.map { it.year },
                    parse = { s -> s.filter(Char::isDigit).take(4).toIntOrNull() }
                )

                FieldWithModeSelector(
                    label = AppText.trackNumberLabelTitle,
                    field = trackNumberField,
                    onChange = { m, v ->
                        trackNumberField = trackNumberField.copy(mode = m, value = v)
                    },
                    existingValues = songs.map { it.trackNumber },
                    parse = { s -> s.filter(Char::isDigit).toIntOrNull() }
                )

            }
        }
    }
}

@Composable
fun <T> FieldWithModeSelector(
    label: String,
    field: MultiEditField<T>,
    onChange: (EditMode, T?) -> Unit,
    existingValues: List<T?>,
    parse: (String) -> T?,
    display: (T) -> String = { it.toString() }
) {
    var expanded by remember { mutableStateOf(false) }
    val textValue = remember(field.value) { mutableStateOf(field.value?.let { display(it) } ?: "") }

    Column {
        Text(label, color = Color.White, style = MaterialTheme.typography.titleMedium)

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box {
                Button(onClick = { expanded = true }, modifier = Modifier.width(140.dp)) {
                    Text(
                        when (field.mode) {
                            EditMode.NO_CHANGE -> AppText.noChangeText
                            EditMode.NEW -> AppText.newText
                        }
                    )
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    DropdownMenuItem(
                        text = { Text(AppText.noChangeText) },
                        onClick = {
                            onChange(EditMode.NO_CHANGE, null)
                            textValue.value = ""
                            expanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(AppText.newText) },
                        onClick = {
                            onChange(EditMode.NEW, null)
                            textValue.value = ""
                            expanded = false
                        }
                    )

                    val nonNullValues = existingValues.filterNotNull().distinct()
                    if (nonNullValues.isNotEmpty()) {
                        HorizontalDivider()
                        nonNullValues.forEach { v ->
                            DropdownMenuItem(
                                text = { Text(display(v)) },
                                onClick = {
                                    onChange(EditMode.NEW, v)
                                    textValue.value = display(v)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.width(8.dp))

            OutlinedTextField(
                value = textValue.value,
                onValueChange = {
                    textValue.value = it
                    onChange(field.mode, parse(it))
                },
                enabled = field.mode == EditMode.NEW,
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun ImageFieldWithModeSelector(
    label: String,
    field: MultiEditField<Uri>,
    onChange: (EditMode, Uri?) -> Unit,
    albumArtUris: List<Uri?>,
    songsForPreview: List<Pair<String, Uri>>,
    onPickNewImage: () -> Unit
) {
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var searchResults by remember { mutableStateOf<List<String>>(emptyList()) }
    var showDialog by remember { mutableStateOf(false) }

    Column {
        Text(label, color = Color.White, style = MaterialTheme.typography.titleMedium)

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box {
                Button(onClick = { expanded = true }, modifier = Modifier.width(140.dp)) {
                    Text(if (field.mode == EditMode.NO_CHANGE) AppText.noChangeText else AppText.newText)
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    DropdownMenuItem(
                        text = { Text(AppText.noChangeText) },
                        onClick = {
                            onChange(EditMode.NO_CHANGE, null)
                            expanded = false
                        }
                    )

                    DropdownMenuItem(
                        text = { Text(AppText.newImageText) },
                        onClick = {
                            expanded = false
                            onPickNewImage()
                        }
                    )


                    val nonNullAlbumArt = albumArtUris.filterNotNull().distinct()
                    if (nonNullAlbumArt.isNotEmpty()) {
                        HorizontalDivider()
                        nonNullAlbumArt.forEach { uri ->
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Image(
                                            painter = rememberAsyncImagePainter(uri),
                                            contentDescription = null,
                                            modifier = Modifier.size(40.dp)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(AppText.albumCoverText)
                                    }
                                },
                                onClick = {
                                    onChange(EditMode.NEW, uri)
                                    expanded = false
                                }
                            )
                        }
                    }

                    val songsWithIndex = songsForPreview
                    if (songsWithIndex.isNotEmpty()) {
                        HorizontalDivider()
                        songsWithIndex.forEach { (title, songUri) ->
                            val embeddedBytes by produceState<ByteArray?>(
                                initialValue = null,
                                key1 = songUri
                            ) {
                                value = loadEmbeddedPictureBytes(context, songUri)
                            }

                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (embeddedBytes != null) {
                                            AsyncImage(
                                                model = embeddedBytes,
                                                contentDescription = null,
                                                modifier = Modifier.size(40.dp)
                                            )
                                        } else {
                                            Icon(
                                                Icons.Default.MusicNote,
                                                contentDescription = null,
                                                modifier = Modifier.size(40.dp),
                                                tint = Color.Gray
                                            )
                                        }
                                        Spacer(Modifier.width(8.dp))
                                        Text(title)
                                    }
                                },
                                onClick = {
                                    scope.launch {
                                        val bytes = loadEmbeddedPictureBytes(context, songUri)
                                        if (bytes != null) {
                                            val tmpUri = writeBytesToTempImageUri(
                                                context,
                                                bytes,
                                                prefix = "from_song"
                                            )
                                            onChange(EditMode.NEW, tmpUri)
                                        } else {
                                        }
                                    }
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.width(8.dp))

            if (field.value != null) {
                Image(
                    painter = rememberAsyncImagePainter(field.value),
                    contentDescription = null,
                    modifier = Modifier.size(60.dp)
                )
            } else {
                Icon(
                    Icons.Default.MusicNote,
                    contentDescription = null,
                    modifier = Modifier.size(60.dp),
                    tint = Color.Gray
                )
            }
        }
    }
}


suspend fun loadEmbeddedPictureBytes(context: Context, audioUri: Uri): ByteArray? =
    withContext(Dispatchers.IO) {
        try {
            val mmr = MediaMetadataRetriever()
            mmr.setDataSource(context, audioUri)
            val bytes = mmr.embeddedPicture
            mmr.release()
            bytes
        } catch (e: Exception) {
            null
        }
    }


suspend fun writeBytesToTempImageUri(
    context: Context,
    bytes: ByteArray,
    prefix: String = "embedded"
): Uri = withContext(Dispatchers.IO) {
    val tmp =
        File.createTempFile("${prefix}_${System.currentTimeMillis()}", ".jpg", context.cacheDir)
    tmp.outputStream().use { it.write(bytes) }
    Uri.fromFile(tmp)
}

object CoverArtService {
    private val client = OkHttpClient()

    suspend fun searchCovers(artist: String, album: String): List<String> {
        val url = "https://itunes.apple.com/search?term=${artist}+${album}&entity=album"
        val request = Request.Builder().url(url).build()

        return withContext(Dispatchers.IO) {
            client.newCall(request).execute().use { response ->
                val json = response.body?.string() ?: return@withContext emptyList()
                val obj = JSONObject(json)
                val results = obj.getJSONArray("results")
                val covers = mutableListOf<String>()
                for (i in 0 until results.length()) {
                    val item = results.getJSONObject(i)
                    val cover = item.getString("artworkUrl100")
                        .replace("100x100", "600x600")
                    covers.add(cover)
                }
                covers
            }
        }
    }
}

suspend fun saveImageFromUrlToCache(context: Context, imageUrl: String): Uri? {
    return withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder().url(imageUrl).build()
            val client = OkHttpClient()
            val response = client.newCall(request).execute()
            val bytes = response.body?.bytes() ?: return@withContext null

            val tempFile = File.createTempFile("cover_", ".jpg", context.cacheDir)
            tempFile.outputStream().use { it.write(bytes) }

            Uri.fromFile(tempFile)
        } catch (e: Exception) {
            null
        }
    }
}