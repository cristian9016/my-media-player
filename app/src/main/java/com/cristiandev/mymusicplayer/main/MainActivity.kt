package com.cristiandev.mymusicplayer.main

import android.Manifest
import android.annotation.SuppressLint
import android.arch.lifecycle.ViewModelProviders
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.databinding.DataBindingUtil
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.support.v4.view.GestureDetectorCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.view.WindowManager
import com.cristiandev.mymusicplayer.R
import com.cristiandev.mymusicplayer.data.Prefs
import com.cristiandev.mymusicplayer.data.adapter.SongAdapter
import com.cristiandev.mymusicplayer.data.adapter.SwipeGestureListener
import com.cristiandev.mymusicplayer.databinding.ActivityMainBinding
import com.cristiandev.mymusicplayer.service.MusicService
import com.cristiandev.mymusicplayer.service.StartAppService
import com.cristiandev.mymusicplayer.util.LifeDisposable
import com.cristiandev.mymusicplayer.util.gone
import com.cristiandev.mymusicplayer.util.visible
import com.jakewharton.rxbinding.widget.RxTextView
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.Observable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.toast

class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by lazy { ViewModelProviders.of(this).get(MainViewModel::class.java) }
    private val dis = LifeDisposable(this)
    val songAdapter = SongAdapter()
    private val permissions = RxPermissions(this)
    var mService: MusicService? = null
    var mIntent: Intent? = null
    var musicBound = false
    var paused = false
    var playBackPaused = false
    private val myRunnable = Runnable { screensaver.visible() }
    private val myVolumeRunnable = Runnable { volumeGroup.gone() }
    private val myHandler = Handler()
    lateinit var binding: ActivityMainBinding
    lateinit var lm: LinearLayoutManager
    private val gestureListener = SwipeGestureListener()
    private val audioManager: AudioManager by lazy { applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager }

    @SuppressLint("CheckResult", "WakelockTimeout")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        startService(Intent(this, StartAppService::class.java))
        songList.adapter = songAdapter
        lm = LinearLayoutManager(this)
        songList.layoutManager = lm
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        val gestureDetectorCompat = GestureDetectorCompat(this, gestureListener)

        permissions.request(Manifest.permission.READ_EXTERNAL_STORAGE)
            .subscribe { allow ->
                if (allow) {
                    dis add viewModel.getSongs(contentResolver)
                        .subscribeBy(
                            onNext = {
                                songAdapter.data = it
                            },
                            onError = {
                                toast(it.message!!)
                                it.printStackTrace()
                            }
                        )
                }
            }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            songList.setOnScrollChangeListener { _, _, _, _, _ ->
                setAnimationVisible()
            }
        }


        RxTextView.textChanges(etSearch)
            .subscribe {
                if (it.toString().isNotEmpty()) {
                    setAnimationVisible()
                    val search = songAdapter.data.find { song ->
                        song.title.toLowerCase().contains(Regex(it.toString().toLowerCase()))
                    }
                    if (search != null) lm.scrollToPositionWithOffset(songAdapter.data.indexOf(search), 2)
                } else etHint.visible()
            }

        btnShuffle.setOnClickListener {
            setAnimationVisible()
            viewModel.isServiceRunning(mService)
                .subscribe {
                    if (it) {
                        if (mService!!.shuffle)
                            btnShuffle.setImageDrawable(getDrawable(R.drawable.ic_shuffle))
                        else btnShuffle.setImageDrawable(getDrawable(R.drawable.ic_shuffle_on))
                        mService!!.setShuffle()
                        Prefs.shuffle = mService!!.shuffle
                    } else {
                        startServ()
                            .subscribe {
                                if (mService!!.shuffle)
                                    btnShuffle.setImageDrawable(getDrawable(R.drawable.ic_shuffle))
                                else btnShuffle.setImageDrawable(getDrawable(R.drawable.ic_shuffle_on))
                                mService!!.setShuffle()
                                Prefs.shuffle = mService!!.shuffle
                            }
                    }

                }

        }
        btnNext.setOnClickListener {
            actionNext()
        }
        btnPrevious.setOnClickListener {
            actionPrevious()
        }
        btnPlay.setOnClickListener {
            actionPlay()
        }

        animationContainer.setOnTouchListener { v, event ->
            gestureDetectorCompat.onTouchEvent(event)
            true
        }
    }

    @SuppressLint("CheckResult")
    override fun onResume() {
        super.onResume()
        if (paused)
            paused = false

        detectGesture.subscribe {
            when (it) {
                0 -> {
                    audioManager.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.FLAG_PLAY_SOUND)
                    showVolumeUpLabel(UP)
                }
                1 -> {
                    audioManager.adjustVolume(AudioManager.ADJUST_LOWER, AudioManager.FLAG_PLAY_SOUND)
                    showVolumeUpLabel(DOWN)
                }
                2 -> actionPrevious()
                3 -> actionNext()
                4 -> actionPlay()
                5 -> {
                    setAnimationGone()
                    setAnimationVisible()
                }
                else -> toast("OTRO")
            }
        }

        hearPlayingSong
            .subscribe { (currPos, prevPos) ->
                btnPlay.setImageDrawable(getDrawable(R.drawable.btn_stop))
                songAdapter.data[prevPos].isPlaying = false
                songAdapter.notifyItemChanged(prevPos)
                songAdapter.data[currPos].isPlaying = true
                songAdapter.notifyItemChanged(currPos)
                binding.title = songAdapter.data[currPos].title
            }

        dis add songAdapter.onClickSong
            .subscribe { song ->
                setAnimationVisible()
                viewModel.isServiceRunning(mService)
                    .subscribe {
                        if (it) {
                            mService!!.setSong(songAdapter.data.indexOf(song))
                            mService!!.playSong()
                            if (playBackPaused) {
                                playBackPaused = false
                            }
                        } else {
                            startServ().subscribeBy(
                                onNext = {
                                    mService!!.setSong(songAdapter.data.indexOf(song))
                                    mService!!.playSong()
                                    if (playBackPaused) {
                                        playBackPaused = false
                                    }
                                },
                                onError = { err ->
                                    toast(err.message!!)
                                }
                            )

                        }
                    }
            }
    }

    override fun onPause() {
        super.onPause()
        paused = true
    }

    @SuppressLint("CheckResult")
    fun actionPrevious() {
        setAnimationVisible()
        viewModel.isServiceRunning(mService)
            .subscribe {
                if (it) {
                    mService!!.playPrev()
                    if (playBackPaused)
                        playBackPaused = false
                    songList.scrollToPosition(mService!!.getCurrentPosition())
                } else {
                    startServ()
                        .subscribe {
                            mService!!.playPrev()
                            if (playBackPaused)
                                playBackPaused = false
                            songList.scrollToPosition(mService!!.getCurrentPosition())
                        }
                }
            }
    }

    @SuppressLint("CheckResult")
    fun actionNext() {
        setAnimationVisible()
        viewModel.isServiceRunning(mService)
            .subscribe {
                if (it) {
                    mService!!.playNext()
                    if (playBackPaused)
                        playBackPaused = false
                    songList.scrollToPosition(mService!!.getCurrentPosition())
                } else {
                    startServ()
                        .subscribe {
                            mService!!.playNext()
                            if (playBackPaused)
                                playBackPaused = false
                            songList.scrollToPosition(mService!!.getCurrentPosition())
                        }
                }

            }
    }

    @SuppressLint("CheckResult")
    fun actionPlay() {
        setAnimationVisible()
        viewModel.isServiceRunning(mService)
            .subscribe {
                if (it) {
                    if (mService!!.isPlaying()) {
                        mService!!.stop()
                        stopService(mIntent)
                        mService = null
                        btnPlay.setImageDrawable(getDrawable(R.drawable.btn_play))
                    } else {
                        mService!!.playSong()
                        btnPlay.setImageDrawable(getDrawable(R.drawable.btn_stop))
                    }
                } else {
                    startServ().subscribeBy(
                        onNext = {
                            mService!!.playSong()
                            btnPlay.setImageDrawable(getDrawable(R.drawable.btn_stop))
                        }, onError = {
                            toast(it.message!!)
                        }
                    )
                }
            }
    }

    private fun showVolumeUpLabel(direction: Int) {
        myHandler.removeCallbacks(myVolumeRunnable)
        val volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        volumeValue.text = Math.round((volume * 100 / 15).toDouble()).toString()
        when (direction) {
            UP -> {
                actionLabel.setImageDrawable(getDrawable(R.drawable.ic_volume_up))
                actionText.text = getString(R.string.volume_up)
            }
            DOWN -> {
                actionLabel.setImageDrawable(getDrawable(R.drawable.ic_volume_down))
                actionText.text = getString(R.string.volume_down)
            }
        }
        volumeGroup.visible()
        myHandler.postDelayed(myVolumeRunnable, 2000)
    }

    private fun setAnimationVisible() {
        myHandler.removeCallbacks(myRunnable)
        myHandler.postDelayed(myRunnable, 5000)
    }

    fun serviceConnection() = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as MusicService.MusicBinder
            mService = binder.service
            mService!!.setList(songAdapter.data)
            musicBound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            musicBound = false
        }

    }

    @SuppressLint("CheckResult")
    override fun onStart() {
        super.onStart()
        startServ().subscribeBy(
            onNext = {
                mService!!.setSong(Prefs.lastSong)
                songList.scrollToPosition(Prefs.lastSong)
                mService!!.shuffle = Prefs.shuffle
                if (mService!!.shuffle)
                    btnShuffle.setImageDrawable(getDrawable(R.drawable.ic_shuffle_on))
                else btnShuffle.setImageDrawable(getDrawable(R.drawable.ic_shuffle))
            },
            onError = {
                it.printStackTrace()
            }
        )
    }

    fun setAnimationGone() {
        screensaver.gone()
    }

    override fun onDestroy() {
        if (mService != null) {
            Prefs.shuffle = mService!!.shuffle
            Prefs.lastSong = mService!!.getCurrentPosition()
            stopService(mIntent)
        }
        super.onDestroy()
    }

    fun startServ(): Observable<Boolean> =
        Observable.create<Boolean> {

            if (mIntent == null) {
                mIntent = Intent(this, MusicService::class.java)
            }
            bindService(mIntent, serviceConnection(), Context.BIND_AUTO_CREATE)
            startService(mIntent)
            Handler().postDelayed({ it.onNext(true) }, 250)
        }

    companion object {
        val hearPlayingSong = PublishSubject.create<Pair<Int, Int>>()
        val detectGesture = PublishSubject.create<Int>()
        const val UP = 0
        const val DOWN = 1
        const val LEFT = 2
        const val RIGHT = 3
        const val TAP = 4
        const val DOUBLE_TAP = 5
    }
}
