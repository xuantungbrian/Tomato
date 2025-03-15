package com.example.tomato

import PostHelper
import ChatItem
import android.content.Intent
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
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.gson.Gson
import kotlinx.coroutines.launch
import org.json.JSONObject

data class RecommendationResponse(
    val posts: List<PostItemRaw>
)

class ProfileActivity : AppCompatActivity() {

    companion object{
        private const val TAG = "ProfileActivity"
    }
    private val yourPostProgress: View by lazy { findViewById(R.id.YourPostProgress) }
    private val recommendationProgress: View by lazy { findViewById(R.id.recommendationProgress) }

    private lateinit var gso: GoogleSignInOptions
    private lateinit var mGoogleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize Google Sign-In options
        gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        // Update user's profile if user is signed in
        updateProfile()

        // Show the progress wheel
        yourPostProgress.visibility = View.VISIBLE

        commonFunction.initNavBarButtons(this@ProfileActivity, this)

        // Initialize Recycler views for "Your Post" & "Recommendations"
        lifecycleScope.launch {
            initYourPost()
            initRecommendation()
        }

        // Initialize sign out button
        val signOutButton = findViewById<TextView>(R.id.sign_out_button)
        signOutButton.setOnClickListener {
            signOutFromGoogle()
        }
    }

    private suspend fun initRecommendation(){
        var recommendationList: List<PostItem>? = getRecommendationList()
        val recommendationRecyclerView = findViewById<RecyclerView>(R.id.recommendationRecyclerView)
        recommendationRecyclerView.layoutManager = LinearLayoutManager(this@ProfileActivity, LinearLayoutManager.HORIZONTAL, false)
        if (recommendationList != null) {
            val recommendationAdapter = ProfilePostAdapter(recommendationList)
            recommendationRecyclerView.adapter = recommendationAdapter
        }
    }

    private suspend fun getRecommendationList(): List<PostItem>? {
        val response = HTTPRequest.sendGetRequest("${BuildConfig.SERVER_ADDRESS}/recommendations",
            this@ProfileActivity)

        Log.d(TAG, "Response: $response")

        val gson = Gson()
        val posts = gson.fromJson(response, RecommendationResponse::class.java).posts

        val postList = mutableListOf<PostItem>()

        for (post in posts){
            postList.add(PostHelper.rawPostToPostItem(post, this))
        }

        // Load is successful, remove progressBar
        recommendationProgress.visibility = View.GONE


        return postList

    }

    private suspend fun initYourPost(){
        var yourPostList: List<PostItem>? = getYourPostList()
        val yourPostRecyclerView = findViewById<RecyclerView>(R.id.yourPostRecycler)
        yourPostRecyclerView.layoutManager = LinearLayoutManager(this@ProfileActivity, LinearLayoutManager.HORIZONTAL, false)
        if (yourPostList != null) {
            val yourPostAdapter = ProfilePostAdapter(yourPostList)
            yourPostRecyclerView.adapter = yourPostAdapter
        }

    }

    private fun signOutFromGoogle(){
        mGoogleSignInClient.signOut()
            .addOnCompleteListener(this) {
                Log.d(TAG, "User signed out from Google")
                UserCredentialManager.clearCredentials(this)
                val intent = Intent(this, MapsActivity::class.java)
                startActivity(intent)
                finish()
            }
    }


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

        val response = HTTPRequest.sendGetRequest("${BuildConfig.SERVER_ADDRESS}/posts-authenticated/?userPostOnly=true",
            this@ProfileActivity)

        val gson = Gson()
        val posts = gson.fromJson(response, Array<PostItemRaw>::class.java)
        Log.d(TAG, "Response: $response")

        val postList = mutableListOf<PostItem>()

        for (post in posts){
            postList.add(PostHelper.rawPostToPostItem(post, this))
        }

        // Load is successful, remove progressBar
        yourPostProgress.visibility = View.GONE

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
            if (post.imageData.isNotEmpty()) {
                postImage.setImageURI(post.imageData[0])
            }
            postLocation.text = post.location

            itemView.setOnClickListener{
                val intent = Intent(itemView.context, PostActivity::class.java)
                intent.putExtra("postId", post.postId)
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