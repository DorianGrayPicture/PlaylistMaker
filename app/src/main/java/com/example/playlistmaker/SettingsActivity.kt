package com.example.playlistmaker

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val navigateBackButton = findViewById<TextView>(R.id.settings_back)

        navigateBackButton.setOnClickListener {
            val navigateBackIntent = Intent(this, MainActivity::class.java)
            startActivity(navigateBackIntent)
        }
    }
}