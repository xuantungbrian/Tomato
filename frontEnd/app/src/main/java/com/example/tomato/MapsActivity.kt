package com.example.tomato

import android.Manifest
import JwtManager
import PostHelper
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapShader
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.RadialGradient
import android.graphics.Shader
import android.location.Location
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.tomato.databinding.ActivityMapsBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Marker
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.lang.Math.max
import java.lang.Math.min
import java.security.MessageDigest
import java.util.UUID

data class SignInResponse(val token: String, val userID: String)

// PARAMETERS
private val fetchDelay = 500 // Minimum idling time before fetching posts
private val postSize = 80 // The circular image size on the map
private val gridSize = 3 * postSize // Distance threshold in pixels for clustering
private var lastFetchJob: Job? = null

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private val activityScope = CoroutineScope(Dispatchers.Main)

    companion object {
        private const val TAG = "MapsActivity"


    }

    private var userPostOnly: Boolean = false


    //Location Search
    private lateinit var placesClient: PlacesClient
    private lateinit var sessionToken: AutocompleteSessionToken
    private lateinit var autoCompleteTextView: AutoCompleteTextView
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var clusterHelper: MapClusterHelper
    private var searchLatitude: Double? = null
    private var searchLongitude: Double? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, BuildConfig.MAP_API_KEY)
        }

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        //Init sign in button
        findViewById<Button>(R.id.sign_in_button).setOnClickListener {
            val credentialManager = CredentialManager.create(this)
            val signInWithGoogleOption = GetSignInWithGoogleOption.Builder(BuildConfig.WEB_CLIENT_ID)
                .setNonce(commonFunction.generateHashedNonce())
                .build()
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(signInWithGoogleOption)
                .build()

            activityScope.launch {
                try {
                    val result = credentialManager.getCredential(request = request, context = this@MapsActivity)
                    handleSignIn(result)
                } catch (e: GetCredentialException) {
                    Log.d(TAG, "Get credential exception", e)
                }
            }
        }

        //Init filter button
        findViewById<Button>(R.id.filter_post_button).setOnClickListener {
            val options = arrayOf("Show only your posts", "Show all viewable posts")

            AlertDialog.Builder(this@MapsActivity)
                .setTitle("Post Filter")
                .setItems(options) { _, which ->
                    userPostOnly = which == 0
                    clusterHelper.getPostsOnScreen(mMap, userPostOnly)
                }
                .setCancelable(true)
                .show()

        }

        commonFunction.initNavBarButtons(this@MapsActivity, this)

        // Update profile (show clickable image) if user is logged in
        updateProfile()

        // Init search location functionality
        initSearchLocation()

        // Init trigger list of chat layout
        initChatList()

        // Init user's current location and map display
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        // Request location permissions first
        if (LocationPermission.checkLocationPermission(this)) {
            initMap()
        } else {
            LocationPermission.requestLocationPermission(this)
        }

    }


    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initMap()
            }
        }
    }

    private fun initMap() {
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun getUserLocation() {
        if (LocationPermission.checkLocationPermission(this)) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                Log.d(TAG, "User location: $location")
                location?.let {
                    val userLatLng = LatLng(it.latitude, it.longitude)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 15f))
                }
            }
        }
    }


    private fun initChatList(){
        val chatButton = findViewById<ImageView>(R.id.map_activity_chat_button)
        chatButton.setOnClickListener {
            if (UserCredentialManager.isLoggedIn(this@MapsActivity)) {
                startActivity(Intent(this@MapsActivity, ChooseChatActivity::class.java))
            }
            else {
                android.app.AlertDialog.Builder(this@MapsActivity)
                    .setTitle("Login is required to chat with others")
                    .setNegativeButton("Okay", null)
                    .show()
            }
        }
    }
    private fun initSearchLocation(){
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
                            searchLatitude = it.latitude
                            searchLongitude = it.longitude
                        }
                    }
                    .addOnFailureListener { exception ->
                        // Handle failure
                    }
            },

        )

        autoCompleteTextView.setOnEditorActionListener { _, actionId, event ->
            val isEnterPressed = actionId == EditorInfo.IME_ACTION_SEARCH ||
                    actionId == EditorInfo.IME_ACTION_DONE ||
                    (event?.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_ENTER)
            if (isEnterPressed) {
                if (searchLatitude != null && searchLongitude != null) {
                    moveMapCameraTo(searchLatitude!!, searchLongitude!!)
                }
                autoCompleteTextView.clearFocus()
                autoCompleteTextView.clearComposingText()
                autoCompleteTextView.text = null

                true // Consume the event.
            } else {
                false
            }
        }
    }

    private fun moveMapCameraTo(latitude: Double, longitude: Double) {
        val latLng = LatLng(latitude, longitude)
        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15f)
        mMap.animateCamera(cameraUpdate)
    }


    private fun updateProfile(){
        val sign_in_button = findViewById<Button>(R.id.sign_in_button)
        val profile_button = findViewById<ImageView>(R.id.map_activity_profile_button)
        if (UserCredentialManager.isLoggedIn(this)) {
            sign_in_button.visibility = View.GONE
            profile_button.visibility = View.VISIBLE
            val (username, profilePicture) = UserCredentialManager.getUserProfile(this)
            Glide.with(this)
                .load(profilePicture)
                .into(profile_button)
            profile_button.setOnClickListener {
                startActivity(Intent(this, ProfileActivity::class.java))
            }
        }
        else{
            sign_in_button.visibility = View.VISIBLE
            profile_button.visibility = View.GONE
        }
    }

    private fun handleSignIn(result: GetCredentialResponse) {
        val credential = result.credential
        if (credential !is CustomCredential || credential.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            Log.e(TAG, "Unexpected type of credential")
            return
        }

        try {
            val googleCredential = GoogleIdTokenCredential.createFrom(credential.data)
            googleCredential.displayName?.let { username ->
                UserCredentialManager.saveUserProfile(this@MapsActivity, username, googleCredential.profilePictureUri.toString())
            }
            sendSignInRequest(googleCredential.idToken)
        } catch (e: GoogleIdTokenParsingException) {
            Log.e(TAG, "Received an invalid google id token response", e)
        }
    }

    private fun sendSignInRequest(token: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    1
                )
            }
        }
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                // Handle failure
                Log.w("Firebase", "Fetching FCM registration token failed", task.exception)
            }
            else {
                val body = JSONObject()
                    .put("googleToken", token)
                    .put("firebaseToken", task.result)
                    .toString()

                lifecycleScope.launch {
                    val response = HTTPRequest.sendPostRequest(
                        "${BuildConfig.SERVER_ADDRESS}/user/auth",
                        body,
                        this@MapsActivity
                    )
                    Log.d(TAG, "sendPostRequest: $response")
                    if (response != null) {
                        val signInResponse = Gson().fromJson(response, SignInResponse::class.java)
                        JwtManager.saveToken(this@MapsActivity, signInResponse.token)
                        val userID = signInResponse.userID
                        UserCredentialManager.saveUserId(this@MapsActivity, userID)
                        updateProfile()
                    }
                }
            }
        }
    }

    @SuppressLint("PotentialBehaviorOverride")
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        clusterHelper = MapClusterHelper(this, mMap)
        googleMap.setOnCameraIdleListener {
            clusterHelper.getPostsOnScreen(googleMap, userPostOnly)
        }

        if (LocationPermission.checkLocationPermission(this)) {
            mMap.isMyLocationEnabled = true
            getUserLocation()

        }

        mMap.setOnMarkerClickListener { marker ->
            Toast.makeText(this, "Loading post...", Toast.LENGTH_SHORT).show()
            val tag = marker.tag
            Log.d(TAG, "Marker clicked: $tag")
            if (tag is String) {
                Log.d(TAG, "Single post clicked: $tag")
                // TODO: Open post details using tag (post ID)
            } else if (tag is List<*>) {
                Log.d(TAG, "Aggregated marker clicked with ${tag.size} posts")
                // TODO: Handle aggregated marker click (e.g., zoom in or show list)
            }
            if((tag as List<PostItemRaw>).size == 1){
                PostHelper.showPostActivity(tag[0], this@MapsActivity)

            }
            else{
                clusterHelper.showClusterDialog(tag as List<PostItemRaw>)
            }
            true
        }
    }

}

