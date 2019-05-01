package com.cristiandev.mymusicplayer.data.model

class Song(
    var id: Long,
    var title: String,
    var artist: String,
    var duration: String,
    var isPlaying: Boolean = false
) {
    init {
        val mil = duration.toInt()
        val minutes = (mil / 1000) / 60
        val seconds = (mil / 1000) % 60
        val sec = if (seconds.toString().length == 1) "0$seconds"
        else "$seconds"
        duration = "$minutes:$sec"
    }
}