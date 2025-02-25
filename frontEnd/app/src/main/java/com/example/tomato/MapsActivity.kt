package com.example.tomato

import JwtManager
import android.annotation.SuppressLint
import android.app.Dialog
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
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tomato.databinding.ActivityMapsBinding
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
    private val fetchDelay = 500 // Minimum idling time before fetching posts
    private val postSize = 80 // The circular image size on the map
    private val gridSize = 3 * postSize // Distance threshold in pixels for clustering

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

        findViewById<Button>(R.id.sign_in_button).setOnClickListener {
            val credentialManager = CredentialManager.create(this)
            val signInWithGoogleOption = GetSignInWithGoogleOption.Builder(BuildConfig.WEB_CLIENT_ID)
                .setNonce(generateHashedNonce())
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

        findViewById<FloatingActionButton>(R.id.bottom_navbar_upload_button).setOnClickListener {
            startActivity(Intent(this, UploadPostActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.bottom_navbar_home_button).setOnClickListener {
            lifecycleScope.launch {
                val response = HTTPRequest.sendGetRequest("http://10.0.2.2:3000/test1", this@MapsActivity)
                Log.d(TAG, "onCreate: $response")
            }
        }

        findViewById<LinearLayout>(R.id.bottom_navbar_profile_button).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }

    private fun handleSignIn(result: GetCredentialResponse) {
        val credential = result.credential
        when (credential) {
            is CustomCredential -> {
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data).idToken
                        sendSignInRequest(googleIdTokenCredential)
                    } catch (e: GoogleIdTokenParsingException) {
                        Log.e(TAG, "Received an invalid google id token response", e)
                    }
                }
            }
            else -> Log.e(TAG, "Unexpected type of credential")
        }
    }

    private fun sendSignInRequest(token: String) {
        val body = JSONObject().put("token", token).toString()
        lifecycleScope.launch {
            val response = HTTPRequest.sendPostRequest("${BuildConfig.SERVER_ADDRESS}/user/auth", body, this@MapsActivity)
            Log.d(TAG, "sendPostRequest: $response")
            if (response != null) {
                val signInResponse = Gson().fromJson(response, SignInResponse::class.java)
                JwtManager.saveToken(this@MapsActivity, signInResponse.token)
                val userID = signInResponse.userID
                commonFunction.saveUserId(this@MapsActivity, userID)
                Log.d(TAG, "sendSignInResponse: $signInResponse")
                Log.d(TAG, "USERID: ${commonFunction.getUserId(this@MapsActivity)}")
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

    @SuppressLint("PotentialBehaviorOverride")
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        googleMap.setOnCameraIdleListener {
            getPostsOnScreen(googleMap)
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
                showPostActivity(tag[0], this@MapsActivity)

            }
            else{
                showClusterDialog(tag as List<PostItemRaw>)
            }

            true
        }
    }



    private fun getPostsOnScreen(googleMap: GoogleMap) {
        lastFetchJob?.cancel()
        lastFetchJob = lifecycleScope.launch {
            delay(fetchDelay.toLong())
            val visibleRegion = googleMap.projection.visibleRegion
            val startLat = min(visibleRegion.farLeft.latitude, visibleRegion.nearRight.latitude)
            val endLat = max(visibleRegion.farLeft.latitude, visibleRegion.nearRight.latitude)
            val startLong = min(visibleRegion.farLeft.longitude, visibleRegion.nearRight.longitude)
            val endLong = max(visibleRegion.farLeft.longitude, visibleRegion.nearRight.longitude)
            val url = "${BuildConfig.SERVER_ADDRESS}/posts?" +
                    "start_lat=$startLat&end_lat=$endLat&" +
                    "start_long=$startLong&end_long=$endLong"

            val response = withContext(Dispatchers.IO) {
                HTTPRequest.sendGetRequest(url, this@MapsActivity)
            }

            if (response != null) {
                try {
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
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.e(TAG, "Error parsing response: ${e.message}")
                }
            }
        }
    }

    private fun showSinglePostMarker(post: PostItemRaw, location: LatLng): Marker {
        val firstImage = post.images[0]
        val bitmap = createMarkerBitmap(firstImage)
        return mMap.addMarker(
            MarkerOptions()
                .position(location)
                .icon(BitmapDescriptorFactory.fromBitmap(bitmap))
        )!!
    }

    private fun showAggregatedMarker(representativePost: PostItemRaw, count: Int, location: LatLng): Marker {
        val firstImage = representativePost.images[0]
        val bitmap = createMarkerBitmap(firstImage, count)
        return mMap.addMarker(
            MarkerOptions()
                .position(location)
                .icon(BitmapDescriptorFactory.fromBitmap(bitmap))
        )!!
    }

    private fun createMarkerBitmap(image: PostImage, count: Int = 1): Bitmap {
        val targetSize = postSize.dpToPx(this)
        val imageByteArray = image.fileData.data.map { it.toByte() }.toByteArray()
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(imageByteArray, 0, imageByteArray.size, options)
        options.inSampleSize = calculateInSampleSize(options, targetSize, targetSize)
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

    fun Int.dpToPx(context: Context): Int = (this * context.resources.displayMetrics.density).toInt()

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val heightRatio = height.toFloat() / reqHeight.toFloat()
            val widthRatio = width.toFloat() / reqWidth.toFloat()
            inSampleSize = max(heightRatio, widthRatio).toInt()
        }
        return inSampleSize
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

    private fun getMedianColor(bitmap: Bitmap): Int {
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

    private fun showClusterDialog(posts: List<PostItemRaw>) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.map_activity_post_preview_dialog)

        val recyclerView = dialog.findViewById<RecyclerView>(R.id.post_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.adapter = PostClusterAdapter(posts, this)

        val screenWidth = resources.displayMetrics.widthPixels
        // Customize the dialog window
        dialog.window?.apply {
            setBackgroundDrawableResource(R.drawable.map_activity_dialog_background) // Set rounded background
            setLayout((screenWidth * 0.8).toInt(), WindowManager.LayoutParams.WRAP_CONTENT)
            decorView.setPadding(0, 0, 0, 0)
        }
        dialog.show()
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
                showPostActivity(post, itemView.context)
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

fun showPostActivity(post: PostItemRaw, context: Context){
    val postItem = commonFunction.rawPostToPostItem(post, context)

    val intent = Intent(context, PostActivity::class.java)
    intent.putExtra("userId", postItem.userId)
    intent.putExtra("images", ArrayList(postItem.imageData))
    intent.putExtra("location", postItem.location)
    intent.putExtra("date", postItem.date)
    intent.putExtra("note", postItem.note)
    intent.putExtra("private", postItem.private)
    context.startActivity(intent)
}

