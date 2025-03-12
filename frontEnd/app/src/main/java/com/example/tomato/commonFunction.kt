package com.example.tomato

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Geocoder
import android.net.Uri
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import androidx.core.content.FileProvider
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.ByteArrayOutputStream
import java.io.File
import java.lang.Math.max
import java.security.MessageDigest
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID

/**
 * Common functions used in multiple activities.
 */
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
        return address.getAddressLine(0).toString()
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
            if (UserCredentialManager.isLoggedIn(context)) {
                context.startActivity(Intent(context, ProfileActivity::class.java))
            }
            else{
                AlertDialog.Builder(context)
                    .setTitle("Login is required to view profile page")
                    .setNegativeButton("Okay", null)
                    .show()
            }
        }
        uploadButton.setOnClickListener {
            if (UserCredentialManager.isLoggedIn(context)) {
                context.startActivity(Intent(context, UploadPostActivity::class.java))
            }
            else {
                AlertDialog.Builder(context)
                    .setTitle("Login is required to upload post")
                    .setNegativeButton("Okay", null)
                    .show()
            }
        }
    }

     fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val heightRatio = height.toFloat() / reqHeight.toFloat()
            val widthRatio = width.toFloat() / reqWidth.toFloat()
            inSampleSize = max(heightRatio, widthRatio).toInt()
        }
        return inSampleSize
    }
    fun getCompressedImageByteArray(
        activity: Activity,
        uri: Uri,
        reqWidth: Int = 512,
        reqHeight: Int = 512,
        quality: Int = 50
    ): ByteArray? {
        // First, decode only the image dimensions.
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        activity.contentResolver.openInputStream(uri)?.use { inputStream ->
            BitmapFactory.decodeStream(inputStream, null, options)
        }

        // Calculate an inSampleSize value (power-of-2 scale factor) to downscale the image.
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
        options.inJustDecodeBounds = false

        // Decode the image file into a Bitmap.
        val bitmap = activity.contentResolver.openInputStream(uri)?.use { inputStream ->
            BitmapFactory.decodeStream(inputStream, null, options)
        } ?: return null

        // Calculate the scaling factor while preserving the original aspect ratio.
        val scaleFactor = minOf(
            reqWidth / bitmap.width.toFloat(),
            reqHeight / bitmap.height.toFloat()
        )
        // If the scaleFactor is greater than 1, the image is smaller than requested dimensions.
        // In that case, keep the original size.
        val newWidth = if (scaleFactor < 1) (bitmap.width * scaleFactor).toInt() else bitmap.width
        val newHeight = if (scaleFactor < 1) (bitmap.height * scaleFactor).toInt() else bitmap.height

        // Create a scaled bitmap using the calculated dimensions.
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)

        // Compress the Bitmap into a ByteArrayOutputStream.
        val outputStream = ByteArrayOutputStream()
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
        return outputStream.toByteArray()
    }

     fun generateHashedNonce(): String {
        val rawNonce = UUID.randomUUID().toString()
        val bytes = rawNonce.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }

}