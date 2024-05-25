package com.example.playlistmaker

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.SearchActivity.Companion.KEY_FOR_ARTIST_NAME
import com.example.playlistmaker.SearchActivity.Companion.KEY_FOR_ARTWORK_URL
import com.example.playlistmaker.SearchActivity.Companion.KEY_FOR_COLLECTION_NAME
import com.example.playlistmaker.SearchActivity.Companion.KEY_FOR_COUNTRY
import com.example.playlistmaker.SearchActivity.Companion.KEY_FOR_PRIMARY_GENRE_NAME
import com.example.playlistmaker.SearchActivity.Companion.KEY_FOR_RELEASE_DATE
import com.example.playlistmaker.SearchActivity.Companion.KEY_FOR_TRACK_NAME
import com.example.playlistmaker.SearchActivity.Companion.KEY_FOR_TRACK_TIME

class AudioPlayerActivity : AppCompatActivity() {

    private lateinit var arrowBackButton: ImageView
    private lateinit var trackName: TextView
    private lateinit var artistName: TextView
    private lateinit var trackTime: TextView
    private lateinit var collectionName: TextView
    private lateinit var releaseDate: TextView
    private lateinit var primaryGenreName: TextView
    private lateinit var country: TextView
    private lateinit var artWork: ImageView

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

        val extras = intent.extras
        if (extras != null) {
            trackName.text = extras.getString(KEY_FOR_TRACK_NAME)
            artistName.text = extras.getString(KEY_FOR_ARTIST_NAME)
            trackTime.text = extras.getString(KEY_FOR_TRACK_TIME)
            collectionName.text = extras.getString(KEY_FOR_COLLECTION_NAME)
            releaseDate.text = extras.getString(KEY_FOR_RELEASE_DATE)
            primaryGenreName.text = extras.getString(KEY_FOR_PRIMARY_GENRE_NAME)
            country.text = extras.getString(KEY_FOR_COUNTRY)

            val imageUrl = extras.getString(KEY_FOR_ARTWORK_URL)
            Glide.with(artWork)
                .load(imageUrl)
                .centerCrop()
                .placeholder(R.drawable.ic_placeholder)
                .transform(RoundedCorners(8))
                .into(artWork)
        }

        arrowBackButton.setOnClickListener {
            finish()
        }
    }
}