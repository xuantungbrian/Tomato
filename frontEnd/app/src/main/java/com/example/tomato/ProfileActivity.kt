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
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
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

        // Update user's profile if user is signed in
        updateProfile()

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
     * Update profile's username and image based on the current logged in user.
     */
    private fun updateProfile(){
        val usernameView = findViewById<TextView>(R.id.profile_activity_username)
        val profileImageView = findViewById<ImageView>(R.id.profile_activity_profile_image)

        val (username, profileImageURI) = UserCredentialManager.getUserProfile(this)
        usernameView.text = username
        if (profileImageURI != null) {
            Glide.with(this)
                .load(profileImageURI)
                .into(profileImageView)
        }
    }

    /**
     * Obtain the list of PostItem uploaded by current logged in user.
     */
    suspend fun getYourPostList(): List<PostItem>? {

        val response = HTTPRequest.sendGetRequest("${BuildConfig.SERVER_ADDRESS}/posts/user",
            this@ProfileActivity)

        val gson = Gson()
        val posts = gson.fromJson(response, Array<PostItemRaw>::class.java)

        val postList = mutableListOf<PostItem>()

        for (post in posts){
            postList.add(commonFunction.rawPostToPostItem(post, this))
        }

        // Load is successful, remove progressBar
        progressBar.visibility = View.GONE


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
                intent.putExtra("userId", post.userId)
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