class MapClusterHelper(private val activity: MapsActivity,
                        private val mMap: GoogleMap){

     fun showClusterDialog(posts: List<PostItemRaw>) {
        val dialog = Dialog(activity)
        dialog.setContentView(R.layout.map_activity_post_preview_dialog)

        val recyclerView = dialog.findViewById<RecyclerView>(R.id.post_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.adapter = PostClusterAdapter(posts, activity)

        val screenWidth = activity.resources.displayMetrics.widthPixels
        // Customize the dialog window
        dialog.window?.apply {
            setBackgroundDrawableResource(R.drawable.map_activity_dialog_background) // Set rounded background
            setLayout((screenWidth * 0.8).toInt(), WindowManager.LayoutParams.WRAP_CONTENT)
            decorView.setPadding(0, 0, 0, 0)
        }
        dialog.show()
    }

     fun getMedianColor(bitmap: Bitmap): Int {
        val sampleStep = 4
        val reds = mutableListOf<Int>()
        val greens = mutableListOf<Int>()
        val blues = mutableListOf<Int>()
        for (y in 0 until bitmap.height step sampleStep) {
            for (x in 0 until bitmap.width step sampleStep) {
                val color = bitmap.getPixel(x, y)
                if (Color.alpha(color) > 50) {
                    reds.add(Color.red(color))
                    greens.add(Color.green(color))
                    blues.add(Color.blue(color))
                }
            }
        }
        return if (reds.isNotEmpty()) {
            reds.sort(); greens.sort(); blues.sort()
            Color.rgb(reds[reds.size / 2], greens[greens.size / 2], blues[blues.size / 2])
        } else Color.YELLOW
    }




    private fun createCircularBitmap(source: Bitmap, targetSize: Int): Bitmap {
        val scale = if (source.width < source.height) targetSize.toFloat() / source.width else targetSize.toFloat() / source.height
        val scaledWidth = (source.width * scale).toInt()
        val scaledHeight = (source.height * scale).toInt()
        val scaledBitmap = Bitmap.createScaledBitmap(source, scaledWidth, scaledHeight, true)
        val startX = (scaledWidth - targetSize).coerceAtLeast(0) / 2
        val startY = (scaledHeight - targetSize).coerceAtLeast(0) / 2
        val safeWidth = min(targetSize, scaledWidth - startX)
        val safeHeight = min(targetSize, scaledHeight - startY)
        val croppedBitmap = Bitmap.createBitmap(scaledBitmap, startX, startY, safeWidth, safeHeight)
        scaledBitmap.recycle()
        val output = Bitmap.createBitmap(targetSize, targetSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        val paint = Paint().apply {
            isAntiAlias = true
            shader = BitmapShader(croppedBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        }
        canvas.drawCircle(targetSize / 2f, targetSize / 2f, targetSize / 2f, paint)
        croppedBitmap.recycle()
        return output
    }

    private fun addGlow(circularBitmap: Bitmap, glowColor: Int): Bitmap {
        val glowRadius = 50f
        val padding = (glowRadius * 2).toInt()
        val glowBitmap = Bitmap.createBitmap(
            circularBitmap.width + padding,
            circularBitmap.height + padding,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(glowBitmap)
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
        canvas.drawCircle(glowBitmap.width / 2f, glowBitmap.height / 2f, circularBitmap.width / 2f + glowRadius, paint)
        canvas.drawBitmap(circularBitmap, padding / 2f, padding / 2f, null)
        return glowBitmap
    }


    fun createMarkerBitmap(image: PostImage, count: Int = 1): Bitmap {

        fun Int.dpToPx(context: Context): Int = (this * context.resources.displayMetrics.density).toInt()

        val targetSize = postSize.dpToPx(activity)
        val imageByteArray = image.fileData.data.map { it.toByte() }.toByteArray()
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(imageByteArray, 0, imageByteArray.size, options)
        options.inSampleSize = commonFunction.calculateInSampleSize(options, targetSize, targetSize)
        options.inJustDecodeBounds = false
        val originalBitmap = BitmapFactory.decodeByteArray(imageByteArray, 0, imageByteArray.size, options)
            ?: return Bitmap.createBitmap(targetSize, targetSize, Bitmap.Config.ARGB_8888)
        val circularBitmap = createCircularBitmap(originalBitmap, targetSize)
        originalBitmap.recycle()
        val dominantColor = getMedianColor(circularBitmap)
        val darkerDominantColor = darkenColor(dominantColor, 0.35f)
        val glowBitmap = addGlow(circularBitmap, darkerDominantColor)
        circularBitmap.recycle()
        val finalBitmap = if (count > 1) {
            addBadge(glowBitmap, count)
        } else {
            glowBitmap
        }
        return finalBitmap
    }
    fun darkenColor(color: Int, factor: Float): Int {
        // Ensure the factor is within a sensible range
        val safeFactor = factor.coerceIn(0f, 1f)
        val r = (Color.red(color) * safeFactor).toInt().coerceIn(0, 255)
        val g = (Color.green(color) * safeFactor).toInt().coerceIn(0, 255)
        val b = (Color.blue(color) * safeFactor).toInt().coerceIn(0, 255)
        return Color.rgb(r, g, b)
    }


    private fun addBadge(baseBitmap: Bitmap, count: Int): Bitmap {
        val badgeSize = 75 // Size of the badge in pixels
        val badgePaint = Paint().apply {
            color = Color.RED
            style = Paint.Style.FILL
        }
        val textPaint = Paint().apply {
            color = Color.WHITE
            textSize = 40f
            textAlign = Paint.Align.CENTER
        }

        val bitmap = Bitmap.createBitmap(baseBitmap.width, baseBitmap.height, baseBitmap.config!!)
        val canvas = Canvas(bitmap)
        canvas.drawBitmap(baseBitmap, 0f, 0f, null)
        val badgeX = baseBitmap.width - badgeSize / 1.25f
        val badgeY = badgeSize / 1.25f
        canvas.drawCircle(badgeX, badgeY, badgeSize / 2f, badgePaint)
        canvas.drawText(count.toString(), badgeX, badgeY + 5f, textPaint) // Adjust Y for text centering
        baseBitmap.recycle()
        return bitmap
    }

    fun showSinglePostMarker(post: PostItemRaw, location: LatLng): Marker {
        val firstImage = post.images[0]
        val bitmap = createMarkerBitmap(firstImage)
        return mMap.addMarker(
            MarkerOptions()
                .position(location)
                .icon(BitmapDescriptorFactory.fromBitmap(bitmap))
        )!!
    }

    fun showAggregatedMarker(representativePost: PostItemRaw, count: Int, location: LatLng): Marker {
        val firstImage = representativePost.images[0]
        val bitmap = createMarkerBitmap(firstImage, count)
        return mMap.addMarker(
            MarkerOptions()
                .position(location)
                .icon(BitmapDescriptorFactory.fromBitmap(bitmap))
        )!!
    }

    fun getPostsOnScreen(googleMap: GoogleMap, userPostOnly: Boolean = false) {
        lastFetchJob?.cancel()
        lastFetchJob = activity.lifecycleScope.launch {
            delay(fetchDelay.toLong())
            val visibleRegion = googleMap.projection.visibleRegion
            val startLat = min(visibleRegion.farLeft.latitude, visibleRegion.nearRight.latitude)
            val endLat = max(visibleRegion.farLeft.latitude, visibleRegion.nearRight.latitude)
            val startLong = min(visibleRegion.farLeft.longitude, visibleRegion.nearRight.longitude)
            val endLong = max(visibleRegion.farLeft.longitude, visibleRegion.nearRight.longitude)
            var url = "${BuildConfig.SERVER_ADDRESS}/posts?" +
                    "start_lat=$startLat&end_lat=$endLat&" +
                    "start_long=$startLong&end_long=$endLong"
            if(UserCredentialManager.isLoggedIn(activity)){
                url = "${BuildConfig.SERVER_ADDRESS}/posts-authenticated?" +
                        "userPostOnly=$userPostOnly&" +
                        "start_lat=$startLat&end_lat=$endLat&" +
                        "start_long=$startLong&end_long=$endLong"
            }

            val response = withContext(Dispatchers.IO) {
                HTTPRequest.sendGetRequest(url, activity)
            }

            if (response != null) {

                val gson = Gson()
                val postArray = gson.fromJson(response, Array<PostItemRaw>::class.java)
                val projection = mMap.projection
                val clusters = mutableMapOf<Pair<Int, Int>, MutableList<PostItemRaw>>()

                // Group posts into grid cells based on screen coordinates
                for (post in postArray) {
                    val screenPoint = projection.toScreenLocation(LatLng(post.latitude, post.longitude))
                    val cellX = (screenPoint.x / gridSize).toInt()
                    val cellY = (screenPoint.y / gridSize).toInt()
                    val key = Pair(cellX, cellY)
                    clusters.getOrPut(key) { mutableListOf() }.add(post)
                }

                // Clear existing markers and add new ones
                mMap.clear()
                for (cluster in clusters.values) {
                    if (cluster.size == 1) {
                        val post = cluster[0]
                        val postLocation = LatLng(post.latitude, post.longitude)
                        val marker = showSinglePostMarker(post, postLocation)
                        marker.tag = cluster
                    } else {
                        val averageLat = cluster.map { it.latitude }.average()
                        val averageLng = cluster.map { it.longitude }.average()
                        val location = LatLng(averageLat, averageLng)
                        val representativePost = cluster[0] // Use first post as representative
                        val marker = showAggregatedMarker(representativePost, cluster.size, location)
                        marker.tag = cluster
                    }
                }
            }
        }
    }



}

class PostClusterAdapter(
    private val posts: List<PostItemRaw>,
    private val context: Context
) : RecyclerView.Adapter<PostClusterAdapter.PostViewHolder>() {

    class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.map_activity_post_image)
        val locationTextView: TextView = itemView.findViewById(R.id.map_activity_post_location)

        fun bind(post: PostItemRaw){
            val image = post.images[0]
            val byteArray = image.fileData.data.map { it.toByte() }.toByteArray()
            val options = BitmapFactory.Options().apply {
                inSampleSize = calculateInSampleSize(byteArray, 100, 100)
            }
            val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size, options)

            imageView.setImageBitmap(bitmap)
            val location = commonFunction.parseLocation(post.latitude, post.longitude, itemView.context)
            locationTextView.text = location


            itemView.setOnClickListener{
                PostHelper.showPostActivity(post, itemView.context)
            }

            itemView.setOnTouchListener { _, event ->

                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        // Apply a dark overlay
                        imageView.setColorFilter(Color.argb(50, 0, 0, 0))
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        // Clear the overlay
                        imageView.clearColorFilter()
                    }

                    MotionEvent.ACTION_BUTTON_PRESS -> {
                        itemView.performClick()
                    }
                }
                // Return false to allow the click event to be processed
                false
            }

        }
        private fun calculateInSampleSize(byteArray: ByteArray, reqWidth: Int, reqHeight: Int): Int {
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size, options)
            val (height: Int, width: Int) = options.run { outHeight to outWidth }
            var inSampleSize = 1
            if (height > reqHeight || width > reqWidth) {
                val halfHeight = height / 2
                val halfWidth = width / 2
                while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                    inSampleSize *= 2
                }
            }
            return inSampleSize
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.map_activity_post_item, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(posts[position])
    }

    override fun getItemCount(): Int = posts.size

}


