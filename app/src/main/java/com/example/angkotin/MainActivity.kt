//mainActivity.kt
package com.example.angkotin


import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.angkotin.data.RouteFactory
import com.example.angkotin.ui.screens.Routedetailpage
import com.example.angkotin.ui.screens.routelistpage
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.MapStyleOptions

class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set the layout file as the content view.
        setContentView(R.layout.activity_maps)

        // Get a handle to the fragment and register the callback.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment!!.getMapAsync(this)

        setContent {
            BusSurabayaApp()
        }
    }

    // Get a handle to the GoogleMap object and display marker.
    override fun onMapReady(googleMap: GoogleMap) {
        googleMap.addMarker(
            MarkerOptions()
                .position(LatLng(0.0, 0.0))
                .title("Marker")
        )
    }
}

@Composable
fun BusSurabayaApp() {
    val navController = rememberNavController()


    Scaffold(
        topBar = { /* Top bar already in HomePage */ }
    ) { innerPadding ->
        androidx.navigation.compose.NavHost(
            navController = navController,
            startDestination = "home_page",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home_page") { HomePage(navController) }


            composable("list_view_page") { routelistpage(navController) }
            composable("route_detail_page/{routeId}") { backStackEntry ->
                val routeId = backStackEntry.arguments?.getString("routeId") ?: ""
                Routedetailpage(navController = navController, routeId = routeId)
            }
        }
    }
}



@Composable
fun HomePage(navController: androidx.navigation.NavController) {
    val context = LocalContext.current
    var mapView: MapView? = remember { null } // Keep a reference to the MapView

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp) // Add padding to avoid screen edges overlap
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // Top bar
            var searchText by remember { mutableStateOf("") }
            MyTopBar(
                searchText = searchText,
                onSearchTextChange = { newText -> searchText = newText }
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Map View
            AndroidView(
                factory = {
                    mapView = MapView(context).apply {
                        // Set up the map with lifecycle events
                        onCreate(null)
                        onResume()
                        getMapAsync { googleMap ->
                            googleMap.uiSettings.isZoomControlsEnabled = false
                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(-7.966560523100535, 112.6325158401338), 13f))
                            googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style))
                        }
                    }
                    mapView!!
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        // Routes Button at the bottom end
        RoutesButton(onClick = {
            val intent = Intent(context, ListRouteActivity::class.java)
            context.startActivity(intent)
//                mapView?.getMapAsync { googleMap ->
//                    drawRoute(googleMap, "1", context) // Ensure googleMap is available
//                }
        })
    }
}


@Composable
fun MyTopBar(searchText: String, onSearchTextChange: (String) -> Unit) {
    val context = LocalContext.current
    val height = 45.dp
    var showHint by remember { mutableStateOf(searchText.isEmpty()) }

    // State untuk menyimpan hasil pencarian
    var isSearching by remember { mutableStateOf(false) }
    val routes = remember {
        (1..21).mapNotNull { RouteFactory.getRoute(it.toString()) }
    }

    val filteredRoutes = remember(searchText) {
        if (searchText.isEmpty()) {
            emptyList()
        } else {
            routes.filter {
                it.routeName.contains(searchText, ignoreCase = true)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Menu Button
        IconButton(
            onClick = {
                val intent = Intent(context, SidebarActivity::class.java)
                context.startActivity(intent)
            },
            modifier = Modifier
                .align(Alignment.TopStart)
                .clip(CircleShape)
                .background(Color.White)
                .padding(8.dp)
                .height(height)
                .width(45.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Menu,
                contentDescription = "Menu",
                tint = Color.Black,
                modifier = Modifier.size(40.dp)
            )
        }

        // Search Bar and Results
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 70.dp) // Adjust to avoid overlap with the menu button
        ) {
            BasicTextField(
                value = searchText,
                onValueChange = { newText ->
                    onSearchTextChange(newText)
                    showHint = newText.isEmpty()
                    isSearching = newText.isNotEmpty()
                },
                modifier = Modifier
                    .clip(RoundedCornerShape(30.dp))
                    .background(Color.White)
                    .padding(horizontal = 10.dp, vertical = 8.dp)
                    .fillMaxWidth()
                    .height(height)
                    .onFocusChanged { focusState ->
                        showHint = !focusState.isFocused && searchText.isEmpty()
                        isSearching = focusState.isFocused && searchText.isNotEmpty()
                    },
                textStyle = TextStyle(color = Color.Black),
                singleLine = true,
                decorationBox = { innerTextField ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "Search",
                            tint = Color.Gray,
                            modifier = Modifier.size(30.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        if (showHint) {
                            Text(
                                text = "Temukan rute",
                                color = Color.Gray
                            )
                        }
                        innerTextField()
                    }
                }
            )

            // Expand search results below the search bar
            if (isSearching && filteredRoutes.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                        .clip(RoundedCornerShape(30.dp))
                        .background(Color.White)
                        .border(1.dp, Color.White, RoundedCornerShape(30.dp))
                        .heightIn(max = 400.dp)
                ) {
                    LazyColumn(
                        modifier = Modifier.padding(10.dp)
                    ) {
                        items(filteredRoutes) { route ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val intent = Intent(context, RouteDetailActivity::class.java)
                                        intent.putExtra("routeId", route.id)
                                        context.startActivity(intent)
                                    }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painter = painterResource(id = route.logo),
                                    contentDescription = null,
                                    modifier = Modifier.size(40.dp),
                                    tint = Color.Unspecified
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(
                                    text = route.routeName,
                                    color = Color.Black,
                                    fontSize = 12.sp
                                )
                            }
                            Divider()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RoutesButton(onClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(100.dp) // Increase the size of the button (adjust the value as needed)
                .padding(16.dp) // Add some padding for spacing
        ) {
            Icon(
                painter = painterResource(id = R.drawable.routes), // Replace with your drawable
                contentDescription = "Routes",
                modifier = Modifier.size(60.dp), // Increase the size of the icon
                tint = Color.Black// Adjust tint if necessary
            )
        }
    }
}