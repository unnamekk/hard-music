package com.example.hardemusic.gui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.example.hardemusic.data.AppText
import com.example.hardemusic.data.Playlist

@Composable
fun PlaylistsScreen(
    playlists: List<Playlist>,
    onCreatePlaylist: (String, String?) -> Unit,
    onPlaylistClick: (Playlist) -> Unit,
    onEditPlaylist: (String, String?, Playlist) -> Unit,
    onDeletePlaylist: (Playlist) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var playlistName by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        if (playlists.isEmpty()) {

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF121212)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    IconButton(
                        onClick = { showDialog = true },
                        modifier = Modifier
                            .size(72.dp)
                            .background(Color(0xFF58A6FF), CircleShape)
                            .border(2.dp, Color.White, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Crear Playlist",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = AppText.noPlaylistsTitle,
                        color = Color.Gray,
                        fontSize = 16.sp
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF121212))
                    .padding(16.dp)
            ) {


                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(playlists) { playlist ->
                        PlaylistItem(
                            playlist = playlist,
                            onClick = { onPlaylistClick(playlist) },
                            onEdit = { newName, newImageUri, originalPlaylist ->
                                onEditPlaylist(newName, newImageUri, originalPlaylist)
                            },
                            onDelete = { playlist ->
                                onDeletePlaylist(playlist)
                            }
                        )
                    }
                }
            }

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.BottomEnd
            ) {
                FloatingActionButton(
                    onClick = { showDialog = true },
                    containerColor = Color(0xFF58A6FF),
                    contentColor = Color.White,
                    modifier = Modifier
                        .padding(16.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Crear Playlist")
                }
            }
        }

        if (showDialog) {
            var imageUri by remember { mutableStateOf<Uri?>(null) }
            val imagePickerLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.GetContent()
            ) { uri: Uri? ->
                imageUri = uri
            }

            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text(AppText.newPlaylistTitle) },
                text = {
                    Column {
                        TextField(
                            value = playlistName,
                            onValueChange = { playlistName = it },
                            placeholder = { Text(AppText.namePlaylistPlaceholder) },
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                disabledTextColor = Color.Gray,
                                focusedContainerColor = Color.DarkGray,
                                unfocusedContainerColor = Color.DarkGray,
                                disabledContainerColor = Color.LightGray,
                                focusedPlaceholderColor = Color.LightGray,
                                unfocusedPlaceholderColor = Color.LightGray,
                                disabledPlaceholderColor = Color.Gray,
                                cursorColor = Color.White,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent,
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Button(onClick = {
                                imagePickerLauncher.launch("image/*")
                            }) {
                                Text(AppText.selectImageButton)
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            imageUri?.let {
                                AsyncImage(
                                    model = it,
                                    contentDescription = "Imagen seleccionada",
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(6.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (playlistName.isNotBlank()) {
                                onCreatePlaylist(playlistName.trim(), imageUri?.toString())
                                playlistName = ""
                                showDialog = false
                            }
                        }
                    ) {
                        Text(AppText.createButton)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            playlistName = ""
                            showDialog = false
                        }
                    ) {
                        Text(AppText.cancelButton)
                    }
                },
                containerColor = Color(0xFF2C2C2C)
            )
        }
    }
}

@Composable
fun PlaylistItem(
    playlist: Playlist,
    onClick: () -> Unit,
    onEdit: (String, String?, Playlist) -> Unit,
    onDelete: (Playlist) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color.DarkGray)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            playlist.imageUri?.let { uriStr ->
                val painter = rememberAsyncImagePainter(model = Uri.parse(uriStr))
                Image(
                    painter = painter,
                    contentDescription = null,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = playlist.name,
                    fontSize = 18.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${playlist.songs.size} "+ AppText.songsTitle,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            Box {
                IconButton(onClick = { expanded = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Opciones", tint = Color.White)
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(AppText.editPlaylistOption) },
                        onClick = {
                            expanded = false
                            showEditDialog = true
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(AppText.deleteComPlaylistOption) },
                        onClick = {
                            expanded = false
                            onDelete(playlist)
                        }
                    )
                }
            }
        }
    }

    if (showEditDialog) {
        EditPlaylistDialog(
            initialName = playlist.name,
            initialImageUri = playlist.imageUri,
            onConfirm = { name, imageUri ->
                onEdit(name, imageUri, playlist)
                showEditDialog = false
            },
            onDismiss = { showEditDialog = false }
        )
    }
}

@Composable
fun EditPlaylistDialog(
    initialName: String,
    initialImageUri: String?,
    onConfirm: (String, String?) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var imageUri by remember { mutableStateOf(initialImageUri) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            imageUri = it.toString()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(AppText.editPlaylistOption) },
        text = {
            Column {
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = { Text(AppText.namePlaylistPlaceholder) },
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { imagePicker.launch("image/*") }) {
                    Text(AppText.selectImageButton)
                }

                imageUri?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    val painter = rememberAsyncImagePainter(model = Uri.parse(it))
                    Image(
                        painter = painter,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(name.trim(), imageUri) }) {
                Text(AppText.saveButton)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(AppText.cancelButton)
            }
        }
    )
}
