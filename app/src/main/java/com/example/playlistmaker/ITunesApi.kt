package com.example.playlistmaker

import TracksResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ITunesApi {
    @GET("/search?entity=song&media=song")
    fun search(@Query("term") text: String): Call<TracksResponse>
}