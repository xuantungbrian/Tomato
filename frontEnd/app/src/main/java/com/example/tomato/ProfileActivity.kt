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
import com.google.gson.Gson
import kotlinx.coroutines.launch

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

        // Show the progress wheel
        progressBar.visibility = View.VISIBLE

        commonFunction.initNavBarButtons(this@ProfileActivity, this)

        // Initialize Recycler views for "Your Post" & "Recommendations"
        lifecycleScope.launch {
            var yourPostList: List<PostItem>? = getYourPostList()
            val yourPostRecyclerView = findViewById<RecyclerView>(R.id.yourPostRecycler)
            yourPostRecyclerView.layoutManager = LinearLayoutManager(this@ProfileActivity, LinearLayoutManager.HORIZONTAL, false)
            if (yourPostList != null) {
                val yourPostAdapter = ProfilePostAdapter(yourPostList)
                yourPostRecyclerView.adapter = yourPostAdapter
            }

            val chatList: List<ChatItem> = getChatList()
            Log.d(TAG, "chatlist: $chatList")
            val chatRecyclerView = findViewById<RecyclerView>(R.id.chatRecyclerView)
            chatRecyclerView.layoutManager = LinearLayoutManager(this@ProfileActivity)
            val chatAdapter = ChatAdapter(chatList)
            chatRecyclerView.adapter = chatAdapter
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

        val response = HTTPRequest.sendGetRequest("${BuildConfig.SERVER_ADDRESS}/posts-authenticated/?userPostOnly=true",
            this@ProfileActivity)

        val gson = Gson()
        val posts = gson.fromJson(response, Array<PostItemRaw>::class.java)

        val postList = mutableListOf<PostItem>()

        Log.d(TAG, "POSTS: $posts")
        for (post in posts){
            postList.add(PostHelper.rawPostToPostItem(post, this))
        }

        // Load is successful, remove progressBar
        progressBar.visibility = View.GONE


        return postList
    }

    /**
     * Fetch the list of chats.
     */
    private suspend fun getChatList(): List<ChatItem> {
        val response = HTTPRequest.sendGetRequest(
            "${BuildConfig.SERVER_ADDRESS}/chats",
            this@ProfileActivity
        )
        Log.d(TAG, "message: $response")
        val chats = Gson().fromJson(response, Array<ChatItem>::class.java)

        val chatList = mutableListOf<ChatItem>()
        for (chat in chats) {
            chatList.add(ChatItem(chat.chatId, chat.member_1, chat.member_2))
        }
        return chatList
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

class ChatAdapter(private val chatList: List<ChatItem>) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {
    private lateinit var currentUserId: String

    class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val senderTextView: TextView = itemView.findViewById(R.id.chatTargetUser)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.chat_item, parent, false)
        currentUserId = UserCredentialManager.getUserId(parent.context) ?: ""
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chatItem = chatList[position]
        if (chatItem.member_1 == currentUserId) {
            holder.senderTextView.text = chatItem.member_2
        } else {
            holder.senderTextView.text = chatItem.member_1
        }
        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, ChatActivity::class.java)
            intent.putExtra("chatId", chatItem.chatId)
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = chatList.size
}

