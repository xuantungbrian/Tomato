package com.example.tomato

import android.content.Context
import android.location.Geocoder
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

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
        val street = address.thoroughfare // street name
        val locality = address.locality // city or locality
        val adminArea = address.adminArea // state or province
        val country = address.countryName // country

        // Combine the results to get a meaningful address
        val fullAddress = "$street, $locality, $adminArea, $country"

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
            val file = File(cacheDir, "image_${System.currentTimeMillis()}.jpg")
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
     * Convert a list of PostImage to a list of ByteArray.
     */
    fun postImagesToByteArrays(images: List<PostImage>): List<ByteArray> {
        val imageByteArrays = mutableListOf<ByteArray>()
        for (image in images) {
            val imageByteArray = image.fileData.data.map { it.toByte() }.toByteArray()
            imageByteArrays.add(imageByteArray)
        }
        return imageByteArrays
    }

     fun saveUserId(context: Context, userId: String) {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        val sharedPreferences = EncryptedSharedPreferences.create(
            "UserPrefs",
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        with(sharedPreferences.edit()) {
            putString("userId", userId)
            apply()
        }
    }

     fun getUserId(context: Context): String? {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        val sharedPreferences = EncryptedSharedPreferences.create(
            "UserPrefs",
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        return sharedPreferences.getString("userId", null)
    }

    fun rawPostToPostItem(rawPost: PostItemRaw, context: Context): PostItem {
        val address = parseLocation(rawPost.latitude, rawPost.longitude, context)
        val imageURIs = byteToURIs(postImagesToByteArrays(rawPost.images), context.cacheDir, context)
        return PostItem(imageURIs, address, rawPost.date, rawPost.note, rawPost.private, rawPost.userId)
    }
}