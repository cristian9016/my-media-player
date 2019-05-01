package com.cristiandev.mymusicplayer

import android.app.Application
import com.cristiandev.mymusicplayer.data.Prefs

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        Prefs.init(this)
    }
}