package com.example.hardemusic.gui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import com.example.hardemusic.gui.SongListView
import com.example.hardemusic.viewmodel.MainViewModel


@Composable
fun DailyHistoryScreen(viewModel: MainViewModel, navController: NavController, onBack: () -> Unit) {
    val historyToday by viewModel.historyToday.collectAsState()

    SongListView(
        songs = historyToday,
        onSongClick = {
            viewModel.playFrom(it)
            onBack()
        }, viewModel, navController,
        showTitle = "Historial de hoy"
    )
}
