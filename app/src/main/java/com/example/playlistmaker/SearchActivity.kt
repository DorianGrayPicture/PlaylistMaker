package com.example.playlistmaker

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Locale

class SearchActivity : AppCompatActivity() {

    private val iTunesBaseUrl = "https://itunes.apple.com"

    private val searchRunnable = Runnable { searchRequest() }

    private val retrofit = Retrofit.Builder()
        .baseUrl(iTunesBaseUrl)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val iTunesService: ITunesApi = retrofit.create(ITunesApi::class.java)

    private lateinit var sharedPreferences: SharedPreferences

    private val mainThreadHandler = Handler(Looper.getMainLooper())

    private var isClickAllowed = true

    private var savedText = INPUT_TEXT_DEF

    private lateinit var inputEditText: EditText
    private lateinit var navigateBackButton: ImageView
    private lateinit var clearButton: ImageView
    private lateinit var tracksListRecycler: RecyclerView
    private lateinit var placeholderImage: ImageView
    private lateinit var placeholderText: TextView
    private lateinit var refreshButton: TextView
    private lateinit var historyListRecycler: RecyclerView
    private lateinit var clearHistoryButton: TextView
    private lateinit var placeholderHistory: LinearLayout
    private lateinit var progressBar: ProgressBar

    val tracksListAdapter = TrackAdapter {
        if (clickDebounce()) {
            addTrack(it)
            audioPlayerIntent(it)
        }
    }

    val tracksHistoryAdapter = TrackAdapter {
        if (clickDebounce()) {
            audioPlayerIntent(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        inputEditText = findViewById(R.id.inputEditText)
        navigateBackButton = findViewById(R.id.navigate_back)
        clearButton = findViewById(R.id.clearIcon)
        tracksListRecycler = findViewById(R.id.recyclerView)
        placeholderImage = findViewById(R.id.placeholderImage)
        placeholderText = findViewById(R.id.placeholderText)
        refreshButton = findViewById(R.id.refreshButton)
        historyListRecycler = findViewById(R.id.historyRecycler)
        clearHistoryButton = findViewById(R.id.clearHistory)
        placeholderHistory = findViewById(R.id.searchHistory)
        progressBar = findViewById(R.id.progressBar)

        sharedPreferences = getSharedPreferences(SEARCH_HISTORY_PREFERENCE, MODE_PRIVATE)

        val savedTracks = sharedPreferences.getString(TRACK_LIST_KEY, null)
        if (savedTracks != null) {
            tracksHistoryAdapter.tracks = createTrackListFromJson(savedTracks)
        }

        tracksListRecycler.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        tracksListRecycler.adapter = tracksListAdapter

        historyListRecycler.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        historyListRecycler.adapter = tracksHistoryAdapter

        clearButton.setOnClickListener {
            inputEditText.setText("")

            tracksListAdapter.tracks.clear()
            tracksListAdapter.notifyDataSetChanged()
            hidePlaceholders()

            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(inputEditText.windowToken, 0)
        }

        clearHistoryButton.setOnClickListener {
            tracksHistoryAdapter.tracks.clear()
            tracksHistoryAdapter.notifyDataSetChanged()
            sharedPreferences.edit().remove(TRACK_LIST_KEY).apply()

            placeholderHistory.visibility = View.GONE
        }

        refreshButton.setOnClickListener {
            searchRequest()
        }

        inputEditText.setOnFocusChangeListener { _, hasFocus ->
            placeholderHistory.visibility =
                if (hasFocus && inputEditText.text.isEmpty() && tracksHistoryAdapter.tracks.isNotEmpty()) View.VISIBLE else View.GONE
        }

        navigateBackButton.setOnClickListener {
            finish()
        }

        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // empty
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (inputEditText.text.trim().isEmpty()) {
                    tracksListRecycler.visibility = View.GONE
                }

                clearButton.visibility = clearButtonVisibility(s)
                savedText = s.toString()

                placeholderHistory.visibility =
                    if (inputEditText.hasFocus() && s?.trim()
                            ?.isEmpty() == true && tracksHistoryAdapter.tracks.isNotEmpty()
                    ) View.VISIBLE else View.GONE

                searchDebounce()
            }

            override fun afterTextChanged(s: Editable?) {
                // empty
            }
        }

        inputEditText.addTextChangedListener(textWatcher)
    }

    private fun searchDebounce() {
        mainThreadHandler.removeCallbacks(searchRunnable)
        mainThreadHandler.postDelayed(searchRunnable, SEARCH_DEBOUNCE_DELAY)
    }

