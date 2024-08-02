package com.example.playlistmaker

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.SearchActivity.Companion.KEY_FOR_ARTIST_NAME
import com.example.playlistmaker.SearchActivity.Companion.KEY_FOR_ARTWORK_URL
import com.example.playlistmaker.SearchActivity.Companion.KEY_FOR_COLLECTION_NAME
import com.example.playlistmaker.SearchActivity.Companion.KEY_FOR_COUNTRY
import com.example.playlistmaker.SearchActivity.Companion.KEY_FOR_PREVIEW_URL
import com.example.playlistmaker.SearchActivity.Companion.KEY_FOR_PRIMARY_GENRE_NAME
import com.example.playlistmaker.SearchActivity.Companion.KEY_FOR_RELEASE_DATE
import com.example.playlistmaker.SearchActivity.Companion.KEY_FOR_TRACK_NAME
import com.example.playlistmaker.SearchActivity.Companion.KEY_FOR_TRACK_TIME
import java.text.SimpleDateFormat
import java.util.Locale

class AudioPlayerActivity : AppCompatActivity() {

    private var mediaPlayer = MediaPlayer()

    private var previewUrl: String? = null

    private var playerState = STATE_DEFAULT

    private val mainThreadHandler = Handler(Looper.getMainLooper())

    private lateinit var arrowBackButton: ImageView
    private lateinit var trackName: TextView
    private lateinit var artistName: TextView
    private lateinit var trackTime: TextView
    private lateinit var collectionName: TextView
    private lateinit var releaseDate: TextView
    private lateinit var primaryGenreName: TextView
    private lateinit var country: TextView
    private lateinit var artWork: ImageView
    private lateinit var playButton: ImageView
    private lateinit var timerProgressBar: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audio_player)

        arrowBackButton = findViewById(R.id.arrowBack)
        trackName = findViewById(R.id.trackName)
        artistName = findViewById(R.id.artistName)
        trackTime = findViewById(R.id.trackTime)
        collectionName = findViewById(R.id.collectionName)
        releaseDate = findViewById(R.id.releaseDate)
        primaryGenreName = findViewById(R.id.primaryGenreName)
        country = findViewById(R.id.country)
        artWork = findViewById(R.id.artWork)
        playButton = findViewById(R.id.playButton)
        timerProgressBar = findViewById(R.id.timerProgressBar)

        val extras = intent.extras
        if (extras != null) {
            trackName.text = extras.getString(KEY_FOR_TRACK_NAME)
            artistName.text = extras.getString(KEY_FOR_ARTIST_NAME)
            trackTime.text = extras.getString(KEY_FOR_TRACK_TIME)
            collectionName.text = extras.getString(KEY_FOR_COLLECTION_NAME)
            releaseDate.text = extras.getString(KEY_FOR_RELEASE_DATE)
            primaryGenreName.text = extras.getString(KEY_FOR_PRIMARY_GENRE_NAME)
            country.text = extras.getString(KEY_FOR_COUNTRY)
            previewUrl = extras.getString(KEY_FOR_PREVIEW_URL)

            val imageUrl = extras.getString(KEY_FOR_ARTWORK_URL)
            Glide.with(artWork)
                .load(imageUrl)
                .centerCrop()
                .placeholder(R.drawable.ic_placeholder)
                .transform(RoundedCorners(8))
                .into(artWork)
        }

        preparePlayer()

        playButton.setOnClickListener {
            playbackControl()
        }

        arrowBackButton.setOnClickListener {
            finish()
        }
    }

    override fun onPause() {
        super.onPause()
        pausePlayer()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
    }

    private fun preparePlayer() {
        mediaPlayer.apply {
            setDataSource(previewUrl)
            prepareAsync()
            setOnPreparedListener {
                playerState = STATE_PREPARED
            }
            setOnCompletionListener {
                playerState = STATE_PREPARED
                timerProgressBar.text = "00:00"
                playButton.setImageResource(R.drawable.button_play)
            }
        }
    }

    private fun startPlayer() {
        mediaPlayer.start()
        playerState = STATE_PLAYING
        playButton.setImageResource(R.drawable.button_pause)

        mainThreadHandler.post(updateTimer())
    }

    private fun pausePlayer() {
        mediaPlayer.pause()
        playerState = STATE_PAUSED
        playButton.setImageResource(R.drawable.button_play)

        mainThreadHandler.removeCallbacks(updateTimer())
    }

    private fun playbackControl() {
        when (playerState) {
            STATE_PLAYING -> {
                pausePlayer()
            }

            STATE_PREPARED, STATE_PAUSED -> {
                startPlayer()
            }
        }
    }

    private fun updateTimer(): Runnable {
        return object : Runnable {
            override fun run() {
                if (playerState == STATE_PLAYING) {
                    timerProgressBar.text = SimpleDateFormat(
                        "mm:ss",
                        Locale.getDefault()
                    ).format(mediaPlayer.currentPosition)
                    mainThreadHandler.postDelayed(this, 500L)
                }
            }
        }
    }

    companion object {
        private const val STATE_DEFAULT = 0
        private const val STATE_PREPARED = 1
        private const val STATE_PLAYING = 2
        private const val STATE_PAUSED = 3
    }
}