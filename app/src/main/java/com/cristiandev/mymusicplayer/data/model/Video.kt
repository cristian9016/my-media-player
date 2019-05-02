package com.cristiandev.mymusicplayer.data.model

class Video(
    var id: Long,
    var title: String,
    var duration: String,
    var isPlaying: Boolean = false,
    var path:String) {
    init {
        val mil = duration.toInt()
        val minutes = (mil / 1000) / 60
        val seconds = (mil / 1000) % 60
        val sec = if (seconds.toString().length == 1) "0$seconds"
        else "$seconds"
        duration = "$minutes:$sec"
    }
}