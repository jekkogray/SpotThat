package com.example.spotthat

import android.util.Log
import android.widget.Button
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import org.jetbrains.anko.doAsync
import org.json.JSONObject


class SpotifyManager {
    companion object {
        const val TAG = "SpotifyManager"
    }

    val firebaseDatabase = FirebaseDatabase.getInstance()
    val okHttpClient: OkHttpClient
    val firebaseAuth = FirebaseAuth.getInstance()

    // An init block allows us to perform extra logic during instance creation (similar to having
    // extra logic in a constructor)
    init {
        val builder = OkHttpClient.Builder()
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        builder.addInterceptor(loggingInterceptor)

        okHttpClient = builder.build()
    }



    fun fetchUserSpottedFirebasePlaylist(state: String, callback: (MutableList<SpotifyPlaylist>) -> Unit) {
        val userSpottedPlaylist = mutableListOf<SpotifyPlaylist>()
        val playlistDatabaseReference =
            firebaseDatabase.getReference("spotMap/${state}")
        playlistDatabaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach { spotifyPlaylistData ->
                    val spotifyPlaylist =
                        spotifyPlaylistData.getValue(SpotifyPlaylist::class.java)
                    if (spotifyPlaylist != null) {
                        Log.d(
                            TAG,
                            "Fetched $state user spotted playlists from the Firebase database: ${spotifyPlaylist.name}"
                        )
                        userSpottedPlaylist.add(spotifyPlaylist)
                    }
                }
                callback.invoke(userSpottedPlaylist)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(
                    TAG,
                    "Error fetching userSpotted playlists from Firebase.",
                    error.toException()
                )
            }
        })
    }


    fun fetchMyFirebasePlaylist(callback: (MutableList<SpotifyPlaylist>) -> Unit) {
        val myPlaylist = mutableListOf<SpotifyPlaylist>()
        val userPlaylistDatabaseReference =
            firebaseDatabase.getReference("myPlaylists/${firebaseAuth.currentUser?.uid}")
            userPlaylistDatabaseReference.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.children.forEach { spotifyPlaylistData ->
                        val spotifyPlaylist =
                            spotifyPlaylistData.getValue(SpotifyPlaylist::class.java)
                        if (spotifyPlaylist != null) {
                            Log.d(
                                TAG,
                                "fetched from the Firebase database: ${spotifyPlaylist.name}"
                            )
                            myPlaylist.add(spotifyPlaylist)
                        }
                    }
                    callback.invoke(myPlaylist)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(
                        TAG,
                        "Error fetching Spotify playlist from Firebase.",
                        error.toException()
                    )
                }
            })
    }

    fun quickSearchPlaylist(searchTerm: String, accessToken: String, callback:(MutableList<SpotifyPlaylist>) -> Unit){
        val okHttpClient: OkHttpClient
        val builder = OkHttpClient.Builder()
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        builder.addInterceptor(loggingInterceptor)

        okHttpClient = builder.build()
        val request = Request.Builder()
            .url("https://api.spotify.com/v1/search?q=${searchTerm}&type=playlist&fields=items.description, items.external_urls(spotify),  items.images(url), items.name, items.id, items.owner(display_name), items.tracks(href), items.tracks(total),  items.owner.external_urls(spotify), items.uri")
            .header("Authorization", "Bearer ${accessToken}")
            .get()
            .build()
        doAsync{}
        // "Execute" the request (.execute will block the current thread until the server replies with a response)
        val response = okHttpClient.newCall(request).execute()

        val fetchedSpotifyPlaylists = mutableListOf<SpotifyPlaylist>()
        var responseString = response.body?.string()

        // If the response was successful (e.g. status code was a 200) AND the server sent us back
        if (response.isSuccessful && !responseString.isNullOrBlank()) {
            Log.d(SpotifyUserPlaylistActivity.TAG, "Response is successful!")
            // Set up for parsing the JSON response from the root element
            val json = JSONObject(responseString)

            // List of playlists
            val items = json.getJSONObject("playlists").getJSONArray("items")

            // Go to the array of playlists which we will return at the end.
            for (i in 0 until items.length()) {
                val currentPlaylist = items.getJSONObject(i)
                val description = currentPlaylist.getString("description")
                val external_urls_spotify =
                    currentPlaylist.getJSONObject("external_urls").getString("spotify")
                val image_url =
                    currentPlaylist.getJSONArray("images").getJSONObject(0).getString("url")
                val id = currentPlaylist.getString("id")
                val name = currentPlaylist.getString("name")
                val owner_display_name =
                    currentPlaylist.getJSONObject("owner").getString("display_name")
                val owner_external_urls_spotify =
                    currentPlaylist.getJSONObject("owner").getJSONObject("external_urls")
                        .getString("spotify")
                val tracks_href_get = currentPlaylist.getJSONObject("tracks").getString("href")
                val tracks_total = currentPlaylist.getJSONObject("tracks").getInt("total")

                val spotifyPlaylist = SpotifyPlaylist(
                    description,
                    external_urls_spotify,
                    image_url,
                    id,
                    name,
                    owner_display_name,
                    owner_external_urls_spotify,
                    tracks_href_get,
                    tracks_total
                )
                fetchedSpotifyPlaylists.add(spotifyPlaylist)
            }
        } else {
            Log.d(TAG, "fetchAndUploadMyPlaylist() failed.")
        }
        callback.invoke(fetchedSpotifyPlaylists)
    }

    fun fetchMySpotifyPlaylist(accessToken: String, callback:(MutableList<SpotifyPlaylist>) -> Unit){
        val okHttpClient: OkHttpClient
        val builder = OkHttpClient.Builder()
//        val loggingInterceptor = HttpLoggingInterceptor()
//        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
//        builder.addInterceptor(loggingInterceptor)

        okHttpClient = builder.build()
        val request = Request.Builder()
            .url("https://api.spotify.com/v1/me/playlists?fields=items.description, items.external_urls(spotify),  items.images(url), items.name, items.id, items.owner(display_name), items.tracks(href), items.tracks(total),  items.owner.external_urls(spotify), items.uri")
            .header("Authorization", "Bearer ${accessToken}")
            .get()
            .build()
        doAsync{}
        // "Execute" the request (.execute will block the current thread until the server replies with a response)
        val response = okHttpClient.newCall(request).execute()

        val fetchedSpotifyPlaylists = mutableListOf<SpotifyPlaylist>()
        var responseString = response.body?.string()

        // If the response was successful (e.g. status code was a 200) AND the server sent us back
        if (response.isSuccessful && !responseString.isNullOrBlank()) {
            Log.d(SpotifyUserPlaylistActivity.TAG, "Response is successful!")
            // Set up for parsing the JSON response from the root element
            val json = JSONObject(responseString)

            // List of playlists
            val items = json.getJSONArray("items")

            // Go to the array of playlists which we will return at the end.
            for (i in 0 until items.length()) {
                val currentPlaylist = items.getJSONObject(i)
                val description = currentPlaylist.getString("description")
                val external_urls_spotify =
                    currentPlaylist.getJSONObject("external_urls").getString("spotify")
                val image_url =
                    currentPlaylist.getJSONArray("images").getJSONObject(0).getString("url")
                val id = currentPlaylist.getString("id")
                val name = currentPlaylist.getString("name")
                val owner_display_name =
                    currentPlaylist.getJSONObject("owner").getString("display_name")
                val owner_external_urls_spotify =
                    currentPlaylist.getJSONObject("owner").getJSONObject("external_urls")
                        .getString("spotify")
                val tracks_href_get = currentPlaylist.getJSONObject("tracks").getString("href")
                val tracks_total = currentPlaylist.getJSONObject("tracks").getInt("total")

                val spotifyPlaylist = SpotifyPlaylist(
                    description,
                    external_urls_spotify,
                    image_url,
                    id,
                    name,
                    owner_display_name,
                    owner_external_urls_spotify,
                    tracks_href_get,
                    tracks_total
                )
                fetchedSpotifyPlaylists.add(spotifyPlaylist)
            }
        } else {
            Log.d(TAG, "fetchMySpotifyPlaylist() failed.")
        }
        callback.invoke(fetchedSpotifyPlaylists)
    }

    fun getFakeSpotifyPlayList(): MutableList<SpotifyPlaylist> {
        return mutableListOf<SpotifyPlaylist>(
            SpotifyPlaylist(
                "One of the earlier memories of songs I heard.",
                "https://open.spotify.com/playlist/04hxP3JLsZl9pjJ30sdYce",
                "https://mosaic.scdn.co/640/ab67616d0000b273656f6f77c55dcdecf8c26f75ab67616d0000b2737da53b49b2eb7b7a695d9ccaab67616d0000b2739288bc6a8c1f2fca8b5eddf9ab67616d0000b273dd69d8ba2268cc625504b24f",
                "04hxP3JLsZl9pjJ30sdYce",
                "3 Years Old",
                "Jekko Gray",
                "https://open.spotify.com/user/12162736454",
                "https://api.spotify.com/v1/playlists/0jqJhGcCNPTXAZ8eFZqJ1G/tracks",
                7
            ),
            SpotifyPlaylist(
                "One of the earlier memories of songs I heard.",
                "https://open.spotify.com/playlist/04hxP3JLsZl9pjJ30sdYce",
                "https://mosaic.scdn.co/640/ab67616d0000b273656f6f77c55dcdecf8c26f75ab67616d0000b2737da53b49b2eb7b7a695d9ccaab67616d0000b2739288bc6a8c1f2fca8b5eddf9ab67616d0000b273dd69d8ba2268cc625504b24f",
                "04hxP3JLsZl9pjJ30sdYce",
                "3 Years Old",
                "Jekko Gray",
                "https://open.spotify.com/user/12162736454",
                "https://api.spotify.com/v1/playlists/0jqJhGcCNPTXAZ8eFZqJ1G/tracks",
                7
            ),
        )
    }
}