    private fun clearButtonVisibility(s: CharSequence?): Int {
        return if (s.isNullOrEmpty()) {
            View.GONE
        } else {
            View.VISIBLE
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.run {
            putString(INPUT_TEXT, savedText)
        }

        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        savedText = savedInstanceState.getString(INPUT_TEXT, INPUT_TEXT_DEF)
        inputEditText.setText(savedText)
    }

    private fun searchRequest() {
        if (inputEditText.text.trim().isNotEmpty()) {
            hidePlaceholders()
            progressBar.visibility = View.VISIBLE
            tracksListRecycler.visibility = View.GONE

            iTunesService.search(inputEditText.text.toString())
                .enqueue(object : Callback<TracksResponse> {
                    override fun onResponse(
                        call: Call<TracksResponse>,
                        response: Response<TracksResponse>
                    ) {
                        progressBar.visibility = View.GONE
                        when (response.code()) {
                            200 -> {
                                tracksListAdapter.tracks.clear()
                                if (response.body()?.tracks?.isNotEmpty() == true) {
                                    tracksListRecycler.visibility = View.VISIBLE
                                    tracksListAdapter.tracks.addAll(response.body()?.tracks!!)
                                    tracksListAdapter.notifyDataSetChanged()
                                } else {
                                    showPlaceholders(
                                        R.string.nothing_found_placeholder_text,
                                        R.drawable.ic_nothing_found_placeholder
                                    )
                                }
                            }

                            else -> {
                                showPlaceholders(
                                    R.string.network_error_placeholder_text,
                                    R.drawable.ic_network_error_placeholder
                                )
                                refreshButton.visibility = View.VISIBLE
                            }
                        }
                    }

                    override fun onFailure(call: Call<TracksResponse>, t: Throwable) {
                        showPlaceholders(
                            R.string.network_error_placeholder_text,
                            R.drawable.ic_network_error_placeholder
                        )
                        refreshButton.visibility = View.VISIBLE
                    }
                })
        }
    }

    private fun clickDebounce(): Boolean {
        val current = isClickAllowed
        if (isClickAllowed) {
            isClickAllowed = false
            mainThreadHandler.postDelayed({ isClickAllowed = true }, CLICK_DEBOUNCE)
        }

        return current
    }

    private fun addTrack(track: Track) {
        val tracksJson = sharedPreferences.getString(TRACK_LIST_KEY, null)

        if (tracksJson != null) {
            if (track.trackId.toString() in tracksJson) {
                tracksHistoryAdapter.tracks.remove(track)
                tracksHistoryAdapter.tracks.add(0, track)
            } else if (tracksHistoryAdapter.tracks.size == 10) {
                tracksHistoryAdapter.tracks.removeAt(tracksHistoryAdapter.tracks.lastIndex)
                tracksHistoryAdapter.tracks.add(0, track)
            } else {
                tracksHistoryAdapter.tracks.add(0, track)
            }
        } else {
            tracksHistoryAdapter.tracks.add(track)
        }

        tracksHistoryAdapter.notifyDataSetChanged()

        sharedPreferences.edit()
            .putString(TRACK_LIST_KEY, createJsonFromTrackList(tracksHistoryAdapter.tracks))
            .apply()
    }

    private fun audioPlayerIntent(track: Track) {
        val audioPlayerIntent = Intent(this@SearchActivity, AudioPlayerActivity::class.java)
        audioPlayerIntent.apply {
            putExtra(KEY_FOR_TRACK_NAME, track.trackName)
            putExtra(KEY_FOR_ARTIST_NAME, track.artistName)
            putExtra(
                KEY_FOR_TRACK_TIME,
                SimpleDateFormat("mm:ss", Locale.getDefault()).format(track.trackTimeMillis)
            )
            putExtra(KEY_FOR_COLLECTION_NAME, track.collectionName)
            putExtra(KEY_FOR_RELEASE_DATE, track.releaseDate.slice(0..3))
            putExtra(KEY_FOR_PRIMARY_GENRE_NAME, track.primaryGenreName)
            putExtra(KEY_FOR_COUNTRY, track.country)
            putExtra(
                KEY_FOR_ARTWORK_URL,
                track.artworkUrl100.replaceAfterLast('/', "512x512bb.jpg")
            )
        }
        startActivity(audioPlayerIntent)
    }

    private fun showPlaceholders(@StringRes text: Int, @DrawableRes image: Int) {
        placeholderImage.setImageResource(image)
        placeholderImage.visibility = View.VISIBLE
        placeholderText.setText(text)
    }

    private fun hidePlaceholders() {
        placeholderText.text = ""
        placeholderImage.visibility = View.GONE
        refreshButton.visibility = View.GONE
    }

    private fun createJsonFromTrackList(tracks: MutableList<Track>): String {
        return Gson().toJson(tracks)
    }

    private fun createTrackListFromJson(json: String): MutableList<Track> {
        return Gson().fromJson(json, Array<Track>::class.java).toMutableList()
    }

    override fun onStop() {
        super.onStop()

        Log.d("TAG", "onStop")
        Log.d("TAG", createJsonFromTrackList(tracksHistoryAdapter.tracks))

        sharedPreferences.edit()
            .putString(TRACK_LIST_KEY, createJsonFromTrackList(tracksHistoryAdapter.tracks))
            .apply()
    }

    companion object {
        const val INPUT_TEXT = "INPUT_TEXT"
        const val INPUT_TEXT_DEF = ""

        const val SEARCH_HISTORY_PREFERENCE = "search_history_preference"
        const val TRACK_LIST_KEY = "key_for_track_list"

        const val KEY_FOR_TRACK_NAME = "track_name"
        const val KEY_FOR_ARTIST_NAME = "artist_name"
        const val KEY_FOR_TRACK_TIME = "track_time"
        const val KEY_FOR_COLLECTION_NAME = "collection_name"
        const val KEY_FOR_RELEASE_DATE = "release_date"
        const val KEY_FOR_PRIMARY_GENRE_NAME = "primary_genre_name"
        const val KEY_FOR_COUNTRY = "country"
        const val KEY_FOR_ARTWORK_URL = "art_work_url"

        const val SEARCH_DEBOUNCE_DELAY = 2000L
        const val CLICK_DEBOUNCE = 1000L
    }
}