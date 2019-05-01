package com.cristiandev.mymusicplayer.data.adapter

import android.databinding.DataBindingUtil
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.cristiandev.mymusicplayer.R
import com.cristiandev.mymusicplayer.data.model.Song
import com.cristiandev.mymusicplayer.databinding.TemplateSongBinding
import com.cristiandev.mymusicplayer.util.inflate
import io.reactivex.subjects.PublishSubject
import java.util.*

class SongAdapter : RecyclerView.Adapter<SongAdapter.SongHolder>() {

    var data = mutableListOf<Song>()
        set(value) {
            field = value
            field.sortWith(Comparator { a, b ->
                a.title.compareTo(b.title)
            })
            notifyDataSetChanged()
        }
    val onClickSong = PublishSubject.create<Song>()

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): SongHolder =
        SongHolder(p0.inflate(R.layout.template_song))

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(p0: SongHolder, p1: Int) =
        p0.bind(data[p1], onClickSong)

    class SongHolder(view: View) : RecyclerView.ViewHolder(view) {
        val binding: TemplateSongBinding = DataBindingUtil.bind(view)!!
        fun bind(song: Song, onClickSong: PublishSubject<Song>) {
            binding.song = song
            binding.isPlaying = song.isPlaying
            binding.onClickSong = onClickSong
        }
    }
}