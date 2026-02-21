package com.musicplayer

import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.musicplayer.databinding.ActivityPlaylistBinding
import com.musicplayer.models.Playlist

class PlaylistActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlaylistBinding
    private val playlists = mutableListOf<Playlist>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlaylistBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Playlists"

        binding.fabNewPlaylist.setOnClickListener { showCreatePlaylistDialog() }
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        binding.rvPlaylists.layoutManager = LinearLayoutManager(this)
        refreshList()
    }

    private fun refreshList() {
        binding.rvPlaylists.adapter = PlaylistAdapter(playlists) { playlist ->
            val songTitle = intent.getStringExtra("songTitle")
            if (songTitle != null) {
                Toast.makeText(this, "Added \"$songTitle\" to ${playl
