package com.cristiandev.mymusicplayer.video

import android.arch.lifecycle.ViewModel
import android.content.ContentResolver
import com.cristiandev.mymusicplayer.data.model.Song
import com.cristiandev.mymusicplayer.data.model.Video
import com.cristiandev.mymusicplayer.service.MusicService
import com.cristiandev.mymusicplayer.util.applySchedulers
import io.reactivex.Observable

class VideoViewModel : ViewModel() {


    fun getVideos(contentResolver: ContentResolver): Observable<MutableList<Video>> =
        Observable.create<MutableList<Video>> {
            val list = mutableListOf<Video>()
            val uri = android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            try {
                val cursor = contentResolver.query(uri, null, null, null, null)
                if (cursor != null && cursor.moveToFirst()) {
                    val titleColumn = cursor.getColumnIndex(android.provider.MediaStore.Video.Media.TITLE)
                    val idColumn = cursor.getColumnIndex(android.provider.MediaStore.Video.Media._ID)
                    val durationColumn = cursor.getColumnIndex(android.provider.MediaStore.Video.Media.DURATION)
                    val path = cursor.getColumnIndex(android.provider.MediaStore.Video.Media.DATA)
                    do {
                        val id = cursor.getLong(idColumn)
                        val title = cursor.getString(titleColumn)
                        val duration = cursor.getString(durationColumn)
                        val path = cursor.getString(path)

                        list.add(Video(id, title, duration, path = path))
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