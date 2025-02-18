package com.example.tomato

import android.content.Context
import android.location.Geocoder
import java.util.Locale

object commonFunction {
    /**
     * Given the latitude and longitude of a location get the string representation of the location.
     */
    fun parseLocation(latitude: Double, longitude: Double, context: Context): String{
        val geocoder = Geocoder(context, Locale("en", Locale.getDefault().country))
        val addresses = geocoder.getFromLocation(latitude, longitude, 1)
        if (addresses == null || addresses.isEmpty()) {
            return "Unknown Location"
        }

        val address = addresses[0]
        val street = address.thoroughfare // street name
        val locality = address.locality // city or locality
        val adminArea = address.adminArea // state or province
        val country = address.countryName // country

        // Combine the results to get a meaningful address
        val fullAddress = "$street, $locality, $adminArea, $country"

        return fullAddress
    }
}