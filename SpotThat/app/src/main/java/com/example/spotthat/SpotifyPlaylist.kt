package com.example.spotthat

import java.io.Serializable

data class SpotifyPlaylist(
        val description: String,
        val external_urls_spotify: String,
        val image_url: String,
        val id: String,         // Unique Spotify ID
        val name: String,
        val owner_display_name: String,
        val owner_external_urls_spotify: String,
        val tracks_href_get: String, // API Request References
        val tracks_total: Int
) : Serializable {
    constructor() : this("", "", "", "", "", "", "", "", 0)
}

