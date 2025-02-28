package com.example.tomato

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient

class PlaceAutocompleteHelper(
    private val context: Context,
    private val autoCompleteTextView: AutoCompleteTextView,
    private val placesClient: PlacesClient,
    private val sessionToken: AutocompleteSessionToken,
    private val onPredictionSelected: (AutocompletePrediction) -> Unit,
    private val onUseCurrentLocation: (() -> Unit)? = null
) {

    private val searchResults = mutableListOf<AutocompletePrediction>()

    init {
        setupAutoCompleteListener()
    }

    private fun setupAutoCompleteListener() {
        autoCompleteTextView.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { /* no-op */ }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { /* no-op */ }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.isNullOrEmpty()) return

                val request = FindAutocompletePredictionsRequest.builder()
                    .setSessionToken(sessionToken)
                    .setQuery(s.toString())
                    .build()

                placesClient.findAutocompletePredictions(request)
                    .addOnSuccessListener { response ->
                        // Clear previous results
                        searchResults.clear()
                        searchResults.addAll(response.autocompletePredictions)

                        // Update the adapter with "Use Current Location" option and predictions
                        val adapter = ArrayAdapter(
                            context,
                            android.R.layout.simple_dropdown_item_1line,
                            listOf("Use Current Location") + searchResults.map { it.getFullText(null).toString() }
                        )
                        autoCompleteTextView.setAdapter(adapter)
                    }
                    .addOnFailureListener { exception ->
                        // Handle failure (e.g., log or notify the user)
                    }
            }
        })

        autoCompleteTextView.setOnItemClickListener { parent, _, position, _ ->
            // If the first item ("Use Current Location") is selected
            if (position == 0) {
                onUseCurrentLocation?.invoke()
            } else {
                // Adjust for the "Use Current Location" item at index 0
                val prediction = searchResults[position - 1]
                onPredictionSelected(prediction)
            }
        }
    }
}
