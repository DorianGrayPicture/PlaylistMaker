package com.example.playlistmaker

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val searchButton = findViewById<Button>(R.id.search)
        val mediaLibraryButton = findViewById<Button>(R.id.media_library)
        val settingsButton = findViewById<Button>(R.id.settings)

        val searchButtonClickListener: View.OnClickListener = object : View.OnClickListener {
            override fun onClick(p0: View?) {
                val searchIntent = Intent(this@MainActivity, SearchActivity::class.java)
                startActivity(searchIntent)
            }
        }

        val mediaLibraryButtonClickListener: View.OnClickListener = View.OnClickListener {
            val mediaLibraryIntent = Intent(this, MediaLibraryActivity::class.java)
            startActivity(mediaLibraryIntent)
        }

        val settingsButtonClickListener: View.OnClickListener = View.OnClickListener {
            val settingsIntent = Intent(this, SettingsActivity::class.java)
            startActivity(settingsIntent)
        }

        searchButton.setOnClickListener(searchButtonClickListener)
        mediaLibraryButton.setOnClickListener(mediaLibraryButtonClickListener)
        settingsButton.setOnClickListener(settingsButtonClickListener)
    }
}