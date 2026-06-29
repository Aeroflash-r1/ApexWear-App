package com.echostream.player

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import android.os.PowerManager.WakeLock
import androidx.annotation.OptIn
import androidx.core.app.NotificationCompat
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

@OptIn(UnstableApi::class)
class MusicPlaybackService : MediaSessionService() {

    private lateinit var player: ExoPlayer
    private lateinit var mediaSession: MediaSession
    private var wakeLock: WakeLock? = null

    companion object {
        private const val TAG = "MusicPlaybackService"
        private const val NOTIFICATION_CHANNEL_ID = "echostream_playback"
        private const val NOTIFICATION_ID = 1001
        private const val USER_AGENT = "Mozilla/5.0 (Linux; Android 14; Wear OS) AppleWebKit/537.36 Chrome/120 Mobile Safari/537.36 EchoStream/1.0"
        private const val WAKELOCK_TIMEOUT_MS = 10 * 60 * 1000L // 10 minutes
    }

    override fun onCreate() {
        super.onCreate()

        createNotificationChannel()

        // Build ExoPlayer with audio focus
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .build()

        val httpClient = OkHttpClient.Builder()
            .followRedirects(true)
            .followSslRedirects(true)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
        val dataSourceFactory = OkHttpDataSource.Factory(httpClient)
            .setDefaultRequestProperties(defaultPlaybackHeaders())

        player = ExoPlayer.Builder(this)
            .setMediaSourceFactory(DefaultMediaSourceFactory(dataSourceFactory))
            .setAudioAttributes(audioAttributes, true)
            .setHandleAudioBecomingNoisy(true)
            .setWakeMode(C.WAKE_MODE_LOCAL)
            .build()

        // Add listener to manage wake lock based on playback state
        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_READY, Player.STATE_BUFFERING -> {
                        if (player.playWhenReady) {
                            acquireWakeLock()
                        }
                    }
                    Player.STATE_ENDED, Player.STATE_IDLE -> {
                        releaseWakeLock()
                    }
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (isPlaying) {
                    acquireWakeLock()
                } else {
                    releaseWakeLock()
                }
            }
        })

        mediaSession = MediaSession.Builder(this, player)
            .setCallback(EchoStreamSessionCallback())
            .build()

        // WakeLock to keep CPU running during playback
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "EchoStream::PlaybackWakeLock"
        ).apply {
            setReferenceCounted(false)
        }
    }

    private fun acquireWakeLock() {
        wakeLock?.let { lock ->
            if (!lock.isHeld) {
                lock.acquire(WAKELOCK_TIMEOUT_MS)
            }
        }
    }

    private fun releaseWakeLock() {
        wakeLock?.let { lock ->
            if (lock.isHeld) {
                lock.release()
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Music Playback",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "EchoStream music playback controls"
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession = mediaSession

    override fun onTaskRemoved(rootIntent: Intent?) {
        if (!player.playWhenReady || player.mediaItemCount == 0) {
            stopSelf()
        }
    }

    override fun onDestroy() {
        // MUST release in this exact order
        mediaSession.release()
        player.stop()
        player.release()
        releaseWakeLock()
        super.onDestroy()
    }

    private fun defaultPlaybackHeaders(): Map<String, String> = mapOf(
        "User-Agent" to USER_AGENT,
        "Accept" to "audio/webm,audio/mp4,video/webm,video/mp4,application/x-mpegurl,*/*",
        "Accept-Language" to "en-US,en;q=0.9",
        "Referer" to "https://www.youtube.com/"
    )

    private inner class EchoStreamSessionCallback : MediaSession.Callback {
        override fun onAddMediaItems(
            mediaSession: MediaSession,
            controller: MediaSession.ControllerInfo,
            mediaItems: List<MediaItem>
        ): ListenableFuture<List<MediaItem>> = Futures.immediateFuture(mediaItems)

        @OptIn(UnstableApi::class)
        override fun onPlaybackResumption(
            mediaSession: MediaSession,
            controller: MediaSession.ControllerInfo
        ): ListenableFuture<MediaSession.MediaItemsWithStartPosition> {
            val currentItem = player.currentMediaItem
                ?: return Futures.immediateFailedFuture(IllegalStateException("No track available to resume"))

            return Futures.immediateFuture(
                MediaSession.MediaItemsWithStartPosition(
                    listOf(currentItem),
                    0,
                    player.currentPosition.coerceAtLeast(0L)
                )
            )
        }
    }
}
