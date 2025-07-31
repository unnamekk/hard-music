package com.example.hardemusic.data

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.hardemusic.viewmodel.PlaylistViewModel

class PlaylistViewModelFactory(
    private val app: Application
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PlaylistViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PlaylistViewModel(app) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}