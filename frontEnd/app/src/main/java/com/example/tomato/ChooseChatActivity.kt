package com.example.tomato

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
import androidx.recyclerview.widget.RecyclerView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class ChooseChatActivity : AppCompatActivity() {
    private val TAG = "ChooseChatActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_choose_chat)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        lifecycleScope.launch {
            val chatRooms = getChatList()
            val recyclerView = findViewById<RecyclerView>(R.id.chatList)
            Log.d(TAG, "chatRooms: ${chatRooms.size}")
            recyclerView.adapter = ChatListAdapter(chatRooms)
            recyclerView.layoutManager = LinearLayoutManager(this@ChooseChatActivity)

        }

        val backButton = findViewById<ImageView>(R.id.choose_chat_back)
        backButton.setOnClickListener {
            finish()
        }
    }


    private suspend fun getChatList(): List<ChatItem> {
        val response = HTTPRequest.sendGetRequest(
            "${BuildConfig.SERVER_ADDRESS}/chats",
            this@ChooseChatActivity
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


class ChatListAdapter(
    private val chatRooms: List<ChatItem>,
): RecyclerView.Adapter<ChatListAdapter.ChatViewHolder>() {

    class ChatViewHolder(itemView: View): RecyclerView.ViewHolder(itemView)
    {
        private val targetUserTextView: TextView = itemView.findViewById(R.id.chatTargetUser)

        fun bind(chatRoom: ChatItem) {
            itemView.setOnClickListener {
                val intent = Intent(itemView.context, ChatActivity::class.java)
                Log.d("ChatListAdapter", "Chat ID: ${chatRoom.chatId}")
                intent.putExtra("chatId", chatRoom.chatId)
                itemView.context.startActivity(intent)
            }
            // Launch a coroutine on the IO dispatcher for network operations.
            CoroutineScope(Dispatchers.IO).launch {
                try {

                    val currentUserId = UserCredentialManager.getUserId(itemView.context)
                    var targetUserId = ""
                    if(chatRoom.member_1 == currentUserId){
                        targetUserId = chatRoom.member_2
                    }
                    else{
                        targetUserId = chatRoom.member_1
                    }

                    // Second network call: get user details.
                    val response = HTTPRequest.sendGetRequest(
                        "${BuildConfig.SERVER_ADDRESS}/user/$targetUserId",
                        itemView.context
                    )
                    val jsonObj2 = JSONObject(response)
                    val username = jsonObj2.getString("username")

                    // Switch back to the main thread to update the UI.
                    withContext(Dispatchers.Main) {

                        targetUserTextView.text = username
                    }
                } catch (e: Exception) {
                    // Handle errors appropriately (e.g., show a message or log the error)
                    e.printStackTrace()
                }
            }
        }



    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.chat_list_item, parent, false)
        return ChatViewHolder(view)
    }

    // 3. Bind each item
    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(chatRooms[position])

    }

    override fun getItemCount(): Int = chatRooms.size
}