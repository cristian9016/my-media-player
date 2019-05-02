package com.cristiandev.mymusicplayer.data.adapter

import android.databinding.DataBindingUtil
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.cristiandev.mymusicplayer.R
import com.cristiandev.mymusicplayer.data.model.Video
import com.cristiandev.mymusicplayer.databinding.TemplateVideoBinding
import com.cristiandev.mymusicplayer.util.inflate
import io.reactivex.subjects.PublishSubject
import java.util.*

class VideoAdapter : RecyclerView.Adapter<VideoAdapter.VideoHolder>() {

    var data = mutableListOf<Video>()
        set(value) {
            field = value
            field.sortWith(Comparator { a, b ->
                a.title.compareTo(b.title)
            })
            notifyDataSetChanged()
        }
    val onClickVideo = PublishSubject.create<Video>()

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): VideoHolder =
        VideoHolder(p0.inflate(R.layout.template_video))

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(p0: VideoHolder, p1: Int) =
        p0.bind(data[p1], onClickVideo)

    class VideoHolder(view: View) : RecyclerView.ViewHolder(view) {
        val binding: TemplateVideoBinding = DataBindingUtil.bind(view)!!
        fun bind(video: Video, onClickVideo: PublishSubject<Video>) {
            binding.video = video
            binding.isPlaying = video.isPlaying
            binding.onClickVideo = onClickVideo
        }
    }
}