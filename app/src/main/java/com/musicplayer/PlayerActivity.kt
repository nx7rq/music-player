package com.musicplayer

import android.content.*
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.musicplayer.databinding.ActivityPlayerBinding
import com.musicplayer.services.MusicService

class PlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlayerBinding
    private var musicService: MusicService? = null
    private var isBound = false
    private val handler = Handler(Looper.getMainLooper())
    private val updateSeekBar = object : Runnable {
        override fun run() {
            musicService?.let {
                val pos = it.getCurrentPosition()
                val dur = it.getDuration()
                if (dur > 0) {
                    binding.seekBar.progress = (pos * 100 / dur)
                    binding.tvCurrentTime.text = formatTime(pos)
                    binding.tvTotalTime.text = formatTime(dur)
                }
            }
            handler.postDelayed(this, 500)
        }
    }

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            musicService = (service as MusicService.MusicBinder).getService()
            isBound = true
            updateUI()
            handler.post(updateSeekBar)

            musicService?.onSongChanged = { song ->
                runOnUiThread {
                    binding.tvSongTitle.text = song.title
                    binding.tvArtist.text = song.artist
                    binding.tvAlbum.text = song.album
                }
            }
            musicService?.onPlayStateChanged = { playing ->
                runOnUiThread {
                    binding.btnPlayPause.setImageResource(
                        if (playing) R.drawable.ic_pause else R.drawable.ic_play
                    )
                }
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Now Playing"

        setupControls()
        val intent = Intent(this, MusicService::class.java)
        bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    private fun setupControls() {
        binding.btnPlayPause.setOnClickListener { musicService?.playPause() }
        binding.btnNext.setOnClickListener { musicService?.playNext() }
        binding.btnPrevious.setOnClickListener { musicService?.playPrevious() }

        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    val duration = musicService?.getDuration() ?: 0
                    musicService?.seekTo(progress * duration / 100)
                }
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })
    }

    private fun updateUI() {
        musicService?.getCurrentSong()?.let { song ->
            binding.tvSongTitle.text = song.title
            binding.tvArtist.text = song.artist
            binding.tvAlbum.text = song.album
        }
        binding.btnPlayPause.setImageResource(
            if (musicService?.isPlaying == true) R.drawable.ic_pause else R.drawable.ic_play
        )
    }

    private fun formatTime(ms: Int): String {
        val seconds = ms / 1000
        return String.format("%d:%02d", seconds / 60, seconds % 60)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    override fun onDestroy() {
        handler.removeCallbacks(updateSeekBar)
        if (isBound) unbindService(connection)
        super.onDestroy()
    }
}
