package com.example.playlistmaker

import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson

class SearchHistory(private val sharedPreferences: SharedPreferences) {

    fun saveTrack(track: Track) {
//        sharedPreferences.edit()
//            .putString("NEW_TRACK_KEY", createJsonFromTrack(track))
//            .apply()

        Log.d("TAG", createJsonFromTrack(track))
    }

    private fun createJsonFromTrack(track: Track): String {
        return Gson().toJson(track)
    }
}