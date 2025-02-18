package com.example.tomato

import android.util.Base64
import android.Manifest
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import android.widget.ViewSwitcher
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class UploadPostActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var imageAdapter: ImageAdapter
    private lateinit var uploadViewSwitch: ViewSwitcher
    private var postVisibility: String = ""

    /** Variables that are related to post's component **/
    private val imageUris = mutableListOf<Uri>()
    private var postLatitude: Double = 0.0
    private var postLongitude: Double = 0.0
    private var postDate: String = ""

    companion object {
        private const val TAG = "UploadPostActivity"
    }

    // Register post Visibility launcher (the form to obtain the post's visibility)
    private val postVisibilityActivityLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            postVisibility = data?.getStringExtra("visibility") ?: ""
        }

        if(postVisibility != ""){
            updateVisibility()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_upload_post)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        initImageViewer()
        addClickListenersToPostInfoButtons()
    }

    /**
     * Add click listeners to the post info buttons (location, visibility, date).
     */
    private fun addClickListenersToPostInfoButtons() {
        // Add click listener to the + button (add photos) so it opens album/gallery.
        val addPhotosButton = findViewById<ImageView>(R.id.addPhotoButton)
        addPhotosButton.setOnClickListener {
            getMultipleImagesLauncher.launch("image/*")
        }

        // Add click listener to the location button (set location for the post)
        val locationButton = findViewById<LinearLayout>(R.id.addLocation)
        locationButton.setOnClickListener {
            //TODO: Add location search/pinpoint to choose location
            lifecycleScope.launch {
                val (latitude, longitude) = getCurrentLocation()
                updateLocation(latitude, longitude)
                postLatitude = latitude
                postLongitude = longitude
            }
        }

        // Add click listener for Visibility button
        val visibilityButton = findViewById<LinearLayout>(R.id.setVisibility)
        visibilityButton.setOnClickListener {
            val intent = Intent(this, ChoosePostVisibilityActivity::class.java)
            postVisibilityActivityLauncher.launch(intent)
        }

        // Add click listener for Date button
        val dateButton = findViewById<LinearLayout>(R.id.setDate)
        dateButton.setOnClickListener {
            // Get the current date from Calendar instance
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            // Create a DatePickerDialog
            val datePickerDialog = DatePickerDialog(
                this,
                { _, selectedYear, selectedMonth, selectedDay ->
                    // This block is executed when the user selects a date.
                    // Note: selectedMonth is zero-based (0 = January, 11 = December)
                    val chosenDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                    postDate = chosenDate
                    updateDate()
                },
                year, month, day
            )

            // Show the DatePickerDialog
            datePickerDialog.show()
        }

        // Add click listener to upload post
        val uploadPostButton = findViewById<Button>(R.id.upload_post_button)
        uploadPostButton.setOnClickListener {
            uploadPost()
        }
    }

    /**
     * Send a POST request to the server to upload post.
     * TODO: ADD SOME BASIC CHECKS (EG: NO EMPTY FIELDS) BEFORE SENDING
     */
    private fun uploadPost(){
        // Convert ImageURIs to Bytes (Raw data)
        val imageBytes: List<ByteArray> =  imageUris.mapNotNull { uri ->
            contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.readBytes()
            }
        }

        // Convert the Bytes to Base64 encoded strings
        val base64Strings: List<String> = imageBytes.map { bytes ->
            Base64.encodeToString(bytes, Base64.NO_WRAP)
        }

        // JSON-ify the base64strings array so it can be parsed easily on the server
        val imageArray = JSONArray(base64Strings)


        val note = findViewById<TextView>(R.id.noteText).text.toString()
        val postIsPrivate = postVisibility == "Private"
        val dateFormatter = SimpleDateFormat("dd/MM/yyyy")
        val date: Date? = dateFormatter.parse(postDate)

        val body = JSONObject()
        .put("latitude", postLatitude)
        .put("longitude", postLongitude)
        .put("images", imageArray)
        .put("date", date)
        .put("note", note)
        .put("private", postIsPrivate)
        .toString()

        lifecycleScope.launch {
            val response = HTTPRequest.sendPostRequest("${BuildConfig.SERVER_ADDRESS}/posts",
                body, this@UploadPostActivity)
            Log.d(TAG, "Response: $response")
            Log.d(TAG, "JSON Body: $body")

            //TODO: Handle response
            if(response != null){
                Toast.makeText(this@UploadPostActivity, "Post uploaded successfully", Toast.LENGTH_SHORT).show()
            }
            else{
                Toast.makeText(this@UploadPostActivity, "Post upload failed", Toast.LENGTH_SHORT).show()
            }
        }
    }
    /**
     * Translate latitude and longitude to address and display it to the user.
     */
    private fun updateLocation(latitude: Double, longitude: Double) {
        val geocoder = Geocoder(this, java.util.Locale("en", "US"))
        try {
            val addresses: MutableList<android.location.Address>? = geocoder.getFromLocation(latitude, longitude, 1)
            Log.d(TAG, "latitude: $latitude, longitude: $longitude")
            Log.d(TAG, "Address: $addresses")
            if (addresses != null) {
                if (addresses.isNotEmpty()) {
                    setLogoToBlue(R.drawable.upload_post_location, R.id.setLocationImage)
                    val fullAddress = commonFunction.parseLocation(latitude, longitude, this)
                    val locationText = findViewById<TextView>(R.id.setLocationText)
                    locationText.text = fullAddress
                } else {
                    Log.e("GeocoderError", "No address found for this location.")
                }
            }
        } catch (e: Exception) {
            Log.e("GeocoderError", "Error fetching address: ${e.message}")
        }

    }

    /**
     * Update the visibility text and set the logo to blue.
     * @requires postVisibility to be updated before calling this function.
     */
    private fun updateVisibility(){
        setLogoToBlue(R.drawable.visibility, R.id.setVisibilityImage)
        val visibilityText = findViewById<TextView>(R.id.setVisibilityText)
        visibilityText.text = postVisibility

    }

    /**
     * Update the date text and set the logo to blue.
     * @requires postDate to be updated before calling this function.
     */
    private fun updateDate(){
        val dateText = findViewById<TextView>(R.id.setDateText)
        dateText.text = postDate
        setLogoToBlue(R.drawable.upload_post_date, R.id.setDateImage)
    }

    /**
     * Initialize the image viewer/ViewSwitch where the default view is the empty box.
     * Otherwise shows the RecyclerView of uploaded images.
     */
    private fun initImageViewer(){
        // Init viewSwitcher & Show the empty view
        uploadViewSwitch = findViewById(R.id.uploadViewSwitch)
        uploadViewSwitch.displayedChild = 0

        // Set up RecyclerView (the horizontal scrollable view of the uploaded images)
        recyclerView = findViewById(R.id.uploaded_image_recycler)
        recyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        imageAdapter = ImageAdapter(imageUris, ::updateViewSwitch)
        recyclerView.adapter = imageAdapter

    }


    private fun setLogoToBlue(logoID: Int, imageID: Int){
        val logo = ContextCompat.getDrawable(this, logoID)
        val image = findViewById<ImageView>(imageID)

        val color = ContextCompat.getColor(this, R.color.blue)
        logo?.setTint(color)
        image.setImageDrawable(logo)
    }


    /**
     * Update the UI based on whether there are uploaded images.
     * UI is set to default empty box when there are no uploaded images.
     * Otherwise shows the RecyclerView of uploaded images.
     *
     * @requires default empty box display to be at index 0 of the ViewSwitcher
     * and the uploaded images view at index 1.
     */
    private fun updateViewSwitch(){
        if(imageUris.isEmpty()){
            uploadViewSwitch.displayedChild = 0
        }
        else{
            uploadViewSwitch.displayedChild = 1
        }
    }
    /**
     * Launches the image picker (album) to select multiple images.
     */
    private val getMultipleImagesLauncher = registerForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) {
        uris: List<Uri> ->
        // Filter out duplicate images
        val newUris = uris.filter { it !in imageUris }

        // Warn users if there are images not uploaded due to duplication
        if (newUris.count() != uris.count()) {
            val duplicateCount = uris.count() - newUris.count()
            Toast.makeText(
                this,
                "$duplicateCount duplicate images are not uploaded",
                Toast.LENGTH_SHORT
            ).show()
        }
        imageAdapter.addImage(newUris)
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

}

/**
 * Manages the uploaded imageps of the RecyclerView (the horizontal scrollable view of uploaded images).
 */
class ImageAdapter(private val imageUris: MutableList<Uri>, private val updateUI: () -> Unit) : RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {

    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.postImage)
        val removeButton: ImageButton = itemView.findViewById(R.id.removeImageButton)
    }

    /**
     * Customize the view to the image item on the RecyclerView.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.upload_post_image_item, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.imageView.setImageURI(imageUris[position])
        holder.removeButton.setOnClickListener() {
            removeImage(position)

        }
    }

    override fun getItemCount(): Int = imageUris.size

    fun addImage(newUris: List<Uri>) {
        imageUris.addAll(newUris)
        notifyItemRangeInserted(imageUris.size - newUris.size, newUris.size)
        updateUI()
    }

    fun removeImage(position: Int) {
        imageUris.removeAt(position)
        notifyItemRemoved(position)   // Notify RecyclerView about the removal
        notifyItemRangeChanged(position, imageUris.size)  // Update indexes of remaining items
        updateUI()
    }
}
