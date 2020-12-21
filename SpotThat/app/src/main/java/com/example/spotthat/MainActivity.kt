package com.example.spotthat

import android.content.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.widget.Button
import android.widget.SearchView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.spotify.sdk.android.auth.*

class MainActivity : AppCompatActivity() {
    // Debugger
    companion object {
        const val TAG = "MainActivity"
        var globalMyPlaylist = mutableListOf<SpotifyPlaylist>()
        var accessToken: String = ""
    }

    // Spotify API
    private lateinit var CLIENT_ID: String
    private lateinit var REDIRECT_URI: String
    private val REQUEST_CODE = 10

    // UI
    private lateinit var spotMapButton: Button
    private lateinit var myPlaylistRecyclerView: RecyclerView
    private lateinit var addYourSpotifyPlaylistButton: Button
    private lateinit var termSearch: SearchView
    private lateinit var quickSearchButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // SPOTIFY API
        CLIENT_ID = getString(R.string.spotify_client_id) // Spotify Developer Account a project
        REDIRECT_URI =
            getString(R.string.spotify_callback) // set to http://localhost:8888/callback //Make sure to whitelist
        connectSpotify()

        // UI
        // Populate lateinit var
        addYourSpotifyPlaylistButton = findViewById(R.id.addYourSpotifyPlaylistButton)
        myPlaylistRecyclerView = findViewById(R.id.userPlaylistRecyclerView)
        spotMapButton = findViewById(R.id.spotMapButton)
        termSearch = findViewById(R.id.termSearch)
        quickSearchButton = findViewById(R.id.quickSearchButton)
        // Get shared preferences
        val sharedPreferences: SharedPreferences =
            getSharedPreferences("SpotThat", Context.MODE_PRIVATE)

        setTitle(R.string.MySpotThat)
        // UI Logic
        // Request Spotify Permission
        addYourSpotifyPlaylistButton.setOnClickListener {
            val intent = Intent(this@MainActivity, SpotifyUserPlaylistActivity::class.java)
            startActivity(intent)
        }

        val savedQuery = sharedPreferences.getString("SAVED_QUERY", "")

        if (accessToken.isNullOrEmpty()) {
            termSearch.isEnabled = false
        }
        //load previous query
        termSearch.setQuery(savedQuery, false)

        //listen input change termSearch
        termSearch.setOnSearchClickListener {
            startActivity(
                Intent(
                    applicationContext,
                    ResultsActivity::class.java
                )
                    .putExtra("quickSearchQuery", termSearch.query.toString())
            )
            sharedPreferences.edit().putString("SAVED_QUERY", termSearch.query.toString()).apply()
        }
        termSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                //Save search
                startActivity(
                    Intent(
                        applicationContext,
                        ResultsActivity::class.java
                    ).putExtra("quickSearchQuery", termSearch.query.toString())
                )
                sharedPreferences.edit().putString("SAVED_QUERY", query).apply()
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                quickSearchButton.isEnabled = !termSearch.query.isBlank()
                if (termSearch.query.isBlank()) {
                    sharedPreferences.edit().putString("SAVED_QUERY", "").apply()
                } else {
                    sharedPreferences.edit().putString("SAVED_QUERY", newText).apply()

                }
                return quickSearchButton.isEnabled
            }
        })
        quickSearchButton.isEnabled = savedQuery!!.isNotBlank()

        quickSearchButton.setOnClickListener {
            startActivity(
                Intent(
                    applicationContext,
                    ResultsActivity::class.java
                ).putExtra("quickSearchQuery", termSearch.query.toString())
            )
            sharedPreferences.edit().putString("SAVED_QUERY", termSearch.query.toString()).apply()
        }

        spotMapButton.setOnClickListener {
            val intent = Intent(this, SpotMapActivity::class.java)
            startActivity(intent)
        }

        fetchMyPlaylist()
    }

    private fun fetchMyPlaylist() {
        SpotifyManager().fetchMyFirebasePlaylist { myPlaylist: MutableList<SpotifyPlaylist> ->
            globalMyPlaylist = myPlaylist
            val playlistAdapter = SpotifyPlaylistAdapter(myPlaylist, false, false, false,"false", )
            runOnUiThread {
                myPlaylistRecyclerView.adapter = playlistAdapter
                myPlaylistRecyclerView.layoutManager = LinearLayoutManager(this@MainActivity)

            }
        }
    }

    private fun connectSpotify() {
        var builder =
            AuthorizationRequest.Builder(CLIENT_ID, AuthorizationResponse.Type.TOKEN, REDIRECT_URI)
        // request setOfPermissions
        val scopes: Array<String> = arrayOf(
            "user-read-email",
            "playlist-read-private",
            "playlist-read-collaborative",
            "streaming",
            "playlist-modify-public",
        )
        var request = builder.setScopes(scopes).build()
        AuthorizationClient.openLoginActivity(this, REQUEST_CODE, request)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE) {
            var response = AuthorizationClient.getResponse(resultCode, data)
            when (response.type) {
                AuthorizationResponse.Type.TOKEN -> {
                    accessToken = response.accessToken
                    Log.d(TAG, "Token received: $accessToken")

                }
                AuthorizationResponse.Type.ERROR -> {
                    Log.e(TAG, "Token failed: ${response.error}")
                    Toast.makeText(this, "Failed to get your Spotify permission", Toast.LENGTH_LONG)
                        .show()
                }
            }
        }
    }

    public override fun onResume() {
        super.onResume()

        // Playlist update
        val intentFilterMyPlaylistUpdate = IntentFilter("MYPLAYLIST_UPDATE")
        // Listen for change in myPlaylist
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                fetchMyPlaylist()
                Log.d(TAG, "updated myPlaylist")
            }
        }
        this.registerReceiver(receiver, intentFilterMyPlaylistUpdate)


    }



    private fun initializeMyFakePlaylist() {
        val fakePlaylist = SpotifyManager().getFakeSpotifyPlayList()
        val playListAdapter = SpotifyPlaylistAdapter(fakePlaylist, false, false, false,"false")
        myPlaylistRecyclerView.adapter = playListAdapter
        myPlaylistRecyclerView.layoutManager = LinearLayoutManager(this@MainActivity)
    }
}