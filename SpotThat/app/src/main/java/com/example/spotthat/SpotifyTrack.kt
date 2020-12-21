package com.example.spotthat

import java.io.Serializable

data class SpotifyTrack(
    val name: String,
    val popularity: Int,
    val preview_url: String, //30 Second preview
    var uri: String, //Spotify URI
    val external_url: String //Spotify: URL:String
): Serializable  {
   constructor() : this ("", 0,"","","")
}
