package com.musicplayer

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.musicplayer.adapters.SongAdapter
import com.musicplayer.databinding.ActivityMainBinding
import com.musicplayer.models.Song
import com.musicplayer.services.MusicService

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: SongAdapter
    private val songs = mutableListOf<Song>()
    private var musicService: MusicService? = null
    private var isBound = false

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as MusicService.MusicBinder
            musicService = binder.getService()
            isBound = true
            musicService?.onSongChanged = { song ->
                runOnUiThread {
                    val idx = songs.indexOfFirst { it.id == song.id }
                    adapter.setCurrentPlaying(idx)
                    binding.miniPlayer.root.visibility = android.view.View.VISIBLE
                    binding.miniPlayer.tvMiniTitle.text = song.title
                    binding.miniPlayer.tvMiniArtist.text = song.artist
                }
            }
            musicService?.onPlayStateChanged = { playing ->
                runOnUiThread {
                    binding.miniPlayer.btnMiniPlayPause.setImageResource(
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
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        setupRecyclerView()
        setupMiniPlayer()
        requestPermissions()
    }

    private fun setupRecyclerView() {
        adapter = SongAdapter(
            songs,
            onSongClick = { song, index ->
                startMusicService()
                musicService?.setSongList(songs, index)
                val intent = Intent(this, PlayerActivity::class.java)
                startActivity(intent)
            },
            onLongClick = { song ->
                showSongOptions(song)
            }
        )
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }

    private fun setupMiniPlayer() {
        binding.miniPlayer.root.visibility = android.view.View.GONE
        binding.miniPlayer.btnMiniPlayPause.setOnClickListener {
            musicService?.playPause()
        }
        binding.miniPlayer.btnMiniNext.setOnClickListener {
            musicService?.playNext()
        }
        binding.miniPlayer.root.setOnClickListener {
            startActivity(Intent(this, PlayerActivity::class.java))
        }
    }

    private fun showSongOptions(song: Song) {
        val options = arrayOf("Add to Playlist", "Remove from Library")
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(song.title)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> openPlaylistPicker(song)
                    1 -> {
                        adapter.removeSong(song)
                        songs.remove(song)
                        Toast.makeText(this, "Removed from library", Toast.LENGTH_SHORT).show()
                    }
                }
            }.show()
    }

    private fun openPlaylistPicker(song: Song) {
        val intent = Intent(this, PlaylistActivity::class.java)
        intent.putExtra("songId", song.id)
        intent.putExtra("songTitle", song.title)
        startActivity(intent)
    }

    private fun startMusicService() {
        val intent = Intent(this, MusicService::class.java)
        startForegroundService(intent)
        if (!isBound) bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    private fun loadSongs() {
        songs.clear()
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA
        )
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val cursor: Cursor? = contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection, selection, null,
            "${MediaStore.Audio.Media.TITLE} ASC"
        )
        cursor?.use {
            val idCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val durationCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val dataCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)

            while (it.moveToNext()) {
                songs.add(
                    Song(
                        id = it.getLong(idCol),
                        title = it.getString(titleCol) ?: "Unknown",
                        artist = it.getString(artistCol) ?: "Unknown Artist",
                        album = it.getString(albumCol) ?: "Unknown Album",
                        duration = it.getLong(durationCol),
                        uri = it.getString(dataCol)
                    )
                )
            }
        }
        adapter.updateSongs(songs)
        binding.tvEmpty.visibility = if (songs.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
    }

    private fun requestPermissions() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            Manifest.permission.READ_MEDIA_AUDIO
        else
            Manifest.permission.READ_EXTERNAL_STORAGE

        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            loadSongs()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(permission), 100)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 && grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
            loadSongs()
        } else {
            Toast.makeText(this, "Storage permission required to load music", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.add(0, 1, 0, "Dark Mode")
        menu.add(0, 2, 0, "Playlists")
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            1 -> {
                val current = AppCompatDelegate.getDefaultNightMode()
                if (current == AppCompatDelegate.MODE_NIGHT_YES) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                }
                true
            }
            2 -> {
                startActivity(Intent(this, PlaylistActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onStart() {
        super.onStart()
        startMusicService()
    }

    override fun onDestroy() {
        if (isBound) unbindService(connection)
        super.onDestroy()
    }
}
