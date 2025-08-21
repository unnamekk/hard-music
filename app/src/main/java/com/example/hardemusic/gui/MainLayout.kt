package com.example.hardemusic.gui

import android.app.Application
import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.hardemusic.AppNavigation
import com.example.hardemusic.MainActivity
import com.example.hardemusic.R
import com.example.hardemusic.data.PlaylistViewModelFactory
import com.example.hardemusic.data.Song
import com.example.hardemusic.gui.screens.loadEmbeddedPictureBytes
import com.example.hardemusic.viewmodel.MainViewModel
import com.example.hardemusic.viewmodel.PlaylistViewModel
import kotlinx.coroutines.launch
import com.example.hardemusic.data.AppText


@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun MainLayout(viewModel: MainViewModel, navController: NavHostController) {
    val currentSong by viewModel.currentSong.collectAsState()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val hideBottomBarRoutes = listOf("historial", "añadidos", "settings","edit_song","edit_multiple_songs")
    val showBottomBar = currentRoute !in hideBottomBarRoutes
    val showTopBar = currentRoute !in hideBottomBarRoutes

    val topBarTitle = when (currentRoute) {
        "home" -> buildAnnotatedString {
            withStyle(style = SpanStyle(color = Color.White)) {
                append("Hard")
            }
            withStyle(style = SpanStyle(color = Color(0xFF58A6FF))) {
                append("Music")
            }
        }

        "songs" -> buildAnnotatedString { append(AppText.songsTitle) }
        "albums" -> buildAnnotatedString { append(AppText.albumsTitle) }
        "artists" -> buildAnnotatedString { append(AppText.artistsTitle) }
        "playlists" -> buildAnnotatedString { append("Playlists") }
        else -> null
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A))
            .padding(16.dp)
            .systemBarsPadding()
    ) {

        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        val hideNowPlayingBarRoutes = listOf("player", "playback_queue")
        val shouldShowNowPlayingBar = currentRoute !in hideNowPlayingBarRoutes

        val hasNowPlayingBarAppeared by viewModel.hasNowPlayingBarAppeared.collectAsState()


        if (showTopBar && topBarTitle != null) {
            TopBar(title = topBarTitle, navController = navController, viewModel = viewModel)
            Spacer(modifier = Modifier.height(16.dp))
        }

        val context = LocalContext.current.applicationContext as Application
        val playlistViewModel: PlaylistViewModel = viewModel(
            factory = PlaylistViewModelFactory(context)
        )

        Box(modifier = Modifier.weight(1f)) {
            AppNavigation(
                navController = navController,
                viewModel = viewModel,
                playlistViewModel = playlistViewModel
            )
        }

        if (currentSong != null) {
            if (shouldShowNowPlayingBar) {
                if (!hasNowPlayingBarAppeared) {
                    viewModel.markNowPlayingBarAsAppeared()
                }
                NowPlayingBar(viewModel = viewModel, navController = navController)
            }
        }

        if (showBottomBar) {
            if (shouldShowNowPlayingBar) {
                BottomNavBar(navController = navController)
            }
        }
    }
}

@Composable
fun TopBar(
    title: AnnotatedString,
    navController: NavHostController,
    viewModel: MainViewModel = viewModel()
) {
    val isSelectionMode by viewModel.isSelectionMode.collectAsState()
    val selectedSongs by viewModel.selectedSongs.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp, horizontal = 16.dp)
    ) {
        if (isSelectionMode) {
            Row(
                modifier = Modifier.align(Alignment.CenterStart),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Cancelar selección",
                    tint = Color.White,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { viewModel.clearSelection() }
                )
            }

            Text(
                text = "${selectedSongs.size}",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.align(Alignment.Center)
            )

            Row(
                modifier = Modifier.align(Alignment.CenterEnd),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Editar",
                    tint = Color.White,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable {
                            if (selectedSongs.isNotEmpty()) {
                                viewModel.setEditingSongs(selectedSongs)
                                navController.navigate("edit_multiple_songs")
                            }
                        }
                )
                Icon(
                    imageVector = Icons.Default.SelectAll,
                    contentDescription = "Seleccionar todo",
                    tint = Color.White,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable {
                            viewModel.selectAll(viewModel._songs.value)
                        }
                )
            }
        } else {
            Row(
                modifier = Modifier.align(Alignment.CenterStart),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Buscar",
                    tint = Color.White,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { navController.navigate("search") }
                )
            }

            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.align(Alignment.Center)
            )

            Row(
                modifier = Modifier.align(Alignment.CenterEnd),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = "Calendario",
                    tint = Color.White,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { navController.navigate("calendar") }
                )
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Ajustes",
                    tint = Color.White,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { navController.navigate("settings") }
                )
            }
        }
    }
}

