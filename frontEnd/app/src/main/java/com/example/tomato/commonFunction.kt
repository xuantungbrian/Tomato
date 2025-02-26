package com.example.tomato

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.location.Geocoder
import android.net.Uri
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import androidx.core.content.FileProvider
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID

object commonFunction {
    /**
     * Given the latitude and longitude of a location get the string representation of the location.
     */
    fun parseLocation(latitude: Double, longitude: Double, context: Context): String {
        val geocoder = Geocoder(context, Locale("en", Locale.getDefault().country))
        val addresses = geocoder.getFromLocation(latitude, longitude, 1)
        if (addresses == null || addresses.isEmpty()) {
            return "Unknown Location"
        }

        val address = addresses[0]
        val street = address.thoroughfare?:"" // street name
        val locality = address.locality?:"" // city or locality
        val adminArea = address.adminArea?:"" // state or province
        val country = address.countryName?:"" // country

        val addressParts = listOf<String>(street, locality, adminArea, country)
        var fullAddress = ""
        for(part in addressParts){
            if(part != ""){
                fullAddress += part
                if(part != addressParts.last()){
                    fullAddress += ", "
                }
            }

        }

        return fullAddress
    }

    /**
     * Convert a list of ByteArray to a list of Uri.
     * @param imageByteArrays: List of images represented as ByteArray.
     * @param cacheDir: Directory to store the images (just pass cachedir).
     * @param context: Context of the application.
     */
    fun byteToURIs(imageByteArrays: List<ByteArray>, cacheDir: File, context: Context): List<Uri> {
        val imageUris = mutableListOf<Uri>()

        for (byteArray in imageByteArrays) {
            val file = File(cacheDir, "image_${UUID.randomUUID()}.jpg")
            file.outputStream().use { output ->
                output.write(byteArray)
            }
            val uri = FileProvider.getUriForFile(context, "com.example.tomato.fileprovider", file)
            imageUris.add(uri)
        }

        return imageUris
    }

    /**
     * Convert date of format yyyy-MM-dd.... to dd MMM yyyy.
     */
    fun convertDateToString(date: String): String {
        val dateFormatted = date.substring(0, 10)
        val dateParts = dateFormatted.split("-")
        val day = dateParts[2].toInt()
        val month = dateParts[1].toInt()
        val year = dateParts[0].toInt()

        val localDate = LocalDate.of(year, month, day)
        val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.getDefault())
        return localDate.format(formatter)

    }

    /**
     * Initialize the bottom navigation bar buttons
     */
     fun initNavBarButtons(context: Context, activity: Activity) {
        val homeButton = activity.findViewById<LinearLayout>(R.id.bottom_navbar_home_button)
        val profileButton = activity.findViewById<LinearLayout>(R.id.bottom_navbar_profile_button)
        val uploadButton = activity.findViewById<FloatingActionButton>(R.id.bottom_navbar_upload_button)

        homeButton.setOnClickListener {
            context.startActivity(Intent(context, MapsActivity::class.java))
        }
        profileButton.setOnClickListener {
            context.startActivity(Intent(context, ProfileActivity::class.java))
        }
        uploadButton.setOnClickListener {
            context.startActivity(Intent(context, UploadPostActivity::class.java))
        }
    }


}