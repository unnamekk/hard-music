package com.example.hardemusic.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

data class UserProfile(
    val name: String,
    val imageUriString: String?
)

class UserProfileStorage(private val context: Context) {
    private val gson = Gson()
    private val fileName = "user_profile.json"

    fun saveUserProfile(profile: UserProfile) {
        val json = gson.toJson(profile)
        val file = File(context.filesDir, fileName)
        file.writeText(json)
    }

    fun loadUserProfile(): UserProfile? {
        val file = File(context.filesDir, fileName)
        if (!file.exists()) return null
        val json = file.readText()
        return gson.fromJson(json, object : TypeToken<UserProfile>() {}.type)
    }
}