@Composable
fun DetailTopBar(
    title: String,
    navController: NavController,
    mainViewModel: MainViewModel,
    songs: List<Song>,
    onBack: () -> Unit
) {
    val isSelectionMode by mainViewModel.isSelectionMode.collectAsState()
    val selectedSongs by mainViewModel.selectedSongs.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.align(Alignment.CenterStart),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Atrás",
                tint = Color.White,
                modifier = Modifier
                    .size(24.dp)
                    .clickable { onBack() }
            )
        }

        Text(
            text = if (isSelectionMode) "${selectedSongs.size}" else title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.align(Alignment.Center)
        )

        Row(
            modifier = Modifier.align(Alignment.CenterEnd),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (isSelectionMode) {
                Icon(
                    imageVector = Icons.Default.SelectAll,
                    contentDescription = "Seleccionar todo",
                    tint = Color.White,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable {
                            mainViewModel.selectAll(songs)
                        }
                )

                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Editar",
                    tint = Color.White,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable {
                            if (selectedSongs.isNotEmpty()) {
                                mainViewModel.setEditingSongs(selectedSongs)
                                navController.navigate("edit_multiple_songs")
                            }
                        }
                )

                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Salir selección",
                    tint = Color.White,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable {
                            mainViewModel.clearSelection()
                        }
                )
            }
        }
    }
}

@Composable
fun NowPlayingBar(viewModel: MainViewModel, navController: NavHostController) {
    val currentSong by viewModel.currentSong.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val context = LocalContext.current

    currentSong?.let { song ->

        val artworkModel = rememberSongArtwork(context, currentSong)

        val imageModel: Any? = when {
            artworkModel != null -> artworkModel
            song.albumArtUri != null -> song.albumArtUri
            else -> null
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .background(Color.DarkGray, shape = RoundedCornerShape(12.dp))
                .clickable { navController.navigate("player") }
                .padding(horizontal = 16.dp, vertical = 8.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    if (imageModel != null) {
                        AsyncImage(
                            model = imageModel,
                            contentDescription = null,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(6.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = Color.Gray
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(20.dp)
                        ) {
                            ScrollingText(song.title)
                        }
                        Text(
                            text = song.artist,
                            color = Color.LightGray,
                            fontSize = 12.sp,
                            maxLines = 1
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Anterior",
                        tint = Color.White,
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { viewModel.playPrevious() }
                    )
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = Color.White,
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { viewModel.togglePlayPause() }
                    )
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Siguiente",
                        tint = Color.White,
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { viewModel.playNext() }
                    )
                }
            }
        }
    }
}

@Composable
fun ScrollingText(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 14.sp
) {
    Text(
        text = text,
        color = Color.White,
        fontSize = fontSize,
        maxLines = 1,
        modifier = modifier.basicMarquee()
    )
}

@Composable
fun BottomNavBar(navController: NavHostController) {
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    val items = listOf(
        Triple("home", Icons.Default.Home, "Home"),
        Triple("songs", Icons.Default.LibraryMusic, "Songs"),
        Triple("albums", Icons.Default.Album, "Albums"),
        Triple("artists", Icons.Default.Mic, "Artists"),
        Triple("playlists", Icons.AutoMirrored.Filled.QueueMusic, "Playlists")
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(Color(0xFF1A1A1A)),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEach { (route, icon, _) ->
            val isSelected = currentRoute == route

            Icon(
                imageVector = icon,
                contentDescription = route,
                tint = if (isSelected) Color.Cyan else Color.White,
                modifier = Modifier
                    .size(28.dp)
                    .clickable {
                        if (!isSelected) {
                            navController.navigate(route) {
                                popUpTo("home") { inclusive = false }
                                launchSingleTop = true
                            }
                        }
                    }
            )
        }
    }
}

