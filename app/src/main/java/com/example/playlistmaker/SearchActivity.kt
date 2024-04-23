package com.example.playlistmaker

import Track
import TracksResponse
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
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

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
    private lateinit var recycler: RecyclerView
    private lateinit var nothingFoundPlaceholderImage: ImageView
    private lateinit var placeholderText: TextView

    private val tracks = ArrayList<Track>()

    private val adapter = TrackAdapter()

    private var savedText = INPUT_TEXT_DEF
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        inputEditText = findViewById(R.id.inputEditText)
        navigateBackButton = findViewById(R.id.navigate_back)
        clearButton = findViewById(R.id.clearIcon)
        recycler = findViewById(R.id.recyclerView)
        nothingFoundPlaceholderImage = findViewById(R.id.nothingFoundPlaceholderImage)
        placeholderText = findViewById(R.id.placeholderText)

        adapter.tracks = tracks

        recycler.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recycler.adapter = adapter

        clearButton.setOnClickListener {
            inputEditText.setText("")

            tracks.clear()
            adapter.notifyDataSetChanged()

            nothingFoundPlaceholderImage.visibility = View.GONE
            placeholderText.visibility = View.GONE

            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(inputEditText.windowToken, 0)
        }

        inputEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                tracks.clear()

                iTunesService.search(inputEditText.text.toString())
                    .enqueue(object : Callback<TracksResponse> {
                        override fun onResponse(
                            call: Call<TracksResponse>,
                            response: Response<TracksResponse>
                        ) {
                            when (response.code()) {
                                200 -> {
                                    if (response.body()?.tracks?.isNotEmpty() == true) {
                                        tracks.clear()
                                        tracks.addAll(response.body()?.tracks!!)
                                        adapter.notifyDataSetChanged()
                                    } else {
                                        placeholderText.setText(R.string.nothing_found)
                                        nothingFoundPlaceholderImage.visibility = View.VISIBLE
                                    }
                                }

                                else -> {
                                    placeholderText.text =
                                        "Проблемы со связью\n\nЗагрузка не удалась. Проверьте подключение к интернету"
                                }
                            }
                        }

                        override fun onFailure(call: Call<TracksResponse>, t: Throwable) {
                            placeholderText.text =
                                "Проблемы со связью\n\nЗагрузка не удалась. Проверьте подключение к интернету"
                        }
                    })
            }
            false
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

    }

    companion object {
        const val INPUT_TEXT = "INPUT_TEXT"
        const val INPUT_TEXT_DEF = ""
    }
}