package com.cristiandev.mymusicplayer.video

import android.Manifest
import android.annotation.SuppressLint
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.support.constraint.ConstraintSet
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.view.WindowManager
import com.cristiandev.mymusicplayer.R
import com.cristiandev.mymusicplayer.data.Prefs
import com.cristiandev.mymusicplayer.data.adapter.VideoAdapter
import com.cristiandev.mymusicplayer.databinding.ActivityVideoBinding
import com.cristiandev.mymusicplayer.main.MainActivity
import com.cristiandev.mymusicplayer.util.LifeDisposable
import com.cristiandev.mymusicplayer.video.VideoFragment.Companion.pauseVideo
import com.cristiandev.mymusicplayer.video.VideoFragment.Companion.setVideo
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.rxkotlin.subscribeBy
import kotlinx.android.synthetic.main.activity_video.*
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast
import rx.subjects.PublishSubject

class VideoActivity : AppCompatActivity() {

    private val viewModel: VideoViewModel by lazy { ViewModelProviders.of(this).get(VideoViewModel::class.java) }
    private val dis = LifeDisposable(this)
    val videoAdapter = VideoAdapter()
    private val permissions = RxPermissions(this)
    lateinit var binding: ActivityVideoBinding
    lateinit var lm: LinearLayoutManager
    var paused = false
    var position = 0

    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_video)
        videoList.adapter = videoAdapter
        lm = LinearLayoutManager(this)
        videoList.layoutManager = lm
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        supportFragmentManager
            .beginTransaction()
            .hide(fragmentVideo)
            .commit()
        permissions.request(Manifest.permission.READ_EXTERNAL_STORAGE)
            .subscribe { allow ->
                if (allow) {
                    dis add viewModel.getVideos(contentResolver)
                        .subscribeBy(
                            onNext = {
                                videoAdapter.data = it
                            },
                            onError = {
                                toast(it.message!!)
                                it.printStackTrace()
                            }
                        )
                }
            }
        etSearch.setOnClickListener {

        }
        swMusic.setOnClickListener {
            startActivity<MainActivity>()
            Prefs.typeMusic = true
            finish()
        }
        videoAdapter.onClickVideo
            .subscribe {
                position = videoAdapter.data.indexOf(it)
                paused = false
                setVideo.onNext(it.path)
                btnPlay.setImageDrawable(getDrawable(R.drawable.btn_stop))
            }
        btnPlay.setOnClickListener {
            if (paused) {
                pauseVideo.onNext(false)
                btnPlay.setImageDrawable(getDrawable(R.drawable.btn_stop))
            }
            else {
                pauseVideo.onNext(true)
                btnPlay.setImageDrawable(getDrawable(R.drawable.btn_play))
            }
            paused = !paused
        }
        btnNext.setOnClickListener {
            position++
            if (position == videoAdapter.data.size) position = 0
            setVideo.onNext(videoAdapter.data[position].path)
            paused = false
        }
        btnPrevious.setOnClickListener {
            position--
            if (position < 0) position = videoAdapter.data.size - 1
            setVideo.onNext(videoAdapter.data[position].path)
            paused = false
        }
    }

    override fun onResume() {
        super.onResume()
        showPlayer.subscribe ({
            if (it) supportFragmentManager
                .beginTransaction()
                .show(fragmentVideo)
                .commit()
            else supportFragmentManager
                .beginTransaction()
                .hide(fragmentVideo)
                .commit()
        },{
            it.printStackTrace()
        })
    }

    companion object {
        val showPlayer = PublishSubject.create<Boolean>()
    }
}
