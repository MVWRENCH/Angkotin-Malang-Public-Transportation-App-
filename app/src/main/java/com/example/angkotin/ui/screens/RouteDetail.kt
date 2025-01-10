package com.example.angkotin.ui.screens

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.isVisible
import androidx.navigation.NavController
import com.example.angkotin.R
import com.example.angkotin.data.Halte
import com.example.angkotin.data.Jalur
import com.example.angkotin.data.RouteEntity
import com.example.angkotin.data.RouteFactory
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.maps.DirectionsApiRequest
import com.google.maps.GeoApiContext
import com.google.maps.android.PolyUtil
import com.google.maps.android.SphericalUtil
import com.google.maps.model.TravelMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@SuppressLint("UseOfNonLambdaOffsetOverload")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Routedetailpage(navController: NavController, routeId: String) {
    val context = LocalContext.current
    val route = RouteFactory.getRoute(routeId) // Obtain the route details
    val mapView = remember { MapView(context) }
    val bottomSheetScaffoldState = rememberBottomSheetScaffoldState()
    val logoOffsetX by remember { mutableStateOf((-32).dp) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(
                        modifier = Modifier.fillMaxWidth(), // Make the Box take up full width
                        contentAlignment = Alignment.Center // Align the content in the center
                    ) {
                        // Route Logo
                        if (route != null) {
                            Image(
                                painter = painterResource(id = route.logo), // Replace with actual route logo
                                contentDescription = "Route Logo",
                                modifier = Modifier
                                    .size(70.dp) // Adjust the size of the logo
                                    .padding(end = 0.dp)
                                    .offset(x = logoOffsetX)// Add padding to the right of the logo
                            )
                        }
                    }

                },
                navigationIcon = {
                    IconButton(
                        onClick = { (navController.context as? Activity)?.finish() }, // Navigate back
                        modifier = Modifier
                            .padding(5.dp)
                            .size(50.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.Black // Set icon color to black
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White // Custom top bar background color
                )
            )
        }
    ) { paddingValues ->
        BottomSheetScaffold(
            scaffoldState = bottomSheetScaffoldState,
            sheetContent = {
                StreetNamesBottomSheet(route) // No extra drag bar here
            },
            sheetPeekHeight = 50.dp, // Minimum height when collapsed
            modifier = Modifier.padding(paddingValues)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Fullscreen map
                AndroidView(
                    factory = {
                        mapView.onCreate(null)
                        mapView.onResume()
                        mapView.getMapAsync { googleMap ->
                            setupMap(googleMap, context, route)
                            googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style))
                        }
                        mapView
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

// Function to fetch street names from 'halte' based on 'Jalur' data
fun getStreetNamesFromHalte(route: RouteEntity, callback: (List<String>) -> Unit) {
    val db = FirebaseFirestore.getInstance()

    // Fetch the 'Jalur' to get List_ID_Jalan (halte IDs)
    val jalurCollection = db.collection("Jalur")
    jalurCollection.document(route.id) // Assuming route.id corresponds to a document in 'Jalur'
        .get()
        .addOnSuccessListener { document ->
            val jalur = document.toObject(Jalur::class.java)
            val listIdJalan = jalur?.List_ID_Jalan?.split(",")?.map { it.trim() } ?: emptyList()

            // Now fetch the 'halte' details using the List_ID_Jalan
            val halteCollection = db.collection("Halte")
            val streetNames = mutableListOf<String>()

            listIdJalan.forEach { id ->
                halteCollection.document(id) // Assuming each 'id' corresponds to a document in 'Halte'
                    .get()
                    .addOnSuccessListener { halteDocument ->
                        val halte = halteDocument.toObject(Halte::class.java)
                        halte?.Jalan?.let { streetNames.add(it) }

                        // Once all street names are fetched, return the list
                        if (streetNames.size == listIdJalan.size) {
                            callback(streetNames)
                        }
                    }
            }
        }
        .addOnFailureListener { exception ->
            println("Error fetching route details: $exception")
            callback(emptyList()) // Return empty list on failure
        }
}

fun fetchRouteCoordinates(routeId: String, context: Context, callback: (LatLng?, LatLng?, List<LatLng>) -> Unit) {
    val db = FirebaseFirestore.getInstance()
    val jalurCollection = db.collection("Jalur")
    val halteCollection = db.collection("Halte")

    // Fetch the Jalur document based on routeId
    jalurCollection.document(routeId)
        .get()
        .addOnSuccessListener { jalurDoc ->
            val jalur = jalurDoc.toObject(Jalur::class.java)

            // Ensure we have the List_ID_Jalan
            val listIdJalan = jalur?.List_ID_Jalan?.split(",")?.map { it.trim() } ?: emptyList()

            val waypoints = mutableListOf<LatLng>()
            var origin: LatLng? = null
            var destination: LatLng? = null

            // For each Halte ID in List_ID_Jalan, fetch the coordinates
            listIdJalan.forEachIndexed { index, id ->
                halteCollection.document(id).get()
                    .addOnSuccessListener { halteDoc ->
                        val halte = halteDoc.toObject(Halte::class.java)
                        val coordinate = halte?.getLatLng()

                        // Assign first and last halte as origin and destination
                        if (coordinate != null) {
                            if (index == 0) origin = coordinate
                            if (index == listIdJalan.size - 1) destination = coordinate
                            waypoints.add(coordinate)
                        }

                        // If all haltes are processed, call the callback
                        if (waypoints.size == listIdJalan.size) {
                            callback(origin, destination, waypoints)
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.e("drawRoute", "Error fetching Halte: $exception")
                        callback(null, null, emptyList())
                    }
            }
        }
        .addOnFailureListener { exception ->
            Log.e("drawRoute", "Error fetching Jalur: $exception")
            callback(null, null, emptyList())
        }
}

@Composable
fun StreetNamesBottomSheet(route: RouteEntity?) {
    val streetNamesState = remember { mutableStateOf<List<String>>(emptyList()) }
    var textSize by remember { mutableStateOf(13.sp) }

    LaunchedEffect(route) {
        route?.let {
            getStreetNamesFromHalte(it) { streetNames ->
                streetNamesState.value = streetNames
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(16.dp)
            .heightIn(max = 450.dp)
    ) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 8.dp)
        ) {
            if (route != null) {
                itemsIndexed(route.streetNames) { index, streetName ->
                    val isFirstItem = index == 0
                    val isLastItem = index == route.streetNames.size - 1

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 0.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .width(30.dp)
                                .fillMaxHeight(),
                        ) {
                            // Garis vertikal
                            Box(
                                modifier = Modifier
                                    .width(2.dp)
                                    .height(67.dp) // Pastikan garis mencakup tinggi penuh
                                    .background(Color(0xFF1E88E5))
                                    .align(Alignment.Center)
                            )

                            // Lingkaran
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(Color(0xFF1E88E5), shape = CircleShape)
                                    .align(Alignment.Center) // Lingkaran di tengah garis
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp)) // Jarak antara lingkaran dan teks

                        // Teks nama jalan
                        Text(
                            text = streetName,
                            style = MaterialTheme.typography.bodyLarge.copy(fontSize = textSize),
                            color = Color.Black,
                            modifier = Modifier.align(Alignment.CenterVertically) // Sejajarkan teks dengan lingkaran
                        )
                    }
                }
            }
        }
    }
}