@Composable
fun SongListView(
    songs: List<Song>,
    onSongClick: (Song) -> Unit,
    viewModel: MainViewModel,
    navController: NavController,
    modifier: Modifier = Modifier,
    showTitle: String? = null,
    groupByInitial: Boolean = false
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(horizontal = 16.dp)
    ) {
        showTitle?.let {
            Text(
                text = it,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        if (songs.isEmpty()) {
            Text(AppText.noSongsTitle, color = Color.Gray)
        } else {
            if (groupByInitial) {
                val grouped = songs.groupBy { it.title.firstOrNull()?.uppercaseChar() ?: '#' }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    grouped.toSortedMap().forEach { (initial, songsByLetter) ->
                        item {
                            Text(
                                text = initial.toString(),
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                            )
                        }

                        items(songsByLetter) { song ->
                            SongRow(song, onSongClick, navController, viewModel)
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(songs) { song ->
                        SongRow(song, onSongClick, navController, viewModel)
                    }
                }
            }
        }
    }
}


@Composable
fun SongRow(
    song: Song,
    onSongClick: (Song) -> Unit,
    navController: NavController,
    mainViewModel: MainViewModel
) {
    val hasNowPlayingBarAppeared by mainViewModel.hasNowPlayingBarAppeared.collectAsState()

    val selectedSongs by mainViewModel.selectedSongs.collectAsState()
    val isSelectionMode by mainViewModel.isSelectionMode.collectAsState()

    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(LocalContext.current)
            .data(song.albumArtUri)
            .size(128)
            .crossfade(true)
            .memoryCacheKey(song.albumArtUri.toString())
            .placeholder(R.drawable.placeholder_album)
            .error(R.drawable.placeholder_album)
            .build()
    )

    val isSelected = selectedSongs.contains(song)

    val backgroundColor by animateColorAsState(
        if (isSelected) Color(0x502D5DFF) else Color.Transparent,
        label = "song selection background"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .padding(vertical = 8.dp, horizontal = 12.dp)
            .combinedClickable(
                onClick = {
                    if (isSelectionMode) {
                        mainViewModel.toggleSelection(song)
                    } else {
                        onSongClick(song)
                    }
                },
                onLongClick = {
                    mainViewModel.startSelection(song)
                }
            )
            .padding(vertical = 8.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    )  {
        Image(
            painter = painter,
            contentDescription = "Carátula",
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(6.dp)),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                song.title,
                color = Color.White,
                fontSize = 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                song.artist,
                color = Color.Gray,
                fontSize = 14.sp,
                maxLines = 1
            )
        }

        var expanded by remember { mutableStateOf(false) }

        IconButton(onClick = { expanded = true }) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "Más opciones",
                tint = Color.White
            )
        }

        SongOptionsMenu(
            expanded = expanded,
            onDismiss = { expanded = false },
            song = song,
            navController = navController,
            mainViewModel = mainViewModel,
            hasNowPlayingBarAppeared = hasNowPlayingBarAppeared
        )

    }
}


