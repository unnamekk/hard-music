package com.example.hardemusic

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.hardemusic.gui.screens.AlbumDetailScreen
import com.example.hardemusic.gui.screens.AlbumGroupDetailScreen
import com.example.hardemusic.gui.screens.AlbumsScreen
import com.example.hardemusic.gui.screens.ArtistDetailScreen
import com.example.hardemusic.gui.screens.ArtistsScreen
import com.example.hardemusic.gui.screens.CalendarScreen
import com.example.hardemusic.gui.screens.DailyHistoryScreen
import com.example.hardemusic.gui.screens.EditMultipleSongsScreen
import com.example.hardemusic.gui.screens.EditSongScreen
import com.example.hardemusic.gui.screens.MainScreen
import com.example.hardemusic.gui.screens.PlayerQueueScreen
import com.example.hardemusic.gui.screens.PlayerScreen
import com.example.hardemusic.gui.screens.PlaylistDetailScreen
import com.example.hardemusic.gui.screens.PlaylistsScreen
import com.example.hardemusic.gui.screens.RecentlyAddedScreen
import com.example.hardemusic.gui.screens.SearchScreen
import com.example.hardemusic.gui.screens.SettingsScreen
import com.example.hardemusic.gui.screens.SongSelectionScreen
import com.example.hardemusic.gui.screens.SongsScreen
import com.example.hardemusic.viewmodel.AlbumsViewModel
import com.example.hardemusic.viewmodel.ArtistsViewModel
import com.example.hardemusic.viewmodel.MainViewModel
import com.example.hardemusic.viewmodel.PlaylistViewModel
import com.example.hardemusic.viewmodel.SearchViewModel
import com.example.hardemusic.viewmodel.UserProfileViewModel


