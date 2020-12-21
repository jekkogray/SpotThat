package com.example.spotthat

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Address
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.cardview.widget.CardView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.FirebaseDatabase
import org.jetbrains.anko.doAsync

class SpotThatActivity : AppCompatActivity() {
    companion object {
        const val TAG = "SpotThatActivity"
        var userSpottedPlaylist: MutableList<SpotifyPlaylist> = mutableListOf()
        var myPlaylistAtSpotThatActivity: MutableList<SpotifyPlaylist> = mutableListOf()
    }

    private lateinit var firebaseDatabase: FirebaseDatabase

    private var spotThatRecommendedPlaylist: MutableList<SpotifyPlaylist> = mutableListOf()

    // UI
    private lateinit var userSpottedPlaylistRecyclerView: RecyclerView
    private lateinit var spotThatRecommendedPlaylistRecyclerView: RecyclerView
    private lateinit var mySpottedPlaylistRecyclerView: RecyclerView
    private lateinit var mySpottedPlaylistCardView: CardView
    private lateinit var spotMyPlaylistButton: Button
    private lateinit var address: Address

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_spot_that)

        // Get address from intent
        val intent = getIntent()
        address = intent.getParcelableExtra<Address>("address")!!
        setTitle(if (address.countryCode=="US")  address.adminArea else address.countryName)

        // initialize firebase
        firebaseDatabase = FirebaseDatabase.getInstance()

        // UI
        // Populate lateinit var
        userSpottedPlaylistRecyclerView = findViewById(R.id.userSpottedPlaylistRecyclerView)
        spotThatRecommendedPlaylistRecyclerView = findViewById(R.id.spotThatRecommendedPlaylistRecyclerView)

        mySpottedPlaylistCardView = findViewById(R.id.mySpottedPlaylistCardView)
        mySpottedPlaylistRecyclerView = findViewById(R.id.mySpottedPlaylistRecyclerView)
        spotMyPlaylistButton = findViewById(R.id.spotMyPlaylistButton)

        // UI Logic
        mySpottedPlaylistCardView.isVisible = false
        spotMyPlaylistButton.setOnClickListener {
            mySpottedPlaylistCardView.isVisible = !mySpottedPlaylistCardView.isVisible
            if (mySpottedPlaylistCardView.isVisible) {
                spotMyPlaylistButton.text = getString(R.string.closeMyPlaylist)
            } else {
                spotMyPlaylistButton.text =getString(R.string.spotMyPlaylist)
            }
        }
        populateUserSpottedPlaylist(address)
        populateRecommendedPlaylist(address)

        loadMySpottedPlaylist()
    }

    private fun populateRecommendedPlaylist(address: Address) {
        doAsync {
            SpotifyManager().quickSearchPlaylist(
                if (address.countryCode=="US")  address.adminArea else address.countryName,
                MainActivity.accessToken
            ) { recommendedPlaylist ->
                spotThatRecommendedPlaylist = recommendedPlaylist
                runOnUiThread {
                    spotThatRecommendedPlaylistRecyclerView.adapter = SpotifyPlaylistAdapter(
                        recommendedPlaylist,
                        spotThisIsVisible = true,
                        spotHereIsVisible = false,
                        false,
                        state = "false",
                    )
                    spotThatRecommendedPlaylistRecyclerView.layoutManager =
                        LinearLayoutManager(
                            this@SpotThatActivity,
                            LinearLayoutManager.HORIZONTAL,
                            false
                        )
                }
            }
        }
    }

    private fun populateUserSpottedPlaylist(address: Address) {
        doAsync {
            SpotifyManager().fetchUserSpottedFirebasePlaylist(
                if (address.countryCode=="US")  address.adminArea else address.countryName
            ) { userPlaylist ->
                userSpottedPlaylist = userPlaylist
                runOnUiThread {
                    userSpottedPlaylistRecyclerView.adapter = SpotifyPlaylistAdapter(
                        userPlaylist,
                        spotThisIsVisible = true,
                        spotHereIsVisible = false,
                        true,
                        if (address.countryCode=="US")  address.adminArea else address.countryName,
                    )
                    userSpottedPlaylistRecyclerView.layoutManager =
                        LinearLayoutManager(
                            this@SpotThatActivity,
                        )
                }
            }
        }
    }

    private fun loadMySpottedPlaylist() {
        doAsync {
            SpotifyManager().fetchMyFirebasePlaylist { playlists ->
                myPlaylistAtSpotThatActivity = playlists
                val mySpottedPlaylistAdapter =
                    SpotifyPlaylistAdapter(
                        playlists,
                        false,
                        true,
                        false,
                        if (address.countryCode=="US")  address.adminArea else address.countryName,
                    )
                mySpottedPlaylistRecyclerView.adapter = mySpottedPlaylistAdapter
                mySpottedPlaylistRecyclerView.layoutManager =
                    LinearLayoutManager(this@SpotThatActivity)

            }
        }

    }

    public override fun onResume() {
        super.onResume()
        // myPlaylist update
        val intentFilterMyPlaylistUpdate = IntentFilter("MYPLAYLIST_UPDATE")

        // Listen for change in myPlaylist
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                loadMySpottedPlaylist()
            }
        }

        this.registerReceiver(receiver, intentFilterMyPlaylistUpdate)

        // Playlist update
        val intentFilterRecommendedPlaylistUpdate = IntentFilter("PLAYLIST_UPDATE")

        // Listen for delete change in playlist
        val receiver2 = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                populateRecommendedPlaylist(address)
                populateUserSpottedPlaylist(address)
            }
        }
        this.registerReceiver(receiver2, intentFilterRecommendedPlaylistUpdate)

        // Playlist update
        val intentFilterUserSpottedPlaylistUpdate = IntentFilter("SPOTHERE")
        // Listen for change in myPlaylist

        val receiver3 = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                populateUserSpottedPlaylist(address)
                Log.d(MainActivity.TAG, "updated UserSpottedPlaylists")
            }
        }
        this.registerReceiver(receiver3, intentFilterUserSpottedPlaylistUpdate)
    }
}