package com.example.hardemusic.gui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import com.example.hardemusic.data.AppText
import com.example.hardemusic.gui.SongListView
import com.example.hardemusic.viewmodel.MainViewModel

@Composable
fun RecentlyAddedScreen(viewModel: MainViewModel,navController: NavController, onBack: () -> Unit) {
    val songs by viewModel.recentSongs.collectAsState()

    SongListView(
        songs = songs,
        onSongClick = {
            viewModel.playFrom(it)
            onBack()
        },viewModel, navController,
        showTitle = AppText.newSongsTitle
    )
}