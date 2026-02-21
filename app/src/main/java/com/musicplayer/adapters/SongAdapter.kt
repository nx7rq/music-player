package com.musicplayer.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.musicplayer.databinding.ItemSongBinding
import com.musicplayer.models.Song

class SongAdapter(
    private var songs: MutableList<Song>,
    private val onSongClick: (Song, Int) -> Unit,
    private val onLongClick: ((Song) -> Unit)? = null
) : RecyclerView.Adapter<SongAdapter.SongViewHolder>() {

    private var currentPlayingIndex = -1

    inner class SongViewHolder(val binding: ItemSongBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val binding = ItemSongBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SongViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = songs[position]
        with(holder.binding) {
            tvSongTitle.text = song.title
            tvArtist.text = song.artist
            tvDuration.text = song.durationFormatted()
            root.isActivated = position == currentPlayingIndex

            root.setOnClickListener { onSongClick(song, position) }
            root.setOnLongClickListener {
                onLongClick?.invoke(song)
                true
            }
        }
    }

    override fun getItemCount() = songs.size

    fun updateSongs(newSongs: List<Song>) {
        songs.clear()
        songs.addAll(newSongs)
        notifyDataSetChanged()
    }

    fun setCurrentPlaying(index: Int) {
        val old = currentPlayingIndex
        currentPlayingIndex = index
        if (old >= 0) notifyItemChanged(old)
        if (index >= 0) notifyItemChanged(index)
    }

    fun removeSong(song: Song) {
        val idx = songs.indexOf(song)
        if (idx >= 0) {
            songs.removeAt(idx)
            notifyItemRemoved(idx)
        }
    }
}
