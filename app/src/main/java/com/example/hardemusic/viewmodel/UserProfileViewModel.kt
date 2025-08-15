package com.example.hardemusic.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import com.example.hardemusic.data.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.example.hardemusic.data.UserProfileStorage
import com.example.hardemusic.data.AppText

class UserProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val storage = UserProfileStorage(application)

    private val _name = MutableStateFlow(AppText.namePlaceholder)
    val name: StateFlow<String> = _name.asStateFlow()

    private val _imageUri = MutableStateFlow<Uri?>(null)
    val imageUri: StateFlow<Uri?> = _imageUri.asStateFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        storage.loadUserProfile()?.let { profile ->
            _name.value = profile.name
            _imageUri.value = profile.imageUriString?.let { Uri.parse(it) }
        }
    }

    fun updateName(newName: String) {
        _name.value = newName
        saveProfile()
    }

    fun updateImage(uri: Uri) {
        _imageUri.value = uri
        saveProfile()
    }

    private fun saveProfile() {
        val profile = UserProfile(
            name = _name.value,
            imageUriString = _imageUri.value?.toString()
        )
        storage.saveUserProfile(profile)
    }
}