@Composable
fun SongOptionsMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    song: Song,
    navController: NavController,
    mainViewModel: MainViewModel,
    hasNowPlayingBarAppeared: Boolean
) {

    val context = LocalContext.current
    val hideNowRoutes = listOf("player")
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val playlistName = navBackStackEntry?.arguments?.getString("playlistName")
    val currentRoute = navBackStackEntry?.destination?.route
    val playlistViewModel: PlaylistViewModel = viewModel()
    val toast = MainActivity()

    var showPlaylistDialog by remember { mutableStateOf(false) }
    val playlists by playlistViewModel.playlists.collectAsState()

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss
    ) {

        if (hasNowPlayingBarAppeared && currentRoute !in hideNowRoutes) {
            DropdownMenuItem(
                text = { Text(AppText.nextQueueOption) },
                onClick = {
                    onDismiss()
                    mainViewModel.enqueueNext(song)
                }
            )
        }

        if (currentRoute?.startsWith("album_detail") != true) {
            DropdownMenuItem(
                text = { Text(AppText.goAlbumOption) },
                onClick = {
                    onDismiss()
                    song.albumId?.let { albumId ->
                        navController.navigate("album_detail/$albumId")
                    }
                }
            )
        }

        if (currentRoute?.startsWith("artist_detail") != true) {
            DropdownMenuItem(
                text = { Text(AppText.goArtistOption) },
                onClick = {
                    onDismiss()
                    navController.navigate("artist_detail/${song.artist}")
                }
            )
        }

        if (currentRoute?.startsWith("playlist_detail") != true) {
            DropdownMenuItem(
                text = { Text(AppText.addPlaylistOption) },
                onClick = {
                    onDismiss()
                    showPlaylistDialog = true
                }
            )
        }


        DropdownMenuItem(
            text = { Text(AppText.editLabelOption) },
            onClick = {
                onDismiss()
                mainViewModel.setEditingSong(song)
                navController.navigate("edit_song")
            }
        )

        if (currentRoute?.startsWith("playlist_detail") != false) {
            DropdownMenuItem(
                text = { Text(AppText.deletePlaylistOption, color = Color.Red) },
                onClick = {
                    onDismiss()
                    playlistViewModel.removeSongFromPlaylist(playlistName.toString(), song)
                    playlistViewModel.reloadPlaylistDetails(navController, playlistName.toString())
                    toast.showCustomToast(context, AppText.deletedSongToast, false)
                }
            )
        }

        DropdownMenuItem(
            text = { Text(AppText.deleteDeviceOption, color = Color.Red) },
            onClick = {
                onDismiss()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    (context as? MainActivity)?.deleteSongAndRefresh(song)
                } else {
                    Toast.makeText(
                        context,
                        AppText.deleteDevice11Option,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )
    }


    if (showPlaylistDialog) {
        AlertDialog(
            onDismissRequest = { showPlaylistDialog = false },
            title = { Text(AppText.selectPlaylistOption) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    playlists.forEach { playlist ->
                        Text(
                            text = playlist.name,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    playlistViewModel.addSongToPlaylist(playlist.name, song) { message, isSuccess ->
                                        toast.showCustomToast(context, message, isSuccess)
                                    }
                                    showPlaylistDialog = false
                                }
                                .padding(8.dp),
                            color = Color.White
                        )
                    }
                }
            },
            confirmButton = {},
            containerColor = Color(0xFF2C2C2C)
        )
    }
}

@Composable
fun <T> AlphabetScrollbar(
    groupedItems: Map<Char, List<T>>,
    listState: LazyListState,
    modifier: Modifier = Modifier,
    onScrollToIndex: (Char) -> Int
) {
    val scope = rememberCoroutineScope()

    LazyColumn(
        modifier = modifier
            .padding(end = 8.dp)
            .height(200.dp)
            .height(200.dp)
    ) {
        items(groupedItems.keys.toList()) { letter ->
            Text(
                text = letter.toString(),
                color = Color.White,
                fontSize = 12.sp,
                modifier = Modifier
                    .padding(vertical = 2.dp)
                    .clickable {
                        val index = onScrollToIndex(letter)
                        scope.launch {
                            listState.animateScrollToItem(index)
                        }
                    }
            )
        }
    }
}

@Composable
fun rememberSongArtwork(context: Context, song: Song?): Any? {
    val embeddedBytes by produceState<ByteArray?>(initialValue = null, song) {
        value = song?.let { loadEmbeddedPictureBytes(context, it.uri) }
    }

    return when {
        embeddedBytes != null -> embeddedBytes
        song?.albumArtUri != null -> song.albumArtUri
        else -> null
    }
}


