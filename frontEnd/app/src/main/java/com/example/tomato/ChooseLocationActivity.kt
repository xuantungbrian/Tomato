package com.example.tomato

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.LocationServices
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class ChooseLocationActivity : ComponentActivity() {
    private lateinit var placesClient: PlacesClient
    private lateinit var sessionToken: AutocompleteSessionToken
    private lateinit var autoCompleteTextView: AutoCompleteTextView

    // Store the latitude and longitude as variables
    private var latitude: Double? = null
    private var longitude: Double? = null
    private var locationName: String? = null

    companion object {
        private const val TAG = "ChooseLocationActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_location)

        // Initialize Places API
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, BuildConfig.MAP_API_KEY)
        }

        // Initialize Places Client and Session Token
        placesClient = Places.createClient(this)
        sessionToken = AutocompleteSessionToken.newInstance()

        // Find AutoCompleteTextView in the layout
        autoCompleteTextView = findViewById(R.id.locationAutoCompleteTextView)

        // Set up a text change listener for location search
        // Declare searchResults as a mutable list of AutocompletePrediction
        val searchResults = mutableListOf<AutocompletePrediction>()
        autoCompleteTextView.addTextChangedListener(object : android.text.TextWatcher {
            override fun afterTextChanged(s: android.text.Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.isNullOrEmpty()) return

                val request = FindAutocompletePredictionsRequest.builder()
                    .setSessionToken(sessionToken)
                    .setQuery(s.toString())
                    .build()

                // Make request to get autocomplete predictions
                placesClient.findAutocompletePredictions(request)
                    .addOnSuccessListener { response ->
                        Log.d(TAG, "Received response: ${response.autocompletePredictions}")
                        searchResults.clear() // Clear previous results

                        // Add actual predictions to the list
                        for (prediction in response.autocompletePredictions) {
                            searchResults.add(prediction) // Add prediction object
                        }

                        // Update AutoCompleteTextView with the results
                        val adapter = ArrayAdapter(
                            this@ChooseLocationActivity,
                            android.R.layout.simple_dropdown_item_1line,
                            listOf("Use Current Location") + searchResults.map { it.getFullText(null).toString() } // Displaying only the full text
                        )
                        autoCompleteTextView.setAdapter(adapter)
                    }
                    .addOnFailureListener { exception ->
                        Log.e(TAG, "Request failed", exception)
                        Toast.makeText(this@ChooseLocationActivity, "Failed to get predictions: ${exception.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        })

// Add ItemClickListener to AutoCompleteTextView to handle selection of "Use Current Location"
        autoCompleteTextView.setOnItemClickListener { parent, _, position, _ ->
            val selectedItem = parent.getItemAtPosition(position) as String
            if (selectedItem == "Use Current Location") {
                // Call getCurrentLocation when "Use Current Location" is selected
                lifecycleScope.launch {
                    val (lat, lon) = getCurrentLocation()
                    latitude = lat
                    longitude = lon
                    autoCompleteTextView.setText(getLocationFromCoordinates(latitude!!, longitude!!))
                }
            } else {
                // Get the corresponding AutocompletePrediction object
                val selectedPrediction = searchResults[position - 1]

                // Use the placeId from the selected prediction
                val placeId = selectedPrediction.placeId
                val fetchPlaceRequest = FetchPlaceRequest.builder(placeId, listOf(Place.Field.LAT_LNG)).build()

                placesClient.fetchPlace(fetchPlaceRequest)
                    .addOnSuccessListener { response ->
                        val place = response.place
                        val latLng = place.latLng
                        latLng?.let {
                            latitude = it.latitude
                            longitude = it.longitude
                        }

                    }
                    .addOnFailureListener { exception ->
                        // Handle failure
                        Log.e("Places", "Place not found: ${exception.message}")
                    }
            }
        }

        val proceedButton = findViewById<Button>(R.id.submitLocationButton)
        proceedButton.setOnClickListener{
            // Make sure that location is selected (either by "Use Current Location" or from AutoComplete)
            if (latitude != null && longitude != null) {
                    locationName = autoCompleteTextView.text.toString()
                    val resultIntent = intent
                    resultIntent.putExtra("latitude", latitude)
                    resultIntent.putExtra("longitude", longitude)
                    resultIntent.putExtra("locationName", locationName)
                    setResult(RESULT_OK, resultIntent)
                    finish()
            } else {
                Toast.makeText(this@ChooseLocationActivity, "Please select a location", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Obtain user's current location (latitude, longitude).
     */
    private suspend fun getCurrentLocation(): Pair<Double, Double> {
        return suspendCoroutine { continuation ->
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
                continuation.resume(Pair(0.0, 0.0))  // Return fallback values if permission is not granted
                return@suspendCoroutine
            }

            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        continuation.resume(Pair(location.latitude, location.longitude))
                    } else {
                        continuation.resume(Pair(0.0, 0.0))  // Fallback location
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("LocationError", "Failed to get location: ${exception.message}")
                    continuation.resume(Pair(0.0, 0.0))  // Return fallback values on failure
                }
        }
    }

    /**
     * Translate latitude and longitude to address and display it to the user.
     */
    private fun getLocationFromCoordinates(latitude: Double, longitude: Double): String {
        val geocoder = Geocoder(this, java.util.Locale("en", "US"))
        try {
            val addresses: MutableList<android.location.Address>? = geocoder.getFromLocation(latitude, longitude, 1)
            Log.d(TAG, "latitude: $latitude, longitude: $longitude")
            Log.d(TAG, "Address: $addresses")
            if (addresses != null) {
                if (addresses.isNotEmpty()) {
                    val fullAddress = commonFunction.parseLocation(latitude, longitude, this)
                    return fullAddress
                } else {
                    Log.e("GeocoderError", "No address found for this location.")
                }
            }
        } catch (e: Exception) {
            Log.e("GeocoderError", "Error fetching address: ${e.message}")
        }
        return ""
    }
}
