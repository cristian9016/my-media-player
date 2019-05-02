package com.cristiandev.mymusicplayer.data

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences

object Prefs {
    private val SHUFFLE = "shuffle"
    private val LAST_SONG = "last_song"
    private val TYPE_OF_REPRODUCTION = "type"
    private lateinit var preference: SharedPreferences

    fun init(context: Context) {
        preference = context.getSharedPreferences("music_prefs", Activity.MODE_PRIVATE)
    }

    var shuffle: Boolean
        get() = preference.getBoolean(SHUFFLE, false)
        set(value) = preference.edit().putBoolean(SHUFFLE, value).apply()

    var lastSong: Int
        get() = preference.getInt(LAST_SONG, 0)
        set(value) = preference.edit().putInt(LAST_SONG, value).apply()

    var typeMusic: Boolean
        get() = preference.getBoolean(TYPE_OF_REPRODUCTION, true)
        set(value) = preference.edit().putBoolean(TYPE_OF_REPRODUCTION, value).apply()
}