package com.example.hardemusic

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.AudioManager
import android.net.Uri
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.app.NotificationCompat
import com.example.hardemusic.data.Song
import com.example.hardemusic.viewmodel.MainViewModel.ViewModelBridge

class NotificationHelper(
    private val context: Context,
    private val mediaSession: MediaSessionCompat
) {

    companion object {
        const val CHANNEL_ID = "media_playback_channel"
        const val NOTIFICATION_ID = 1
    }

    val contentIntent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }
    val pendingIntent = PendingIntent.getActivity(
        context,
        0,
        contentIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun showNotification(song: Song, isPlaying: Boolean) {
        val mediaStyle = androidx.media.app.NotificationCompat.MediaStyle()
            .setMediaSession(mediaSession.sessionToken)
            .setShowActionsInCompactView(0, 1, 2)

        val playPauseAction = if (isPlaying) {
            NotificationCompat.Action(
                R.drawable.ic_pause,
                "Pausar",
                createPendingIntent("ACTION_PAUSE")
            )
        } else {
            NotificationCompat.Action(
                R.drawable.ic_play,
                "Reproducir",
                createPendingIntent("ACTION_PLAY")
            )
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(song.title)
            .setContentText(song.artist)
            .setContentIntent(pendingIntent)
            .setSmallIcon(R.drawable.ic_musicnote)
            .setLargeIcon(loadAlbumArt(song.albumArtUri))
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOnlyAlertOnce(true)
            .setOngoing(isPlaying)
            .setStyle(mediaStyle)
            .addAction(
                NotificationCompat.Action(
                    R.drawable.ic_previous,
                    "Anterior",
                    createPendingIntent("ACTION_PREVIOUS")
                )
            )
            .addAction(playPauseAction)
            .addAction(
                NotificationCompat.Action(
                    R.drawable.ic_next,
                    "Siguiente",
                    createPendingIntent("ACTION_NEXT")
                )
            )
            .build()

        notificationManager.notify(NOTIFICATION_ID, builder)
    }

    fun cancelNotification() {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NOTIFICATION_ID)
    }

    private fun createPendingIntent(action: String): PendingIntent {
        val intent = Intent(context, MediaActionReceiver::class.java).apply {
            this.action = action
        }
        return PendingIntent.getBroadcast(
            context,
            action.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun loadAlbumArt(uri: Uri?): Bitmap? {
        return try {
            uri?.let {
                val inputStream = context.contentResolver.openInputStream(it)
                BitmapFactory.decodeStream(inputStream)
            }
        } catch (e: Exception) {
            null
        }
    }
}


class MediaActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        val viewModel = ViewModelBridge.mainViewModel ?: return

        when (action) {
            "ACTION_PREVIOUS" -> viewModel.playPrevious()
            "ACTION_PLAY", "ACTION_PAUSE" -> viewModel.togglePlayPause()
            "ACTION_NEXT" -> viewModel.playNext()
            "ACTION_CLOSE" -> {
                viewModel.togglePlayPause()
                val manager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                manager.cancel(NotificationHelper.NOTIFICATION_ID)
            }
        }
    }
}

class NoisyAudioReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action
        if (action == AudioManager.ACTION_AUDIO_BECOMING_NOISY) {
            val viewModel = ViewModelBridge.mainViewModel ?: return
            if (viewModel.isPlaying.value) {
                viewModel.togglePlayPause()
            }
        }
    }
}