@RequiresApi(Build.VERSION_CODES.R)
@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun AppNavigation(
    navController: NavHostController,
    viewModel: MainViewModel,
    albumsViewModel: AlbumsViewModel = viewModel(),
    playlistViewModel: PlaylistViewModel
) {
    val userProfileViewModel: UserProfileViewModel = viewModel()
    val artistsViewModel = remember { ArtistsViewModel(viewModel) }

    val searchViewModel = remember {
        SearchViewModel(
            songsFlow = viewModel.songsList,
            artistsFlow = artistsViewModel.artists,
            albumsFlow = albumsViewModel.albums
        )
    }

    NavHost(navController = navController, startDestination = "home") {
        composable("home") { MainScreen(navController, viewModel, userProfileViewModel) }

        composable("historial") {
            DailyHistoryScreen(
                viewModel = viewModel,
                navController = navController,
                onBack = { navController.popBackStack() }
            )
        }

        composable("añadidos") {
            RecentlyAddedScreen(viewModel = viewModel,navController = navController, onBack = { navController.popBackStack() })
        }

        composable("settings") {
            SettingsScreen(viewModel = viewModel, navController = navController, albumsViewModel = albumsViewModel)
        }

        composable("songs") { SongsScreen(viewModel, navController) }

        composable("albums") {
            AlbumsScreen(
                viewModel = albumsViewModel,
                onAlbumClick = { album ->
                    navController.navigate("album_detail/${album.id}")
                }
            )
        }

        composable("artists") {
            val artistsViewModel = remember { ArtistsViewModel(viewModel) }

            ArtistsScreen(
                artistsViewModel = artistsViewModel,
                navController = navController
            )
        }

        composable("calendar") {
            val context = LocalContext.current
            CalendarScreen(viewModel = viewModel, context = context, navController = navController)
        }

        composable(
            route = "day_songs/{date}",
            arguments = listOf(navArgument("date") { type = NavType.StringType })
        ) { backStackEntry ->
            val date = backStackEntry.arguments?.getString("date") ?: ""
            SongSelectionScreen(date = date, viewModel = viewModel, navController = navController) {
                navController.popBackStack()
            }
        }

        composable("edit_song") {
            albumsViewModel.loadAlbums()
            val song = viewModel.editingSong.collectAsState().value
            val albums by albumsViewModel.albums.collectAsState()
            val context = LocalContext.current

            if (song != null && albums.isNotEmpty()) {
                EditSongScreen(
                    song = song,
                    albums = albums,
                    onSave = { updatedSong ->
                        viewModel.updateSong(context, updatedSong)
                        navController?.navigate("calendar") {
                            popUpTo(navController!!.graph.startDestinationId) {
                                inclusive = true
                            }
                        }
                    },
                    navController = navController,
                    onBack = {
                        navController.navigate("calendar")
                    }
                )
            }
        }

        composable("edit_multiple_songs") {
            albumsViewModel.loadAlbums()
            val songs = viewModel.editingSongs.collectAsState().value
            val albums by albumsViewModel.albums.collectAsState()
            val context = LocalContext.current

            if (songs.isNotEmpty()) {
                EditMultipleSongsScreen(
                    songs = songs,
                    albums = albums,
                    onSave = { updatedSongs ->
                        viewModel.updateSongs(context, updatedSongs)
                        navController?.navigate("calendar") {
                            popUpTo(navController!!.graph.startDestinationId) {
                                inclusive = true
                            }
                        }
                    },
                    onBack = {
                        navController.navigate("calendar")
                    }
                )
            }
        }


        composable(
            route = "artist_detail/{artistName}",
            arguments = listOf(navArgument("artistName") { type = NavType.StringType })
        ) { backStackEntry ->
            val artistName = backStackEntry.arguments?.getString("artistName") ?: return@composable
            ArtistDetailScreen(
                artistName = artistName,
                mainViewModel = viewModel,
                albumsViewModel = albumsViewModel,
                navController = navController,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "album_group_detail/{albumName}/{artistName}",
            arguments = listOf(
                navArgument("albumName") { type = NavType.StringType },
                navArgument("artistName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val albumName = backStackEntry.arguments?.getString("albumName") ?: ""
            val artistName = backStackEntry.arguments?.getString("artistName") ?: ""

            AlbumGroupDetailScreen(
                albumName = albumName,
                artistName = artistName,
                mainViewModel = viewModel,
                albumsViewModel = albumsViewModel,
                navController = navController,
                onBack = { navController.popBackStack() },
                hasNowPlayingBarAppeared = viewModel.hasNowPlayingBarAppeared.value
            )
        }

        composable("search") {
            SearchScreen(
                navController = navController,
                mainViewModel = viewModel,
                searchViewModel = searchViewModel
            )
        }

        composable("playlists") {
            LaunchedEffect(Unit) {
                playlistViewModel.forceReload()
            }

            PlaylistsScreen(
                playlists = playlistViewModel.playlists.collectAsState().value,
                onCreatePlaylist = { name, imageUri -> playlistViewModel.createPlaylist(name, imageUri) },
                onPlaylistClick = { playlist -> navController.navigate("playlist_detail/${playlist.name}") },
                onEditPlaylist = { newName, newImageUri, oldPlaylist ->
                    playlistViewModel.editPlaylist(
                        originalName = oldPlaylist.name,
                        newName = newName,
                        newImageUri = newImageUri
                    )
                },
                onDeletePlaylist = { playlist ->
                    playlistViewModel.deletePlaylist(playlist.name)
                }
            )
        }

        composable(
            route = "playlist_detail/{playlistName}",
            arguments = listOf(navArgument("playlistName") { type = NavType.StringType })
        ) { backStackEntry ->
            val playlistName = Uri.decode(backStackEntry.arguments?.getString("playlistName") ?: return@composable)

            LaunchedEffect(Unit) {
                playlistViewModel.forceReload()
            }

            PlaylistDetailScreen(
                playlistName = playlistName,
                playlistViewModel = playlistViewModel,
                onBack = { navController.popBackStack() },
                mainViewModel = viewModel,
                navController = navController
            )
        }

        composable("player") {
            PlayerScreen(viewModel = viewModel, navController = navController, onBack = { navController.popBackStack() })
        }

        composable("playback_queue") {
            PlayerQueueScreen(
                mainViewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "album_detail/{albumId}",
            arguments = listOf(navArgument("albumId") { type = NavType.LongType })
        ) { backStackEntry ->
            val albumId = backStackEntry.arguments?.getLong("albumId") ?: return@composable

            val album = albumsViewModel.albums.value.firstOrNull { it.id == albumId } ?: return@composable
            val songs = viewModel.getSongsByAlbumArtUri(album.albumArtUri)

            AlbumDetailScreen(
                album = album,
                songs = songs,
                onBack = { navController.popBackStack() },
                mainViewModel = viewModel,navController, viewModel.hasNowPlayingBarAppeared.value
            )
        }
    }

}