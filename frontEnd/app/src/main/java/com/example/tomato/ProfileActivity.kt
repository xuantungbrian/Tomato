package com.example.tomato

import android.content.Intent
import android.graphics.BitmapFactory
import android.location.Geocoder
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
import com.google.gson.Gson
import kotlinx.coroutines.launch
import java.util.Locale



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
                val yourPostAdapter = ProfilePostAdapter(yourPostList)
                yourPostRecyclerView.adapter = yourPostAdapter
            }

        }
    }

    /**
     * Obtain the list of PostItem uploaded by current logged in user.
     */
    suspend fun getYourPostList(): List<PostItem>? {

        val response = HTTPRequest.sendGetRequest("${BuildConfig.SERVER_ADDRESS}/posts",
            this@ProfileActivity)

        val gson = Gson()
        val posts = gson.fromJson(response, Array<PostItemRaw>::class.java)

        val postList = mutableListOf<PostItem>()
        val geocoder = Geocoder(this, java.util.Locale("en", Locale.getDefault().country))

        for (post in posts){
            val imageData: MutableList<ByteArray> = mutableListOf()

            for (image in post.images){
                val imageBytes = image.fileData.data.map { it.toByte() }.toByteArray()
                imageData.add(imageBytes)
            }
            val imageURIs = commonFunction.byteToURIs(imageData, cacheDir, this)

            val location = geocoder.getFromLocation(post.latitude, post.longitude, 1)
            if (location != null){
                val address = commonFunction.parseLocation(post.latitude, post.longitude, this)
                postList.add(PostItem(imageURIs, address, post.date, post.note, post.private))

                // Load is successful, remove progressBar
                progressBar.visibility = View.GONE
            }


        }

        return postList
    }
}

class ProfilePostAdapter(
    private val postList: List<PostItem>
) : RecyclerView.Adapter<ProfilePostAdapter.PostViewHolder>() {

    // 1. Define a ViewHolder
    class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val postImage: ImageView = itemView.findViewById(R.id.profile_post_image)
        private val postLocation: TextView = itemView.findViewById(R.id.profile_post_location)

        fun bind(post: PostItem) {
            postImage.setImageURI(post.imageData[0])
            postLocation.text = post.location

            itemView.setOnClickListener{
                val intent = Intent(itemView.context, PostActivity::class.java)
                intent.putExtra("images", ArrayList(post.imageData))
                intent.putExtra("location", post.location)
                intent.putExtra("date", post.date)
                intent.putExtra("note", post.note)
                intent.putExtra("private", post.private)
                itemView.context.startActivity(intent)
            }
        }
    }

    // 2. Inflate (convert XML to viewable element) item layout
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
