package com.musicplayer.services

import android.app.*
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.musicplayer.MainActivity
import com.musicplayer.R
import com.musicplayer.models.Song

class MusicService : Service() {

    companion object {
        const val CHANNEL_ID = "music_player_channel"
        const val NOTIFICATION_ID = 1
        const val ACTION_PLAY_PAUSE = "com.musicplayer.PLAY_PAUSE"
        const val ACTION_NEXT = "com.musicplayer.NEXT"
        const val ACTION_PREV = "com.musicplayer.PREV"
    }

    private val binder = MusicBinder()
    private var mediaPlayer: MediaPlayer? = null
    private var currentSong: Song? = null
    private var songList: List<Song> = emptyList()
    private var currentIndex = 0
    var isPlaying = false
    var onSongChanged: ((Song) -> Unit)? = null
    var onPlayStateChanged: ((Boolean) -> Unit)? = null

    inner class MusicBinder : Binder() {
        fun getService(): MusicService = this@MusicService
    }

    override fun onBind(intent: Intent): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    fun setSongList(songs: List<Song>, startIndex: Int = 0) {
        songList = songs
        currentIndex = startIndex
        playSong(songs[startIndex])
    }

    fun playSong(song: Song) {
        mediaPlayer?.release()
        currentSong = song
        mediaPlayer = MediaPlayer().apply {
            setDataSource(song.uri)
            prepare()
            start()
            setOnCompletionListener { playNext() }
        }
        isPlaying = true
        onSongChanged?.invoke(song)
        onPlayStateChanged?.invoke(true)
        showNotification(song)
    }

    fun playPause() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
                isPlaying = false
            } else {
                it.start()
                isPlaying = true
            }
            onPlayStateChanged?.invoke(isPlaying)
            currentSong?.let { song -> showNotification(song) }
        }
    }

    fun playNext() {
        if (songList.isEmpty()) return
        currentIndex = (currentIndex + 1) % songList.size
        playSong(songList[currentIndex])
    }

    fun playPrevious() {
        if (songList.isEmpty()) return
        currentIndex = if (currentIndex - 1 < 0) songList.size - 1 else currentIndex - 1
        playSong(songList[currentIndex])
    }

    fun getCurrentPosition(): Int = mediaPlayer?.currentPosition ?: 0
    fun getDuration(): Int = mediaPlayer?.duration ?: 0
    fun seekTo(position: Int) = mediaPlayer?.seekTo(position)
    fun getCurrentSong() = currentSong

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Music Player",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Music playback controls"
                setShowBadge(false)
            }
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    private fun showNotification(song: Song) {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        val playPauseAction = NotificationCompat.Action(
            if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play,
            if (isPlaying) "Pause" else "Play",
            getPendingIntent(ACTION_PLAY_PAUSE)
        )
        val prevAction = NotificationCompat.Action(
            R.drawable.ic_previous, "Previous", getPendingIntent(ACTION_PREV)
        )
        val nextAction = NotificationCompat.Action(
            R.drawable.ic_next, "Next", getPendingIntent(ACTION_NEXT)
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(song.title)
            .setContentText(song.artist)
            .setSmallIcon(R.drawable.ic_music_note)
            .setContentIntent(pendingIntent)
            .addAction(prevAction)
            .addAction(playPauseAction)
            .addAction(nextAction)
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setShowActionsInCompactView(0, 1, 2)
            )
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(isPlaying)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    private fun getPendingIntent(action: String): PendingIntent {
        val intent = Intent(this, MusicService::class.java).apply { this.action = action }
        return PendingIntent.getService(this, action.hashCode(), intent, PendingIntent.FLAG_IMMUTABLE)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY_PAUSE -> playPause()
            ACTION_NEXT -> playNext()
            ACTION_PREV -> playPrevious()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        mediaPlayer?.release()
        super.onDestroy()
    }
}
