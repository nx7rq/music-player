package com.musicplayer.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Song(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val duration: Long,
    val uri: String,
    val albumArtUri: String? = null
) : Parcelable {
    fun durationFormatted(): String {
        val minutes = (duration / 1000) / 60
        val seconds = (duration / 1000) % 60
        return String.format("%d:%02d", minutes, seconds)
    }
}

data class Playlist(
    val id: Long,
    val name: String,
    val songs: MutableList<Song> = mutableListOf()
)
