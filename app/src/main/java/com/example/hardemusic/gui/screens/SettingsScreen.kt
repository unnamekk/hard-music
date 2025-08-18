package com.example.hardemusic.gui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.hardemusic.viewmodel.AlbumsViewModel
import com.example.hardemusic.viewmodel.MainViewModel
import com.example.hardemusic.data.AppText


@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    navController: NavController,
    albumsViewModel: AlbumsViewModel
) {
    val context = LocalContext.current

    val currentLangCode by viewModel.language.collectAsState()
    val selectedLanguage = when(currentLangCode) {
        "Es" -> AppText.spanish
        "En" -> AppText.english
        else -> AppText.spanish
    }

    var expandedLanguage by remember { mutableStateOf(false) }
    val languages = listOf(AppText.spanish, AppText.english)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp)
    ) {
        Text(
            text = AppText.SettingsTitle,
            fontSize = 24.sp,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Text(AppText.languageLabel, color = Color.White, fontSize = 18.sp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Button(
                onClick = { expandedLanguage = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.DarkGray,
                    contentColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(selectedLanguage)
            }

            DropdownMenu(
                expanded = expandedLanguage,
                onDismissRequest = { expandedLanguage = false }
            ) {
                languages.forEach { lang ->
                    DropdownMenuItem(
                        text = { Text(lang) },
                        onClick = {
                            expandedLanguage = false

                            val langCode = when(lang) {
                                AppText.english -> "En"
                                AppText.spanish -> "Es"
                                else -> "Es"
                            }

                            AppText.language = langCode
                            viewModel.setLanguage(langCode)
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text(
                text = AppText.excludeAudios,
                color = Color.White,
                fontSize = 16.sp,
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = viewModel.excludeWhatsApp.collectAsState().value,
                onCheckedChange = { checked ->
                    viewModel.setExcludeWhatsApp(checked)
                    albumsViewModel.loadAlbums()
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.Cyan,
                    uncheckedThumbColor = Color.Gray
                )
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/unnamekk/hard-music"))
                context.startActivity(intent)
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF0D47A1),
                contentColor = Color.White
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Link,
                contentDescription = "GitHub",
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("GitHub")
        }
    }
}
