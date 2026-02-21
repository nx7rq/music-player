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
                Toast.makeText(this, "Added \"$songTitle\" to ${playlist.name}", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun showCreatePlaylistDialog() {
        val input = EditText(this).apply { hint = "Playlist name" }
        AlertDialog.Builder(this)
            .setTitle("New Playlist")
            .setView(input)
            .setPositiveButton("Create") { _, _ ->
                val name = input.text.toString().trim()
                if (name.isNotEmpty()) {
                    playlists.add(Playlist(System.currentTimeMillis(), name))
                    refreshList()
                } else {
                    Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}

class PlaylistAdapter(
    private val playlists: List<Playlist>,
    private val onClick: (Playlist) -> Unit
) : androidx.recyclerview.widget.RecyclerView.Adapter<PlaylistAdapter.VH>() {

    inner class VH(val view: android.view.View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): VH {
        val tv = android.widget.TextView(parent.context).apply {
            setPadding(48, 36, 48, 36)
            textSize = 16f
        }
        return VH(tv)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val pl = playlists[position]
        (holder.view as android.widget.TextView).text = "🎵  ${pl.name}  (${pl.songs.size} songs)"
        holder.view.setOnClickListener { onClick(pl) }
    }

    override fun getItemCount() = playlists.size
}
