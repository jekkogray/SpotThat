package com.example.spotthat

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso

class SpotifyPlaylistAdapter(
    private var playlist: MutableList<SpotifyPlaylist>,
    private val spotThisIsVisible: Boolean,
    private val spotHereIsVisible: Boolean,
    internal val userSpottedPlaylistRemoveIsVisible: Boolean,
    private val state: String,
) :
    RecyclerView.Adapter<SpotifyPlaylistAdapter.ViewHolder>() {
    companion object {
        const val TAG = "SpotifyPlaylistAdapter"
    }

    private val firebaseDatabase = FirebaseDatabase.getInstance()
    private val firebaseAuth = FirebaseAuth.getInstance()

    /**
     * @property itemView the view that sets the context similar to setContentView()
     * Extends RecyclerView.Adapter<SpotifyPlaylistAdapter.ViewHolder>() this means the view is consisted of ViewHolder objects
     */
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val description: TextView = itemView.findViewById(R.id.description)
        val viewPlaylistButton: Button = itemView.findViewById(R.id.viewPlaylistButton)
        val playlistTitleTextView: TextView = itemView.findViewById(R.id.playlistTitleTextView)
        val playlistOwner: TextView = itemView.findViewById(R.id.playlistOwner)
        val thumbnail: ImageView = itemView.findViewById(R.id.thumbnail)
        val trackTotalTextView: TextView = itemView.findViewById(R.id.trackTotalTextView)
        val spotThisButton: Button = itemView.findViewById(R.id.spotThisButton)
        val spotHereButton: Button = itemView.findViewById(R.id.spotHereButton)
        val removePlaylistButton: ImageButton = itemView.findViewById(R.id.removePlaylistButton)
    }

    /**
     * @return a ViewHolder object from an inflated xml file. This is an object consisting of
     * one view --> one card view technically.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater: LayoutInflater = LayoutInflater.from(parent.context)
        val itemView: View = layoutInflater.inflate(R.layout.row_spotify_playlist, parent, false)
        return ViewHolder(itemView)
    }

    /**
     * @param holder this is the given ViewHolder object from above use as a context reference.
     * Allows for independent context for each holder.
     * @return loads data in a newsCard
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {


        // load data into a row
        val currentPlaylist = playlist[position]
        holder.description.text = currentPlaylist.description
        holder.playlistTitleTextView.text = currentPlaylist.name
        holder.playlistOwner.text = currentPlaylist.owner_display_name
        holder.trackTotalTextView.text = "${holder.trackTotalTextView.context.getString(R.string.trackTotal)} ${currentPlaylist.tracks_total}"
        holder.spotThisButton.isVisible = spotThisIsVisible
        holder.removePlaylistButton.isVisible = !spotThisIsVisible
        holder.spotThisButton.isEnabled = !MainActivity.globalMyPlaylist.contains(currentPlaylist)
        holder.spotHereButton.isEnabled =
            !SpotThatActivity.userSpottedPlaylist.contains(currentPlaylist)
        holder.playlistOwner.setOnClickListener {
            var intent =
                Intent(Intent.ACTION_VIEW, Uri.parse(currentPlaylist.owner_external_urls_spotify))
            it.context.startActivity(intent)
        }

        holder.viewPlaylistButton.setOnClickListener {
            var intent =
                Intent(Intent.ACTION_VIEW, Uri.parse(currentPlaylist.external_urls_spotify))
            it.context.startActivity(intent)
        }

        holder.playlistTitleTextView.setOnClickListener {
            var intent =
                Intent(Intent.ACTION_VIEW, Uri.parse(currentPlaylist.external_urls_spotify))
            it.context.startActivity(intent)
        }

        var playlistDatabaseReference =
            firebaseDatabase.getReference("myPlaylists/${firebaseAuth.currentUser?.uid}")

        Log.d(
            TAG,
            " Playlist -- ${currentPlaylist.name} exists: ${
                MainActivity.globalMyPlaylist.contains(currentPlaylist)
            }"
        )



        Log.d(TAG, "${MainActivity.globalMyPlaylist}")

        holder.spotThisButton.setOnClickListener {
            playlistDatabaseReference =
                firebaseDatabase.getReference("myPlaylists/${firebaseAuth.currentUser?.uid}")
            Log.d(TAG, "spotThisButton clicked")
            if (!MainActivity.globalMyPlaylist.contains(currentPlaylist)) {
                playlistDatabaseReference.child(currentPlaylist.id).setValue(currentPlaylist)
                // Add and disable button
                holder.spotThisButton.isEnabled = !holder.spotThisButton.isEnabled
                // Update playlist
                it.context.sendBroadcast(Intent("MYPLAYLIST_UPDATE"))
            } else
                Toast.makeText(
                    holder.itemView.context,
                    "You already have this in your Spotted playlist",
                    Toast.LENGTH_SHORT
                ).show()
        }

        // User Spotted Playlist
        if (userSpottedPlaylistRemoveIsVisible && SpotThatActivity.myPlaylistAtSpotThatActivity.contains(currentPlaylist)) {
            holder.removePlaylistButton.isVisible = true
            holder.removePlaylistButton.setOnClickListener { buttonView ->
                playlistDatabaseReference =
                    firebaseDatabase.getReference("spotMap/${state}")
                Log.d(TAG, "removePlaylistButton Clicked")
                playlistDatabaseReference.child(currentPlaylist.id)
                    .removeValue().addOnCompleteListener {
                        notifyDataSetChanged()
                        buttonView.context.sendBroadcast(Intent("PLAYLIST_UPDATE"))
                    }
            }
        }
        // My Playlist
        else {
            holder.removePlaylistButton.setOnClickListener { buttonView ->
                holder.removePlaylistButton.isVisible = true
                playlistDatabaseReference =
                    firebaseDatabase.getReference("myPlaylists/${firebaseAuth.currentUser?.uid}")
                Log.d(TAG, "removePlaylistButton Clicked")
                playlistDatabaseReference.child(currentPlaylist.id)
                    .removeValue().addOnCompleteListener {
                        notifyDataSetChanged()
                        buttonView.context.sendBroadcast(Intent("MYPLAYLIST_UPDATE"))
                    }
            }
        }

        if (spotHereIsVisible && state != "false" && !userSpottedPlaylistRemoveIsVisible) {
            holder.removePlaylistButton.isVisible = true
            holder.spotHereButton.isVisible = spotHereIsVisible
            // TODO Figure out a way to add to a list with a given location
            playlistDatabaseReference =
                firebaseDatabase.getReference("spotMap/$state")
            holder.spotHereButton.setOnClickListener {
                Log.d(TAG, "spotHereButton clicked")
                if (!SpotThatActivity.userSpottedPlaylist.contains(currentPlaylist)) {
                    playlistDatabaseReference.child(currentPlaylist.id)
                        .setValue(currentPlaylist)
                    // Add and disable button
                    holder.spotHereButton.isEnabled = !holder.spotHereButton.isEnabled
                    // Update playlist
                    it.context.sendBroadcast(Intent("SPOTHERE"))
                } else
                    Toast.makeText(
                        holder.itemView.context,
                        "You already spotted this in this location",
                        Toast.LENGTH_SHORT
                    ).show()
            }
        }


        // Update playlist
//            it.context.sendBroadcast(Intent("PLAYLIST_UPDATE"))

        // See the process in network and cache
        Picasso.get().setIndicatorsEnabled(true)
        // Create thumbnail
        Picasso.get()
            .load(currentPlaylist.image_url)
            .into(holder.thumbnail)
    }

    /**
     * This determines how much is going to be generated as a whole.
     * The adapter determines how much can be seen at the same time.
     * @return the size of list rendered
     */
    override fun getItemCount(): Int {
        return playlist.size
    }
}