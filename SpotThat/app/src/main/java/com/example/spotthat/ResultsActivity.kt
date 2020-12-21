package com.example.spotthat

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.jetbrains.anko.doAsync
import java.lang.Exception

class ResultsActivity : AppCompatActivity() {
    companion object{
        const val TAG = "ResultsActivity"
    }
    lateinit var spotifyPlaylistRecyclerView: RecyclerView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_results)
        spotifyPlaylistRecyclerView = findViewById(R.id.spotifyPlaylistRecyclerView)
        var query = intent.getStringExtra("quickSearchQuery")

        // Perform quick search
        if (query != null) {
            this.title = "Quick Search Results for $query"
            if (MainActivity.accessToken.isEmpty()) {
                Log.d(TAG, "Failed to get token.")
                val mainActivityIntent = Intent(this@ResultsActivity, MainActivity::class.java)
                startActivity(mainActivityIntent)
            } else
                Log.d(TAG, "Token received: ${MainActivity.accessToken}")
            quickSearch(query)
        }
    }

    private fun quickSearch(query: String) {
        try {
            doAsync {
                // Get current user Spotify playlist
                SpotifyManager().quickSearchPlaylist(
                    query,
                    MainActivity.accessToken
                ) { mySpotifyPlaylist ->
                    val playListAdapter = SpotifyPlaylistAdapter(
                        mySpotifyPlaylist,
                        true,
                        false,
                        false,
                        "false",
                    )
                    runOnUiThread {
                        spotifyPlaylistRecyclerView.adapter = playListAdapter
                        spotifyPlaylistRecyclerView.layoutManager =
                            LinearLayoutManager(applicationContext)
                    }
                }
            }
        } catch (exception: Exception) {
            Log.e(SpotifyUserPlaylistActivity.TAG, "Fetching Spotify playlist failed", exception)
            runOnUiThread {
                Toast.makeText(
                    this@ResultsActivity,
                    "Failed to retrieve Spotify playlist",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}


