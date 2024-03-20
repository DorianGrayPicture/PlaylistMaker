package com.example.playlistmaker

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val navigateBackButton = findViewById<TextView>(R.id.settings_back)
        val shareAppButton = findViewById<TextView>(R.id.share_app)
        val textToSupportButton = findViewById<TextView>(R.id.text_to_support)
        val openConsumerApplicationButton = findViewById<TextView>(R.id.consumer_application)

        navigateBackButton.setOnClickListener {
            val navigateBackIntent = Intent(this, MainActivity::class.java)
            startActivity(navigateBackIntent)
        }

        shareAppButton.setOnClickListener {
            val link = resources.getString(R.string.yandex_practicum_link)
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, link)
            }
            val chooser: Intent = Intent.createChooser(shareIntent, null)

            try {
                startActivity(chooser)
            } catch (e: ActivityNotFoundException) {
                val toast = Toast.makeText(applicationContext, resources.getString(R.string.app_not_found), Toast.LENGTH_SHORT)
                toast.show()
            }
        }

        textToSupportButton.setOnClickListener {
            val text = resources.getString(R.string.text_to_support)
            val subject = resources.getString(R.string.topic_text_to_support)
            val email = arrayOf(resources.getString(R.string.student_email))
            val supportIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL, email)
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, text)
            }

            try {
                startActivity(supportIntent)
            } catch (e: ActivityNotFoundException) {
                val toast = Toast.makeText(applicationContext, resources.getString(R.string.app_not_found), Toast.LENGTH_SHORT)
                toast.show()
            }
        }

        openConsumerApplicationButton.setOnClickListener {
            val webpage: Uri = Uri.parse(resources.getString(R.string.practicum_offer))
            val browseIntent = Intent(Intent.ACTION_VIEW, webpage)

            try {
                startActivity(browseIntent)
            } catch (e: ActivityNotFoundException) {
                val toast = Toast.makeText(applicationContext, resources.getString(R.string.app_not_found), Toast.LENGTH_SHORT)
                toast.show()
            }
        }
    }
}