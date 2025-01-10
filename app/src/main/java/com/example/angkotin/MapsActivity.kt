package com.example.angkotin

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.core.app.ActivityCompat
import com.example.angkotin.databinding.ActivityMapsBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener
import com.google.android.gms.maps.GoogleMap.OnMyLocationClickListener
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.material.animation.Positioning
import com.google.maps.DirectionsApiRequest
import com.google.maps.GeoApiContext
import com.google.android.gms.maps.model.LatLng as GmsLatLng


internal class MapsActivity : AppCompatActivity(),
    OnMapReadyCallback ,
    OnMyLocationButtonClickListener,
    OnMyLocationClickListener{

    private lateinit var locationCallback: LocationCallback
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var directionsClient: DirectionsApiRequest
    private var isMapReady = false
    private val apiKey = "AIzaSyC0h2qsxAwT8x3bgExbSGoIEaeydTuj6II"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        directionsClient = DirectionsApiRequest(GeoApiContext.Builder().apiKey(apiKey).build())
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Set the top bar content
        val composeTopBar = findViewById<ComposeView>(R.id.compose_top_bar)
        composeTopBar.setContent {
            var searchText by remember { mutableStateOf("") }
            MyTopBar(
                searchText = searchText,
                onSearchTextChange = { newText -> searchText = newText }
            )
        }

        // Set the routes button content
        val composeRoutesButton = findViewById<ComposeView>(R.id.compose_routes_button)
        composeRoutesButton.setContent {
            RoutesButton(onClick = {
                // Start ListRoute activity
                val intent = Intent(this@MapsActivity, ListRouteActivity::class.java)
                startActivity(intent)
            })
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            requestLocationPermission()
            Log.d(
                "LOCATION_INFO_1",
                "LOCATION NOT GRANTED Going to requestLocationPermission()"
            )
        } else {
            accessLocation()
            Log.d("LOCATION_INFO_1",
                "LOCATION ALREADY GRANTED Going to accessLocation()")
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permission ->
        val fineLocationGranted = permission[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permission[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        if (fineLocationGranted || coarseLocationGranted) {
            //izin telah didapat, lanjutkan fungsi
            accessLocation()
            Log.d(
                "LOCATION_INFO_2",
                "LOCATION ALREADY GRANTED Going to accessLocation()"
            )

        } else {
            //izin ditolak, memunculkan pesan
            Toast.makeText(
                this,
                "Izin lokasi ditolak. Mohon izinkan untuk mendapat pengalaman yang lebih baik",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun requestLocationPermission() {
        //meminta izin fine location dan coarse location
        requestPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    private fun accessLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestLocationPermission()
            Log.d(
                "LAST_LOCATION_INFO",
                "LOCATION NOT GRANTED GOING BACK to requestLocationPermission"
            )
        }else {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        //Request location update
                        val locationRequest =
                            LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
                                .setIntervalMillis(1000).build()
                        locationCallback = object : LocationCallback() {
                            override fun onLocationResult(locationResult: LocationResult) {
                                for (loc in locationResult.locations) {
                                    Log.d(
                                        "LOCATION_UPDATE",
                                        "LAT: ${loc.latitude}, LONG: ${loc.longitude}"
                                    )
                                }
                            }
                        }
                        fusedLocationClient.requestLocationUpdates(
                            locationRequest,
                            locationCallback,
                            null
                        )
                        Log.d("LOCATION_INFO_GMAP",
                            "LOCATION GRANTED Initializing Map Component")
                        mMap.isMyLocationEnabled = true
                        mMap.setOnMyLocationClickListener(this)
                        mMap.setOnMyLocationButtonClickListener(this)
                    }
                }
        }

    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        isMapReady = true
        val malangLatLng = GmsLatLng(-7.966560523100535, 112.6325158401338)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(malangLatLng, 13f))
        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style))
        mMap.uiSettings.isZoomControlsEnabled = false
        mMap.uiSettings.isMyLocationButtonEnabled = false
    }

    override fun onMyLocationClick(location: Location) {
        Toast.makeText(this, "Current location:\n$location", Toast.LENGTH_LONG)
        .show()
    }

    override fun onMyLocationButtonClick(): Boolean {
        Positioning(0, 50F,50F)
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT)
            .show()
        return false
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
}