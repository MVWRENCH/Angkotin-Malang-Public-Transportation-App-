package com.example.angkotin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng as GmsLatLng
import com.example.angkotin.databinding.ActivityMapsBinding
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.DirectionsApiRequest
import com.google.maps.GeoApiContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import com.example.angkotin.data.RouteFactory
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.PolyUtil
import com.google.maps.model.TravelMode
import kotlin.collections.isNotEmpty
import kotlin.collections.toTypedArray

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var directionsClient: DirectionsApiRequest
    private var isMapReady = false
    private val apiKey = "AIzaSyC0h2qsxAwT8x3bgExbSGoIEaeydTuj6II"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        directionsClient = DirectionsApiRequest(GeoApiContext.Builder().apiKey(apiKey).build())

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
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        isMapReady = true
        val malangLatLng = GmsLatLng(-7.966560523100535, 112.6325158401338)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(malangLatLng, 13f))
        googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style))
        mMap.uiSettings.isZoomControlsEnabled = false
    }

    private fun drawRoute(routeId: String) {
        if (isMapReady) {
            val route = RouteFactory.getRoute(routeId)

            if (route != null) {
                val context = GeoApiContext.Builder()
                    .apiKey(apiKey)
                    .build()

                val origin = GmsLatLng(route.origin.latitude, route.origin.longitude)
                val destination = GmsLatLng(route.destination.latitude, route.destination.longitude)
                val waypoints = route.waypoints.map { com.google.maps.model.LatLng(it.latitude, it.longitude) }


                // Add markers for origin and destination
                mMap.addMarker(MarkerOptions().position(origin).title("Origin"))
                mMap.addMarker(MarkerOptions().position(destination).title("Destination"))

                // Draw route on the map
                CoroutineScope(Dispatchers.Main).launch {
                    try {
                        val result = directionsClient
                            .origin(com.google.maps.model.LatLng(origin.latitude, origin.longitude)) // Convert to com.google.maps.model.LatLng
                            .destination(com.google.maps.model.LatLng(destination.latitude, destination.longitude)) // Convert to com.google.maps.model.LatLng
                            .waypoints(*waypoints.map { com.google.maps.model.LatLng(it.lat, it.lng) }.toTypedArray()) // Convert waypoints to com.google.maps.model.LatLng
                            .mode(TravelMode.DRIVING)
                            .await()

                        if (result.routes.isNotEmpty()) {
                            val apiRoute = result.routes[0]
                            val encodedPath = apiRoute.overviewPolyline.encodedPath
                            val decodedPath = PolyUtil.decode(encodedPath)

                            val polylineOptions = PolylineOptions()
                                .addAll(decodedPath.map { GmsLatLng(it.latitude, it.longitude) })
                                .width(10f)
                                .color(Color.Blue.hashCode()) // Use Android color
                            mMap.addPolyline(polylineOptions)

                            // Adjust camera to fit the route
//                            val bounds = PolyUtil.getBounds(encodedPath)
//                            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100)) // Add padding
                        } else {
                            Toast.makeText(this@MapsActivity, "No route found", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this@MapsActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Invalid route ID", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Map is not ready yet", Toast.LENGTH_SHORT).show()
        }
    }

}