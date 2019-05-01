package com.cristiandev.mymusicplayer.util

import android.databinding.BindingAdapter
import android.widget.TextView

@BindingAdapter("app:millisToTime")
fun millisToTime(tv: TextView, millis: String) {
    val mil = millis.toInt()
    val minutes = (mil / 1000) / 60
    val seconds = (mil / 1000) % 60
    tv.text = "$minutes:$seconds"
}