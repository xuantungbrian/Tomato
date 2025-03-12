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
import android.widget.ImageView
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
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Activity for choosing a location for a post to be uploaded.
 */
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
        // Instantiate the autocomplete helper

        PlaceAutocompleteHelper(
            context = this,
            autoCompleteTextView = autoCompleteTextView,
            placesClient = placesClient,
            sessionToken = sessionToken,
            onPredictionSelected = { prediction ->
                // Handle the selected prediction
                val placeId = prediction.placeId
                val fetchPlaceRequest = com.google.android.libraries.places.api.net.FetchPlaceRequest.builder(
                    placeId,
                    listOf(com.google.android.libraries.places.api.model.Place.Field.LAT_LNG)
                ).build()

                placesClient.fetchPlace(fetchPlaceRequest)
                    .addOnSuccessListener { response ->
                        val place = response.place
                        place.latLng?.let {
                            latitude = it.latitude
                            longitude = it.longitude
                        }
                    }
                    .addOnFailureListener { exception ->
                        // Handle failure
                    }
            },
            onUseCurrentLocation = {
                // launch a coroutine to obtain the location
                lifecycleScope.launch {
                    val (lat, lon) = getCurrentLocation()
                    latitude = lat
                    longitude = lon
                    autoCompleteTextView.setText(getLocationFromCoordinates(latitude!!, longitude!!))
                }
            }
        )

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

        val backButton = findViewById<ImageView>(R.id.choose_location_back)
        backButton.setOnClickListener {
            finish()
        }
    }

    private suspend fun getCurrentLocation(): Pair<Double, Double> {
        return suspendCoroutine { continuation ->
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
                continuation.resumeWith(Result.success(Pair(0.0, 0.0)))
            }
            else{
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
    }

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
        } catch (e: IOException) {
            Log.e("GeocoderError", "Network or service issue: ${e.message}")
        } catch (e: IllegalArgumentException) {
            Log.e("GeocoderError", "Invalid latitude or longitude: ${e.message}")
        } catch (e: SecurityException) {
            Log.e("GeocoderError", "Missing location permissions: ${e.message}")
        }
        return ""
    }
}
