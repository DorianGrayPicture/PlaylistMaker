package com.example.playlistmaker

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView

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
            val link = "https://practicum.yandex.ru/android-developer/?from=catalog"
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_TEXT, link)
            startActivity(Intent.createChooser(shareIntent, "Sharing app"))
        }

        textToSupportButton.setOnClickListener {
            val message = "Спасибо разработчикам за крутое приложение!"
            val messageTopic = "Сообщение разработчикам приложения Playlist Maker"
            val supportIntent = Intent(Intent.ACTION_SENDTO)
            supportIntent.data = Uri.parse("mailto:")
            supportIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf("ArbeitenDenis@yandex.ru"))
            supportIntent.putExtra(
                Intent.EXTRA_SUBJECT,
                messageTopic
            )
            supportIntent.putExtra(Intent.EXTRA_TEXT, message)
            startActivity(supportIntent)
        }

        openConsumerApplicationButton.setOnClickListener {
            val browseIntent =
                Intent(Intent.ACTION_VIEW, Uri.parse("https://yandex.ru/legal/practicum_offer/"))
            startActivity(browseIntent)
        }
    }
}