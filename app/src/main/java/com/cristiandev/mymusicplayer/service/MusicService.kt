package com.cristiandev.mymusicplayer.service

import android.app.*
import android.bluetooth.BluetoothAdapter
import android.content.*
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.util.Log
import com.cristiandev.mymusicplayer.R
import com.cristiandev.mymusicplayer.data.model.Song
import com.cristiandev.mymusicplayer.main.MainActivity
import com.cristiandev.mymusicplayer.main.MainActivity.Companion.hearPlayingSong
import org.jetbrains.anko.startActivity
import java.lang.Exception
import java.util.*

class MusicService : Service(), MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
    MediaPlayer.OnCompletionListener,AudioManager.OnAudioFocusChangeListener {

    lateinit var player: MediaPlayer
    private var thsSongs = mutableListOf<Song>()
    private var songPosition = 0
    private var lastSongPosition = 0
    private val musicBind = MusicBinder()
    var songTitle = ""
    val NOTIFY_ID = 1
    var shuffle = false
    lateinit var rand: Random
//    lateinit var focus:AudioManager

    override fun onCreate() {
//        focus = AudioManager
        createNotificationChannel()
        player = MediaPlayer()
        initMusicPlayer()
        rand = Random()
    }

    fun setShuffle() {
        shuffle = !shuffle
    }

    fun setList(songs: MutableList<Song>) {
        thsSongs = songs
    }

    fun initMusicPlayer() {
        player.setWakeMode(applicationContext, PowerManager.PARTIAL_WAKE_LOCK)
        player.setAudioStreamType(AudioManager.STREAM_MUSIC)
        player.setOnPreparedListener(this)
        player.setOnErrorListener(this)
        player.setOnCompletionListener(this)
    }

    fun stop() = player.stop()

    fun playSong() {
        player.reset()
        val song = thsSongs[songPosition]
        songTitle = song.title
        val currSong = song.id
        val trackUri =
            ContentUris.withAppendedId(android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, currSong)
        try {
            player.setDataSource(applicationContext, trackUri)
        } catch (e: Exception) {
            Log.e("MUSIC SERVICE", "ERROR SETTING DATA SOURCE")
        }
        hearPlayingSong.onNext(Pair(songPosition, lastSongPosition))
        player.prepareAsync()
    }

    fun setSong(songIndex: Int) {
        lastSongPosition = songPosition
        songPosition = songIndex
    }

    inner class MusicBinder : Binder() {
        internal val service: MusicService
            get() = this@MusicService
    }

    override fun onPrepared(mp: MediaPlayer?) {
        mp!!.start()
        val notIntent = Intent(this, MainActivity::class.java)
        notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0, notIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val builder = NotificationCompat.Builder(this, NOTIFY_ID.toString())
            .setContentIntent(pendingIntent)
            .setVibrate(null)
            .setSmallIcon(R.drawable.ic_play)
            .setTicker(songTitle)
            .setOngoing(true)
            .setContentTitle("Playing")
            .setContentText(songTitle)
            .setVibrate(LongArray(1) { 0 })
        with(NotificationManagerCompat.from(this)) {
            notify(NOTIFY_ID, builder.build())
        }
    }

    override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        mp!!.reset()
        return false
    }

    override fun onCompletion(mp: MediaPlayer?) {
        if (player.currentPosition > 0) {
            mp!!.reset()
            playNext()
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return musicBind
    }

    override fun onUnbind(intent: Intent?): Boolean {
        player.stop()
        player.release()
        return false
    }

    fun playPrev() {
        lastSongPosition = songPosition
        if (shuffle) {
            var newSong = songPosition
            while (newSong == songPosition) {
                newSong = rand.nextInt(thsSongs.size)
            }
            songPosition = newSong
        } else {
            songPosition--
            if (songPosition < 0) songPosition = thsSongs.size - 1
        }
        playSong()
    }

    fun playNext() {
        lastSongPosition = songPosition
        if (shuffle) {
            var newSong = songPosition
            while (newSong == songPosition) {
                newSong = rand.nextInt(thsSongs.size)
            }
            songPosition = newSong
        } else {
            songPosition++
            if (songPosition > thsSongs.size) songPosition = 0
        }

        playSong()
    }

    fun getCurrentPosition() = songPosition
    fun isPlaying() = player.isPlaying

    override fun onDestroy() {
        with(NotificationManagerCompat.from(this)) {
            cancel(NOTIFY_ID)
        }
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "MyMusicChannel"
            val descriptionText = "MyMusicChannelDescription"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(NOTIFY_ID.toString(), name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onAudioFocusChange(focusChange: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}