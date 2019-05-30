package com.cristiandev.mymusicplayer.main

import android.arch.lifecycle.ViewModel
import android.content.ContentResolver
import com.cristiandev.mymusicplayer.data.model.Song
import com.cristiandev.mymusicplayer.service.MusicService
import com.cristiandev.mymusicplayer.util.applySchedulers
import io.reactivex.Observable

class MainViewModel : ViewModel() {

    fun getSongs(contentResolver: ContentResolver): Observable<MutableList<Song>> =
        Observable.create<MutableList<Song>> {
            val list = mutableListOf<Song>()
            val uri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            try {
                val cursor = contentResolver.query(uri, null, null, null, null)
                if (cursor != null && cursor.moveToFirst()) {
                    val titleColumn = cursor.getColumnIndex(android.provider.MediaStore.Audio.Media.TITLE)
                    val idColumn = cursor.getColumnIndex(android.provider.MediaStore.Audio.Media._ID)
                    val artistColumn = cursor.getColumnIndex(android.provider.MediaStore.Audio.Media.ARTIST)
                    val durationColumn = cursor.getColumnIndex(android.provider.MediaStore.Audio.Media.DURATION)
                    do {
                        val id = cursor.getLong(idColumn)
                        val title = cursor.getString(titleColumn)
                        val artist = cursor.getString(artistColumn)
                        val duration = cursor.getString(durationColumn)
                        if(duration!=null && duration.toLong() > 120000)
                            list.add(Song(id, title, artist, duration))
                    } while (cursor.moveToNext())
                }
                it.onNext(list)
            } catch (e: Exception) {
                it.onError(e)
            }
        }.applySchedulers()

    fun isServiceRunning(service: MusicService?): Observable<Boolean> =
        Observable.create<Boolean> {
            if (service != null)
                it.onNext(true)
            else it.onNext(false)
        }.applySchedulers()

}