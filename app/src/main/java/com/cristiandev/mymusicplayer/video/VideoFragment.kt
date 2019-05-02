package com.cristiandev.mymusicplayer.video

import android.databinding.DataBindingUtil
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cristiandev.mymusicplayer.R
import com.cristiandev.mymusicplayer.databinding.FragmentVideoBinding
import com.cristiandev.mymusicplayer.video.VideoActivity.Companion.showPlayer
import rx.subjects.PublishSubject

class VideoFragment : Fragment() {

    lateinit var binding: FragmentVideoBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_video, null, false)

        val onCompletion = MediaPlayer.OnCompletionListener { mp ->
            mp!!.reset()
            binding.videoView.stopPlayback()
            showPlayer.onNext(false)
        }

        val onPrepared = MediaPlayer.OnPreparedListener {
            it.start()
        }
        binding.videoView.setOnPreparedListener(onPrepared)
        binding.videoView.setOnCompletionListener(onCompletion)

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        setVideo.subscribe({
            showPlayer.onNext(true)
            binding.videoView.setVideoURI(Uri.parse(it))
        }, {
            it.printStackTrace()
        })
        pauseVideo.subscribe(
            {
                if (it) {
                    binding.videoView.pause()
                    showPlayer.onNext(false)
                } else {
                    binding.videoView.resume()
                    showPlayer.onNext(true)
                }
            }, {
                it.printStackTrace()
            }
        )
    }

    companion object {
        val setVideo = PublishSubject.create<String>()
        val pauseVideo = PublishSubject.create<Boolean>()
    }

}