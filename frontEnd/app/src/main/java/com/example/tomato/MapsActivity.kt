package com.example.tomato

import JwtManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapShader
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.RadialGradient
import android.graphics.Shader
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.lifecycleScope

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.tomato.databinding.ActivityMapsBinding
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Marker
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.android.libraries.places.api.Places
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.lang.Math.max
import java.lang.Math.min
import java.security.MessageDigest
import java.util.UUID

data class SignInResponse(val token: String, val userID: String)

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {
    private var lastFetchJob: Job? = null

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    private val activityScope = CoroutineScope(Dispatchers.Main)

    companion object {
        private const val TAG = "MapsActivity"
    }

    // PARAMETERS
    private val fetchDelay = 500 //Minimum idling time before fetching posts
    private val postSize = 80 //The circular image size on the map

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // Initialize Places SDK if not already done.
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), BuildConfig.WEB_CLIENT_ID);
        }

        // Initialize the Map Fragment
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)


        // Add event listener for sign in button
        findViewById<Button>(R.id.sign_in_button).setOnClickListener {
            val credentialManager = CredentialManager.create(this)
            val signInWithGoogleOption: GetSignInWithGoogleOption = GetSignInWithGoogleOption
                .Builder(BuildConfig.WEB_CLIENT_ID)
                .setNonce(generateHashedNonce())
            .build()

            val request: GetCredentialRequest = GetCredentialRequest.Builder()
                .addCredentialOption(signInWithGoogleOption)
                .build()

            activityScope.launch {
                try {
                    val result = credentialManager.getCredential(
                        request = request,
                        context = this@MapsActivity,
                    )
                    handleSignIn(result)
                } catch (e: GetCredentialException) {
                    Log.d(TAG, "Get credential exception", e)
                }
            }
        }

        // Event listener to upload post button
        findViewById<FloatingActionButton>(R.id.bottom_navbar_upload_button).setOnClickListener {
            val intent = Intent(this, UploadPostActivity::class.java)
            startActivity(intent)
        }

        // Event listener to home button
        findViewById<LinearLayout>(R.id.bottom_navbar_home_button).setOnClickListener {
            lifecycleScope.launch {
                val response = HTTPRequest.sendGetRequest("http://10.0.2.2:3000/test1", this@MapsActivity)
                Log.d(TAG, "onCreate: $response")
            }
        }

        // Event listener to profile button
        findViewById<LinearLayout>(R.id.bottom_navbar_profile_button).setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

    }


    private fun handleSignIn(result: GetCredentialResponse) {
        // Handle the successfully returned credential.
        val credential = result.credential

        when (credential) {
            is CustomCredential -> {
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        // Use googleIdTokenCredential and extract id to validate and
                        // authenticate on your server.
                        val googleIdTokenCredential = GoogleIdTokenCredential
                            .createFrom(credential.data).idToken
                        sendSignInRequest(googleIdTokenCredential)
                    } catch (e: GoogleIdTokenParsingException) {
                        Log.e(TAG, "Received an invalid google id token response", e)
                    }
                }

            }

            else -> {
                // Catch any unrecognized credential type here.
                Log.e(TAG, "Unexpected type of credential")
            }
        }
    }

    private fun sendSignInRequest(token: String) {
        // JSON string to send in the POST request
        val body = JSONObject().put("token", token).toString()
        lifecycleScope.launch {
            val response = HTTPRequest.sendPostRequest("${BuildConfig.SERVER_ADDRESS}/user/auth",
                body, this@MapsActivity)
            Log.d(TAG, "sendPostRequest: $response")

            if (response != null) {
                val signInResponse = Gson().fromJson(response, SignInResponse::class.java)
                JwtManager.saveToken(this@MapsActivity, signInResponse.token)
                Log.d(TAG, "sendSignInResponse: $signInResponse")
            }
        }
    }


    private fun generateHashedNonce(): String {
        val rawNonce = UUID.randomUUID().toString()
        val bytes = rawNonce.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        googleMap.setOnCameraIdleListener {
            getPostsOnScreen(googleMap)
        }
    }



    private fun getPostsOnScreen(googleMap: GoogleMap) {
        // Only fetch if user has idled for quite awhile.
        lastFetchJob?.cancel()
        lastFetchJob = lifecycleScope.launch {
            delay(fetchDelay.toLong()) //delay only blocks this coroutine (inside lifecycleScope)
            val visibleRegion = googleMap.projection.visibleRegion
            val startLat = min(visibleRegion.farLeft.latitude, visibleRegion.nearRight.latitude)
            val endLat = max(visibleRegion.farLeft.latitude, visibleRegion.nearRight.latitude)
            val startLong = min(visibleRegion.farLeft.longitude, visibleRegion.nearRight.longitude)
            val endLong = max(visibleRegion.farLeft.longitude, visibleRegion.nearRight.longitude)
            val url = "${BuildConfig.SERVER_ADDRESS}/posts?" +
                    "start_lat=$startLat&end_lat=$endLat&" +
                    "start_long=$startLong&end_long=$endLong"


            // Fetch on the background
            val response = withContext(Dispatchers.IO) {
                HTTPRequest.sendGetRequest(
                    url,
                    this@MapsActivity
                )
            }

            if (response != null) {
                try{
                    val gson = Gson()
                    val postArray = gson.fromJson(response, Array<PostItemRaw>::class.java)
                    for(post in postArray){
                        val postLocation = LatLng(post.latitude, post.longitude)
                        // Only need to show the first image of post on the map.
                        val firstImage = post.images[0]
                        showImageOnMap(firstImage, postLocation)
                    }

                }
                catch (e: Exception){
                    e.printStackTrace()
                    Log.e(TAG, "Error parsing response: ${e.message}")
                }
            }
        }

    }

    fun Int.dpToPx(context: Context): Int = (this * context.resources.displayMetrics.density).toInt()

    /**
     * Show image on map.
     * @param image: the image of the post to be displayed on the map.
     * @param postLocation: the location of the post.
     */
    private fun showImageOnMap(image: PostImage, postLocation: LatLng) {
        val targetSize = postSize.dpToPx(this)

        //Convert image to ByteArray
        val imageByteArray = image.fileData.data.map { it.toByte() }.toByteArray()

        // Memory optimization: setting inJustDecodeBounds to true so we only get the metadata
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }

        // Only needs metadata to calculate inSampleSize
        BitmapFactory.decodeByteArray(imageByteArray, 0, imageByteArray.size, options)
        options.inSampleSize = calculateInSampleSize(options, targetSize, targetSize)

        // Set back to false, now we need the actual pixels
        options.inJustDecodeBounds = false
        val originalBitmap = BitmapFactory.decodeByteArray(imageByteArray
            , 0,
            imageByteArray.size,
            options) ?: return

        // Create circular bitmap with aspect ratio preservation
        val circularBitmap = createCircularBitmap(originalBitmap, targetSize)
        originalBitmap.recycle()

        // Add glow effect
        val dominantColor = getMedianColor(circularBitmap)
        val glowBitmap = addGlow(circularBitmap, dominantColor)
        circularBitmap.recycle()

        // Add marker with glow
        mMap.addMarker(
            MarkerOptions()
                .position(postLocation)
                .icon(BitmapDescriptorFactory.fromBitmap(glowBitmap))
        )
    }

    /**
     * Compute how much should we scale down the image to fit the requirement.
     */
    private fun calculateInSampleSize(options: BitmapFactory.Options,
                                      reqWidth: Int, reqHeight: Int): Int {
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val heightRatio = height.toFloat() / reqHeight.toFloat()
            val widthRatio = width.toFloat() / reqWidth.toFloat()
            inSampleSize = max(heightRatio, widthRatio).toInt()
        }
        return inSampleSize
    }

    /**
     * Crop an original bitmap to circular bitmap.
     */
    private fun createCircularBitmap(source: Bitmap, targetSize: Int): Bitmap {
        // Calculate scale to fit the smaller dimension to targetSize
        val scale = if (source.width < source.height) {
            targetSize.toFloat() / source.width
        } else {
            targetSize.toFloat() / source.height
        }

        // Scale the image proportionally
        val scaledWidth = (source.width * scale).toInt()
        val scaledHeight = (source.height * scale).toInt()
        val scaledBitmap = Bitmap.createScaledBitmap(source, scaledWidth, scaledHeight, true)

        // Calculate crop coordinates (centered)
        val startX = (scaledWidth - targetSize).coerceAtLeast(0) / 2
        val startY = (scaledHeight - targetSize).coerceAtLeast(0) / 2

        // Ensure we don't exceed bitmap dimensions
        val safeWidth = min(targetSize, scaledWidth - startX)
        val safeHeight = min(targetSize, scaledHeight - startY)

        // Crop to target size
        val croppedBitmap = Bitmap.createBitmap(
            scaledBitmap,
            startX,
            startY,
            safeWidth,
            safeHeight
        )
        scaledBitmap.recycle()

        // Create circular bitmap
        val output = Bitmap.createBitmap(targetSize, targetSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)

        val paint = Paint().apply {
            isAntiAlias = true
            shader = BitmapShader(croppedBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        }
        canvas.drawCircle(
            targetSize / 2f,
            targetSize / 2f,
            targetSize / 2f,
            paint
        )

        croppedBitmap.recycle()
        return output
    }
    private fun addGlow(circularBitmap: Bitmap, glowColor: Int): Bitmap {
        val glowRadius = 25f
        val padding = (glowRadius * 2).toInt()

        val glowBitmap = Bitmap.createBitmap(
            circularBitmap.width + padding,
            circularBitmap.height + padding,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(glowBitmap)

        // Create gradient from glowColor to transparent
        val paint = Paint().apply {
            shader = RadialGradient(
                glowBitmap.width / 2f,
                glowBitmap.height / 2f,
                circularBitmap.width / 2f + glowRadius,
                intArrayOf(glowColor, Color.TRANSPARENT),
                floatArrayOf(0f, 1f),
                Shader.TileMode.CLAMP
            )
            maskFilter = BlurMaskFilter(glowRadius, BlurMaskFilter.Blur.NORMAL)
        }

        canvas.drawCircle(
            glowBitmap.width / 2f,
            glowBitmap.height / 2f,
            circularBitmap.width / 2f + glowRadius,
            paint
        )

        // Draw original image centered
        canvas.drawBitmap(
            circularBitmap,
            padding / 2f,
            padding / 2f,
            null
        )

        return glowBitmap
    }

    private fun getMedianColor(bitmap: Bitmap): Int {
        // Sample every 4th pixel for efficiency
        val sampleStep = 4
        val reds = mutableListOf<Int>()
        val greens = mutableListOf<Int>()
        val blues = mutableListOf<Int>()

        for (y in 0 until bitmap.height step sampleStep) {
            for (x in 0 until bitmap.width step sampleStep) {
                val color = bitmap.getPixel(x, y)
                if (Color.alpha(color) > 50) { // Ignore transparent pixels
                    reds.add(Color.red(color))
                    greens.add(Color.green(color))
                    blues.add(Color.blue(color))
                }
            }
        }

        return if (reds.isNotEmpty()) {
            reds.sort()
            greens.sort()
            blues.sort()
            Color.rgb(
                reds[reds.size / 2],
                greens[greens.size / 2],
                blues[blues.size / 2]
            )
        } else {
            Color.YELLOW // Fallback
        }
    }

}