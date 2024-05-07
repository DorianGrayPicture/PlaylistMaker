package com.example.playlistmaker

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

const val SEARCH_HISTORY_PREFERENCE = "search_history_preference"

class SearchActivity : AppCompatActivity() {

    private val iTunesBaseUrl = "https://itunes.apple.com"

    private val retrofit = Retrofit.Builder()
        .baseUrl(iTunesBaseUrl)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val iTunesService: ITunesApi = retrofit.create(ITunesApi::class.java)

    private lateinit var inputEditText: EditText
    private lateinit var navigateBackButton: ImageView
    private lateinit var clearButton: ImageView
    private lateinit var tracksListRecycler: RecyclerView
    private lateinit var placeholderImage: ImageView
    private lateinit var placeholderText: TextView
    private lateinit var refreshButton: TextView
    private lateinit var historyListRecycler: RecyclerView

    private val tracksListAdapter = TrackAdapter()
    private val historyListAdapter = TrackAdapter()

    private var savedText = INPUT_TEXT_DEF
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

        val placeholderHistory = findViewById<LinearLayout>(R.id.searchHistory)

        tracksListRecycler.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        tracksListRecycler.adapter = tracksListAdapter

        val sharedPreferences = getSharedPreferences(SEARCH_HISTORY_PREFERENCE, MODE_PRIVATE)
        val searchHistory: SearchHistory = SearchHistory(sharedPreferences)

        clearButton.setOnClickListener {
            inputEditText.setText("")

            tracksListAdapter.tracks.clear()
            tracksListAdapter.notifyDataSetChanged()
            hidePlaceholders()

            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(inputEditText.windowToken, 0)
        }

        refreshButton.setOnClickListener {
            search()
        }

        inputEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                if (inputEditText.text.isNotEmpty()) {
                    search()
                }
            }
            false
        }

        inputEditText.setOnFocusChangeListener { view, hasFocus ->
            placeholderHistory.visibility =
                if (hasFocus && inputEditText.text.isEmpty()) View.VISIBLE else View.GONE
        }

        navigateBackButton.setOnClickListener {
            finish()
        }

        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // empty
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                clearButton.visibility = clearButtonVisibility(s)
                savedText = s.toString()

                placeholderHistory.visibility =
                    if (inputEditText.hasFocus() && s?.isEmpty() == true) View.VISIBLE else View.GONE
            }

            override fun afterTextChanged(s: Editable?) {
                // empty
            }
        }

        inputEditText.addTextChangedListener(textWatcher)
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

    private fun search() {
        tracksListAdapter.tracks.clear()
        hidePlaceholders()

        iTunesService.search(inputEditText.text.toString())
            .enqueue(object : Callback<TracksResponse> {
                override fun onResponse(
                    call: Call<TracksResponse>,
                    response: Response<TracksResponse>
                ) {
                    when (response.code()) {
                        200 -> {
                            if (response.body()?.tracks?.isNotEmpty() == true) {
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


    companion object {
        const val INPUT_TEXT = "INPUT_TEXT"
        const val INPUT_TEXT_DEF = ""
    }
}