fun setupMap(map: GoogleMap, context: Context, route: RouteEntity?) {
    route?.let {
        val start = LatLng(it.origin.latitude, it.origin.longitude)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(start, 12f))
        drawRoute(map, it.id, context) // Draw route based on route ID
    }
}

fun drawRoute(map: GoogleMap?, routeId: String, context: Context) {
    if (map == null) return

    val route = RouteFactory.getRoute(routeId)
    if (route != null) {
        val geoContext = GeoApiContext.Builder()
            .apiKey(context.getString(R.string.google_map_api_key))
            .build()

        val origin = LatLng(route.origin.latitude, route.origin.longitude)
        val destination = LatLng(route.destination.latitude, route.destination.longitude)
        val waypoints = route.waypoints.map { com.google.maps.model.LatLng(it.latitude, it.longitude) }

        map.addMarker(MarkerOptions().position(origin).title("Origin"))
        map.addMarker(MarkerOptions().position(destination).title("Destination"))

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val result = DirectionsApiRequest(geoContext)
                    .origin(com.google.maps.model.LatLng(origin.latitude, origin.longitude))
                    .destination(com.google.maps.model.LatLng(destination.latitude, destination.longitude))
                    .waypoints(*waypoints.toTypedArray())
                    .mode(TravelMode.DRIVING)
                    .await()

                if (result.routes.isNotEmpty()) {
                    val apiRoute = result.routes[0]
                    val encodedPath = apiRoute.overviewPolyline.encodedPath
                    val decodedPath = PolyUtil.decode(encodedPath)

                    val polylineOptions = PolylineOptions()
                        .addAll(decodedPath.map { LatLng(it.latitude, it.longitude) })
                        .width(10f)
                        .color(0xFF1E88E5.toInt())
                    map.addPolyline(polylineOptions)
                    val arrowMarkers = addArrowsToPolyline(map, decodedPath, 700, context) // Adjust arrow spacing as needed
                    // Add camera listener to control arrow visibility
                    map.setOnCameraIdleListener {
                        val currentZoom = map.cameraPosition.zoom
                        val arrowVisibility = currentZoom >= 13 // Adjust zoom threshold as needed

                        arrowMarkers.forEach { marker ->
                            marker.isVisible = arrowVisibility
                        }
                    }
                } else {
                    Toast.makeText(context, "No route found", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    } else {
        Toast.makeText(context, "Invalid route ID", Toast.LENGTH_SHORT).show()
    }
}

fun addArrowsToPolyline(map: GoogleMap, points: List<LatLng>, spacing: Int, context: Context): MutableList<Marker> {    var currentDistance = 0.0
    val arrowMarkers = mutableListOf<Marker>()
    for (i in 0 until points.size - 1) {
        val p1 = points[i]
        val p2 = points[i + 1]
        val segmentDistance = SphericalUtil.computeDistanceBetween(p1, p2)
        currentDistance += segmentDistance


        if (currentDistance >= spacing) {
            currentDistance = 0.0 // Reset distance for next arrow

            // Calculate arrow position (midpoint of segment)
            val arrowPosition = SphericalUtil.interpolate(p1, p2, 0.5)

            // Calculate arrow heading (bearing from p1 to p2)
            val heading = SphericalUtil.computeHeading(p1, p2)

            val arrowIcon = BitmapDescriptorFactory.fromResource(R.drawable.arrow_icon)
            val drawable = ContextCompat.getDrawable(context, R.drawable.arrow_icon)
            if (drawable != null) {
                val bitmap = drawable.toBitmap(50, 50) // Scale the bitmap here

                val scaledIcon = BitmapDescriptorFactory.fromBitmap(bitmap)

                // Add arrow marker
                val marker = map.addMarker(
                    MarkerOptions()
                        .position(arrowPosition)
                        .icon(scaledIcon)
                        .rotation(heading.toFloat())
                        .anchor(0.5f, 0.5f)
                        .flat(true)
                )
                if (marker != null) { // Check if marker was added successfully
                    arrowMarkers.add(marker)
                } // Add each marker to the list
            } else {
                // Handle the case where the bitmap is null (e.g., log an error)
                Log.e("addArrowsToPolyline", "Failed to convert arrow icon to bitmap")
            }
        }
    }
    return arrowMarkers // Return the list of arrow markers
}
