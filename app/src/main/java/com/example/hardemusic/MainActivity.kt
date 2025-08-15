package com.example.hardemusic

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.support.v4.media.session.MediaSessionCompat
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.hardemusic.NotificationHelper.Companion.CHANNEL_ID
import com.example.hardemusic.data.AppText
import com.example.hardemusic.data.Song
import com.example.hardemusic.gui.MainLayout
import com.example.hardemusic.ui.theme.HardeMusicTheme
import com.example.hardemusic.viewmodel.MainViewModel
import com.example.hardemusic.viewmodel.MainViewModel.ViewModelBridge


@Suppress("DEPRECATION")
class MainActivity : ComponentActivity() {
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>

    private lateinit var deleteSongLauncher: ActivityResultLauncher<IntentSenderRequest>
    private var pendingDeleteSong: Song? = null

    private lateinit var viewModel: MainViewModel

    private var navControllerRef: NavController? = null

    lateinit var notificationHelper: NotificationHelper

    private lateinit var noisyReceiver: NoisyAudioReceiver

    private lateinit var editSongLauncher: ActivityResultLauncher<IntentSenderRequest>
    private var pendingEditedSong: Song? = null


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("UnspecifiedRegisterReceiverFlag", "LocalContextConfigurationRead")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        createNotificationChannel(this)

        val mediaSession = MediaSessionCompat(this, "HardeMusicSession")
        mediaSession.setFlags(
            MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or
                    MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
        )
        mediaSession.isActive = true
        notificationHelper = NotificationHelper(this, mediaSession)

        mediaSession.setCallback(object : MediaSessionCompat.Callback() {
            override fun onPlay() {
                ViewModelBridge.mainViewModel?.togglePlayPause()
            }

            override fun onPause() {
                ViewModelBridge.mainViewModel?.togglePlayPause()
            }

            override fun onSkipToNext() {
                ViewModelBridge.mainViewModel?.playNext()
            }

            override fun onSkipToPrevious() {
                ViewModelBridge.mainViewModel?.playPrevious()
            }
        })


        val filter = IntentFilter().apply {
            addAction("com.example.ACTION_PLAY")
            addAction("com.example.ACTION_PAUSE")
            addAction("com.example.ACTION_NEXT")
            addAction("com.example.ACTION_PREVIOUS")
            addAction("com.example.ACTION_CLOSE")
        }
        registerReceiver(MediaActionReceiver(), filter, RECEIVER_NOT_EXPORTED)

        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        ViewModelBridge.mainViewModel = viewModel
        ViewModelBridge.notificationHelper = notificationHelper
        ViewModelBridge.mediaSession = mediaSession

        permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val allGranted = permissions.values.all { it }

            if (allGranted) {
                showCustomToast(this, AppText.grantedPermissionsToast, true)

                Handler(Looper.getMainLooper()).postDelayed({
                    val intent = Intent(this, MainActivity::class.java)
                    finish()
                    startActivity(intent)
                }, 1000)

            } else {
                showCustomToast(this, AppText.deniedPermissionsToast, false)

                Handler(Looper.getMainLooper()).postDelayed({
                    finishAffinity()
                }, 1500)
            }

        }


        checkPermissionsAndRequest()

        setContent {
            HardeMusicTheme {
                val navController = rememberNavController()
                navControllerRef = navController

                MainLayout(
                    viewModel = viewModel,
                    navController = navController
                )
            }
        }


        deleteSongLauncher =
            registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    showCustomToast(this, AppText.songDeletedToast, true)
                    pendingDeleteSong?.let {
                        navControllerRef?.navigate("home") {
                            popUpTo(navControllerRef!!.graph.startDestinationId) {
                                inclusive = true
                            }
                        }
                    }
                } else {
                    showCustomToast(this, AppText.songNotDeletedToast, false)
                }
                pendingDeleteSong = null
            }

        editSongLauncher =
            registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    pendingEditedSong?.let { song ->
                        viewModel.updateSong(this, song)
                        navControllerRef?.navigate("calendar") {
                            popUpTo(navControllerRef!!.graph.startDestinationId) {
                                inclusive = true
                            }
                        }
                    }
                } else {
                    showCustomToast(this, AppText.notEditionToast, false)
                }
                pendingEditedSong = null
            }

        noisyReceiver = NoisyAudioReceiver()
        val noisyFilter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
        registerReceiver(noisyReceiver, noisyFilter)

    }

    override fun onDestroy() {
        super.onDestroy()
        notificationHelper.cancelNotification()
        if (::noisyReceiver.isInitialized) {
            unregisterReceiver(noisyReceiver)
        }
    }

    override fun onStop() {
        super.onStop()

        val isPlaying = viewModel.isPlaying.value
        if (!isPlaying) {
            viewModel.currentSong.value?.let {
                notificationHelper.showNotification(it, isPlaying = false)
            }
            notificationHelper.cancelNotification()
        }
    }

    private fun checkPermissionsAndRequest() {
        val permissions = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_MEDIA_AUDIO
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissions.add(Manifest.permission.READ_MEDIA_AUDIO)
            }
        } else {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    1001
                )
            }
        }

        if (permissions.isNotEmpty()) {
            permissionLauncher.launch(permissions.toTypedArray())
        }
    }

    fun showCustomToast(context: Context, message: String, isSuccess: Boolean) {
        val inflater = LayoutInflater.from(context)
        val layout = inflater.inflate(R.layout.custom_toast, null)

        val iconView = layout.findViewById<ImageView>(R.id.toast_icon)
        val textView = layout.findViewById<TextView>(R.id.toast_text)

        textView.text = message

        val iconRes = if (isSuccess) R.drawable.ic_check else R.drawable.ic_error
        iconView.setImageResource(iconRes)

        val toast = Toast(context)
        toast.duration = Toast.LENGTH_SHORT
        toast.view = layout
        toast.show()
    }

    @RequiresApi(Build.VERSION_CODES.R)
    fun requestEditPermission(song: Song) {
        try {
            val uri = song.uri
            val pendingIntent = MediaStore.createWriteRequest(contentResolver, listOf(uri))
            val request = IntentSenderRequest.Builder(pendingIntent.intentSender).build()
            pendingEditedSong = song
            editSongLauncher.launch(request)
        } catch (e: Exception) {
            e.printStackTrace()
            showCustomToast(this, AppText.errorPermissionToast, false)
        }
    }

    var pendingEditedSongs: List<Song>? = null

    @RequiresApi(Build.VERSION_CODES.R)
    val editMultipleSongsLauncher =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                pendingEditedSongs?.let { songs ->
                    viewModel.updateSongs(this, songs)
                }
            } else {
                showCustomToast(this, AppText.notEditionSongsToast, false)
            }
            pendingEditedSongs = null
        }

    @RequiresApi(Build.VERSION_CODES.R)
    fun deleteSongAndRefresh(song: Song) {
        try {
            val uri = song.uri
            val pendingIntent = MediaStore.createDeleteRequest(contentResolver, listOf(uri))
            val request = IntentSenderRequest.Builder(pendingIntent.intentSender).build()
            pendingDeleteSong = song
            deleteSongLauncher.launch(request)
        } catch (e: Exception) {
            e.printStackTrace()
            showCustomToast(this, AppText.songNotDeletedToast, false)
        }
    }

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Reproductor",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Controles del reproductor"
            }

            val manager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
}




