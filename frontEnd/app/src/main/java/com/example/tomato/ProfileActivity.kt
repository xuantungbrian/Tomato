package com.example.tomato

import android.graphics.BitmapFactory
import android.location.Geocoder
import android.media.Image
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import kotlinx.coroutines.launch
import java.util.Locale


data class PostItem(val imageData: ByteArray, val location: String)
data class PostItemRaw(
    val images: List<PostImage>,
    val latitude: Double,
    val longitude: Double
)

data class PostImage(
    val fileData: FileData,
    val fileType: String
)

data class FileData(
    val type: String,
    val data: List<Int>
)

class ProfileActivity : AppCompatActivity() {

    companion object{
        private const val TAG = "ProfileActivity"
    }
    private val progressBar: View by lazy { findViewById(R.id.YourPostProgress) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Show the progress bar
        progressBar.visibility = View.VISIBLE

        // Initialize Recycler views for "Your Post" & "Recommendations"
        lifecycleScope.launch {
            var yourPostList: List<PostItem>? = getYourPostList()
            val yourPostRecyclerView = findViewById<RecyclerView>(R.id.yourPostRecycler)
            yourPostRecyclerView.layoutManager = LinearLayoutManager(this@ProfileActivity, LinearLayoutManager.HORIZONTAL, false)
            if (yourPostList != null) {
                val yourPostAdapter = PostAdapter(yourPostList)
                yourPostRecyclerView.adapter = yourPostAdapter
            }

        }
    }

    /**
     * Obtain the list of PostItem of current logged in user.
     */
    suspend fun getYourPostList(): List<PostItem>? {

        val response = HTTPRequest.sendGetRequest("${BuildConfig.SERVER_ADDRESS}/posts",
            this@ProfileActivity)

        val gson = Gson()
        val posts = gson.fromJson(response, Array<PostItemRaw>::class.java)

        val postList = mutableListOf<PostItem>()
        val geocoder = Geocoder(this, java.util.Locale("en", Locale.getDefault().country))

        for (post in posts){
            val imageData = post.images.firstOrNull()?.fileData?.data?.map { it.toByte() }?.toByteArray()
            val location = geocoder.getFromLocation(post.latitude, post.longitude, 1)
            Log.d(TAG, "latitude: ${post.latitude}, longitude: ${post.longitude}")
            Log.d(TAG, "Location: $location")
            if (location != null){
                Log.d(TAG, "Location: $location")
                Log.d(TAG, "Image: ${imageData?.size}")
                val address = commonFunction.parseLocation(post.latitude, post.longitude, this)
                postList.add(PostItem(imageData!!, address))

                // Load is successful, remove progressBar
                progressBar.visibility = View.GONE
            }
        }

        return postList
    }
}

class PostAdapter(
    private val postList: List<PostItem>
) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    // 1. Define a ViewHolder
    class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val postImage: ImageView = itemView.findViewById(R.id.postImage)
        private val postLocation: TextView = itemView.findViewById(R.id.postLocation)

        fun bind(post: PostItem) {
            // Convert ByteArray to a Bitmap ( raw bytes)
            val bitmap = BitmapFactory.decodeByteArray(post.imageData, 0, post.imageData.size)
            postImage.setImageBitmap(bitmap)

            // Set the date text
            postLocation.text = post.location
        }
    }

    // 2. Inflate item layout
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.profile_post_item, parent, false)
        return PostViewHolder(view)
    }

    // 3. Bind each item
    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(postList[position])
    }

    override fun getItemCount(): Int = postList.size
}
