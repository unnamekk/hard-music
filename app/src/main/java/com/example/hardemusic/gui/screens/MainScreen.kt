package com.example.hardemusic.gui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.hardemusic.viewmodel.MainViewModel
import com.example.hardemusic.viewmodel.UserProfileViewModel
import androidx.compose.runtime.getValue
import androidx.compose.material3.Icon
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil.compose.rememberAsyncImagePainter
import com.example.hardemusic.gui.ScrollingText
import com.example.hardemusic.viewmodel.AlbumsViewModel

@Composable
fun MainScreen(navController: NavHostController, viewModel: MainViewModel,profileViewModel: UserProfileViewModel = viewModel(),
               albumsViewModel: AlbumsViewModel = viewModel()) {
    Column(
    )
    {
        WelcomeSection(profileViewModel)
        Spacer(modifier = Modifier.height(24.dp))
        MenuButtons(navController, viewModel)
        Spacer(modifier = Modifier.height(24.dp))
        SuggestionsSection(albumsViewModel, navController)
    }
}



@Composable
fun WelcomeSection(profileViewModel: UserProfileViewModel) {
    val name by profileViewModel.name.collectAsState()
    val imageUri by profileViewModel.imageUri.collectAsState()

    var editingName by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) profileViewModel.updateImage(uri)
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(Color.Yellow, shape = CircleShape)
                .clickable { imagePickerLauncher.launch("image/*") },
            contentAlignment = Alignment.Center
        ) {
            if (imageUri != null) {
                Image(
                    painter = rememberAsyncImagePainter(imageUri),
                    contentDescription = "Perfil",
                    modifier = Modifier.size(48.dp).clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text("👤", fontSize = 24.sp)
            }
        }

        Spacer(modifier = Modifier.width(12.dp))


        Row(
            verticalAlignment = Alignment.CenterVertically
        ){
            Column(
                modifier = Modifier.clickable { editingName = true }
            ) {
                Text("Bienvenido!", color = Color.White, fontSize = 16.sp)

                if (editingName) {
                    BasicTextField(
                        value = name,
                        onValueChange = { profileViewModel.updateName(it) },
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier
                            .background(Color.DarkGray, RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                } else {
                    Text(name, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }

            if (editingName) {
                Spacer(modifier = Modifier.width(8.dp))

                IconButton(onClick = { editingName = false }) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Guardar nombre",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun MenuButtons(navController: NavHostController, viewModel: MainViewModel) {
    val context = LocalContext.current
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            MenuButtonItem("Historial", Modifier.weight(1f).height(80.dp)) {
                navController.navigate("historial")
            }
            MenuButtonItem("Nuevas canciones", Modifier.weight(1f).height(80.dp)) {
                viewModel.loadRecentlyAddedSongs(context)
                navController.navigate("añadidos")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        MenuButtonItem(
            "Aleatorio",
            Modifier
                .fillMaxWidth()
                .height(80.dp)
        ) {
            viewModel.playRandomSong()
        }
    }
}

@Composable
fun MenuButtonItem(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .background(Color.DarkGray, shape = RoundedCornerShape(12.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun SuggestionsSection(
    albumsViewModel: AlbumsViewModel,
    navController: NavHostController
) {
    val albums by albumsViewModel.albums.collectAsState()
    val shuffled = remember(albums) { albums.shuffled().take(5) }

    Column {
        Text(
            text = "Sugerencias",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            shuffled.forEach { album ->
                Column(
                    modifier = Modifier
                        .width(120.dp)
                        .clickable {
                            navController.navigate("album_detail/${album.id}")
                        }
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(album.albumArtUri),
                        contentDescription = null,
                        modifier = Modifier
                            .size(120.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    ScrollingText(
                        text = album.name,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = album.artist,
                        color = Color.Gray,
                        fontSize = 12.sp,
                        maxLines = 1
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(72.dp))
    }
}
