package com.example.spotthat

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import org.jetbrains.anko.doAsync
import java.lang.NullPointerException
import java.util.Calendar

class SpotMapActivity : AppCompatActivity(), OnMapReadyCallback {
    companion object {
        const val TAG = "SpotMapActivity"
   }
    private lateinit var confirm: Button
    private lateinit var currentLocation: ImageButton
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var locationProvider: FusedLocationProviderClient
    private var currentAddress: Address? = null
    private lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_spot_map)
        // Remove support action bar
        try {
            this.supportActionBar?.hide()
        } catch (e: NullPointerException) {
            Log.e(SignInActivity.TAG, "Exception: ", e)
        }
        firebaseAuth = FirebaseAuth.getInstance()
        locationProvider = LocationServices.getFusedLocationProviderClient(this)
        currentLocation = findViewById(R.id.current_location)
        currentLocation.setOnClickListener {
            checkLocationPermission()
        }

        confirm = findViewById(R.id.confirm)
        confirm.setOnClickListener {
            if (currentAddress != null) {
                val intent = Intent(this@SpotMapActivity, SpotThatActivity::class.java)
                intent.putExtra("address", currentAddress)
                startActivity(intent)
            }
        }
        confirm.isEnabled = false
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }


    override fun onMapReady(googleMap: GoogleMap) {
        try {
            val success: Boolean
            val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            // Night time
            if (hour < 6 || hour > 18) {
                success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                        this,
                        R.raw.darkblue_google_maps_style
                    )
                )
            }
            // Daylight
            else {
                success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                        this,
                        R.raw.teal_google_maps_style
                    )
                )
            }
            if (!success)
                Log.e(TAG, "Style parsing failed")
        } catch (e: Exception) {
            Log.d(TAG, "Style not found", e)
        }
        mMap = googleMap
        mMap.setOnMapLongClickListener { coords: LatLng ->
            doGeocoding(coords)
        }
    }

    private fun checkLocationPermission() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Check permission: Permission already granted")

            // User can already granted the Location Permission
            // (no prompted needed)
            useCurrentLocation()
        } else {
            Log.d(TAG, "Check permission: Permission not granted")

            // We don't have the location permission
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 200)
        }
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            super.onLocationResult(result)

            // Make sure we have unregistered for location updates (e.g. allow GPS to turn off)
            locationProvider.removeLocationUpdates(this)

            val location = result.lastLocation
            val coords = LatLng(location.latitude, location.longitude)
            doGeocoding(coords)
        }
    }

    @SuppressLint("MissingPermission")
    private fun useCurrentLocation() {
        // Use default location retrieval settings (slides have other examples)
        val locationRequest = LocationRequest.create()

        locationProvider.requestLocationUpdates(
            locationRequest,
            locationCallback,
            null
        )
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 200) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // User was prompted for the permission and granted
                Log.d(TAG, "Permission result: Permission granted")
                useCurrentLocation()
            } else {
                // We were denied the location permission
                if(shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    // Regular denial (user can still be re-prompted)
                    Log.d(TAG, "Permission result: Permission denied (regular)")

                } else {
                    // 'Do not ask me again' denial (user cannot be re-prompted)
                    Log.d(TAG, "Permission result: Permission denied (do not re-prompt)")

                    Toast.makeText(
                        this,
                        "To use this feature, go into your Settings and enable the Location permission",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
    // TODO:Request permission to use Spotify

    private fun doGeocoding(coords: LatLng) {
        mMap.clear()

        // Geocoding needs to be performed on a background thread, otherwise we run the risk
        // of freezing the app (the UI Thread) if it takes too long.
        doAsync {
            val geocoder = Geocoder(this@SpotMapActivity)
            val results: List<Address> = try {
                geocoder.getFromLocation(
                    coords.latitude,
                    coords.longitude,
                    10
                )
            } catch (exception: Exception) {
                Log.e("MapsActivity", "Geocoding failed", exception)
                listOf()
            }

            // Now that we have results, let's hop back onto the UI Thread since we can only perform
            // UI events (like dropping map markers) if we're on the UI Thread.
            runOnUiThread {
                if (results.isNotEmpty()) {
                    // Just grab the first result - we're not super concerned with geocoding accuracy here
                    // and the first result may be the one with the "highest confidence"
                    val firstResult: Address = results.first()
                    val streetAddress: String = firstResult.getAddressLine(0)

                    mMap.addMarker(
                        MarkerOptions()
                            .position(coords)
                            .title(streetAddress)
                    )

                    mMap.animateCamera(
                        CameraUpdateFactory.newLatLng(coords)
                    )

                    updateConfirmButton(firstResult)
                } else {
                    Log.e("MapsActivity", "Geocoding failed or returned no results")
                    Toast.makeText(
                        this@SpotMapActivity,
                        "No results for location!",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

    }

    /**
     * Flips the button color from red --> green and updates the icon
     */
    private fun updateConfirmButton(address: Address) {
        val greenColor = getColor(R.color.buttonGreen)
        val checkIcon = getDrawable(R.drawable.ic_baseline_check_24)

        confirm.setBackgroundColor(greenColor)
        confirm.setCompoundDrawablesWithIntrinsicBounds(checkIcon, null, null, null)
        confirm.text = address.getAddressLine(0)
        confirm.isEnabled = true

        currentAddress = address
    }
}
