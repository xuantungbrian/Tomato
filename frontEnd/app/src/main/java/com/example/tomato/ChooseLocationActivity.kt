//// ChooseLocationActivity.kt
package com.example.tomato
//
//import android.content.Context
//import android.location.Geocoder
//import android.os.Bundle
//import android.widget.Toast
import androidx.activity.ComponentActivity
//import androidx.activity.compose.setContent
//import androidx.activity.enableEdgeToEdge
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.padding
//import androidx.compose.material3.Button
//import androidx.compose.material3.ExperimentalMaterial3Api
//import androidx.compose.material3.FloatingActionButton
//import androidx.compose.material3.Scaffold
//import androidx.compose.material3.Text
//import androidx.compose.material3.TextField
//import androidx.compose.material3.TopAppBar
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.unit.dp
//import androidx.lifecycle.viewmodel.compose.viewModel
//import com.google.maps.android.compose.GoogleMap
//import com.google.maps.android.compose.MapProperties
//import com.google.maps.android.compose.MapUiSettings
//import com.google.maps.android.compose.Marker
//import com.google.maps.android.compose.MarkerState
//import com.google.maps.android.compose.rememberCameraPositionState
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.withContext
//
class ChooseLocationActivity : ComponentActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
//        setContent {
//            TOMAToTheme {
//                val viewModel: LocationViewModel = viewModel()
//                ChooseLocationScreen(viewModel) {
//                    // Handle location selection completion
//                    finish()
//                }
//            }
//        }
//    }
}
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun ChooseLocationScreen(
//    viewModel: LocationViewModel,
//    onComplete: () -> Unit
//) {
//    val context = LocalContext.current
//    var searchQuery by remember { mutableStateOf("") }
//    val cameraPositionState = rememberCameraPositionState()
//
//    // Sync ViewModel's camera position with the state
//    LaunchedEffect(viewModel.cameraPosition) {
//        viewModel.cameraPosition?.let {
//            cameraPositionState.position = it
//        }
//    }
//
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = { Text("Choose Location") }
//            )
//        },
//        floatingActionButton = {
//            FloatingActionButton(
//                onClick = { viewModel.getCurrentLocation(context) }
//            ) {
//                Text("Current")
//            }
//        }
//    ) { innerPadding ->
//        Column(
//            modifier = Modifier
//                .padding(innerPadding)
//                .fillMaxSize(),
//            verticalArrangement = Arrangement.Top
//        ) {
//            TextField(
//                value = searchQuery,
//                onValueChange = { searchQuery = it },
//                modifier = Modifier.padding(16.dp),
//                placeholder = { Text("Search location...") },
//                singleLine = true,
//                trailingIcon = {
//                    Button(onClick = {
//                        viewModel.searchLocation(context, searchQuery)
//                    }) {
//                        Text("Search")
//                    }
//                }
//            )
//
//            Box(modifier = Modifier.weight(1f)) {
//                GoogleMap(
//                    modifier = Modifier.fillMaxSize(),
//                    cameraPositionState = cameraPositionState,
//                    properties = MapProperties(),
//                    uiSettings = MapUiSettings(zoomControlsEnabled = false)
//                ) {
//                    viewModel.selectedLocation.value?.let { location ->
//                        Marker(
//                            state = MarkerState(position = location),
//                            title = "Selected Location",
//                            snippet = "${location.latitude}, ${location.longitude}",
//                            draggable = true,
//                            onDragEnd = { newPosition ->
//                                viewModel.updateSelectedLocation(newPosition)
//                                viewModel.cameraPosition = CameraPosition.fromLatLngZoom(newPosition, 15f)
//                            }
//                        )
//                    }
//                }
//            }
//
//            Button(
//                onClick = {
//                    viewModel.selectedLocation.value?.let {
//                        Toast.makeText(
//                            context,
//                            "Location selected: ${it.latitude}, ${it.longitude}",
//                            Toast.LENGTH_LONG
//                        ).show()
//                        onComplete()
//                    }
//                },
//                modifier = Modifier
//                    .align(Alignment.CenterHorizontally)
//                    .padding(16.dp)
//            ) {
//                Text("Confirm Location")
//            }
//        }
//    }
//}
//
//// LocationViewModel.kt
//package com.example.tomato
//
//import android.content.Context
//import android.location.Location
//import androidx.lifecycle.ViewModel
//import com.google.android.gms.location.LocationServices
//import com.google.android.gms.maps.model.CameraPosition
//import com.google.android.gms.maps.model.LatLng
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.tasks.await
//import kotlinx.coroutines.withContext
//import java.io.IOException
//
//class LocationViewModel : ViewModel() {
//    val selectedLocation = mutableStateOf<LatLng?>(null)
//    var cameraPosition: CameraPosition? by mutableStateOf(null)
//        private set
//
//    fun updateSelectedLocation(latLng: LatLng) {
//        selectedLocation.value = latLng
//    }
//
//    fun searchLocation(context: Context, query: String) {
//        if (query.isEmpty()) return
//
//        CoroutineScope(Dispatchers.IO).launch {
//            try {
//                val geocoder = Geocoder(context)
//                val addresses = geocoder.getFromLocationName(query, 1)
//                addresses?.firstOrNull()?.let {
//                    val latLng = LatLng(it.latitude, it.longitude)
//                    withContext(Dispatchers.Main) {
//                        selectedLocation.value = latLng
//                        cameraPosition = CameraPosition.fromLatLngZoom(latLng, 15f)
//                    }
//                }
//            } catch (e: IOException) {
//                withContext(Dispatchers.Main) {
//                    Toast.makeText(context, "Search failed: ${e.message}", Toast.LENGTH_SHORT).show()
//                }
//            }
//        }
//    }
//
//    fun getCurrentLocation(context: Context) {
//        CoroutineScope(Dispatchers.IO).launch {
//            try {
//                val locationClient = LocationServices.getFusedLocationProviderClient(context)
//                val location: Location? = locationClient.lastLocation.await()
//
//                location?.let {
//                    val latLng = LatLng(it.latitude, it.longitude)
//                    withContext(Dispatchers.Main) {
//                        selectedLocation.value = latLng
//                        cameraPosition = CameraPosition.fromLatLngZoom(latLng, 15f)
//                    }
//                } ?: run {
//                    withContext(Dispatchers.Main) {
//                        Toast.makeText(context, "Unable to get current location", Toast.LENGTH_SHORT).show()
//                    }
//                }
//            } catch (e: SecurityException) {
//                withContext(Dispatchers.Main) {
//                    Toast.makeText(context, "Location permission denied", Toast.LENGTH_SHORT).show()
//                }
//            }
//        }
//    }
//}