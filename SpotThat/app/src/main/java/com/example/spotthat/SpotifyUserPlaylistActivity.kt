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
import java.lang.NullPointerException

class SpotifyUserPlaylistActivity : AppCompatActivity() {
    // debugger
    companion object {
        const val TAG = "SpotifyUserPlaylist"
    }

    // Spotify API
    private lateinit var userSpotifyPlaylistRecyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_spotify_user_playlist)

        userSpotifyPlaylistRecyclerView = findViewById(R.id.userSpotifyPlaylistRecyclerView)
        try {
            this.supportActionBar?.hide()
        } catch (e: NullPointerException) {
            Log.e(SignInActivity.TAG, "Exception: ", e)
        }
        if (MainActivity.accessToken.isNullOrEmpty()) {
            Log.d(TAG, "Failed to get token.")
            val mainActivityIntent = Intent(this@SpotifyUserPlaylistActivity, MainActivity::class.java)
            startActivity(mainActivityIntent)
        } else
            Log.d(TAG, "Token received: ${MainActivity.accessToken}")
        getSpotifyPlaylist()
    }

    private fun getSpotifyPlaylist() {
        try {
            doAsync {
                // Get current user Spotify playlist
                SpotifyManager().fetchMySpotifyPlaylist(
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
                        userSpotifyPlaylistRecyclerView.adapter = playListAdapter
                        userSpotifyPlaylistRecyclerView.layoutManager =
                            LinearLayoutManager(applicationContext)
                    }
                }
            }
        } catch (exception: Exception) {
            Log.e(TAG, "Fetching Spotify playlist failed", exception)
            runOnUiThread {
                Toast.makeText(
                    this@SpotifyUserPlaylistActivity,
                    "Failed to retrieve Spotify playlist",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}


