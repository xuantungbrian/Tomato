package com.example.tomato

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.net.URI

class ChatActivity : AppCompatActivity() {

    private lateinit var messageInput: EditText
    private lateinit var sendButton: Button
    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var chatMessageAdapter: ChatMessageAdapter
    private lateinit var currentUserId: String
    private lateinit var chatId: String
    private lateinit var chatWebSocket: ChatWebSocket

    companion object {
        private const val TAG = "ChatActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        chatId = intent.getStringExtra("chatId") ?: ""
        currentUserId = UserCredentialManager.getUserId(this) ?: ""

        messageInput = findViewById(R.id.messageInput)
        sendButton = findViewById(R.id.sendButton)
        chatRecyclerView = findViewById(R.id.chatMessageRecyclerView)

        // Initialize RecyclerView and adapter
        chatMessageAdapter = ChatMessageAdapter(mutableListOf(), this)
        chatRecyclerView.layoutManager = LinearLayoutManager(this)
        chatRecyclerView.adapter = chatMessageAdapter


        // Fetch messages when the activity is created
        lifecycleScope.launch {
            getMessages()
        }

        val webSocketServerUri = "${BuildConfig.WSS_ADDRESS}?chatId=$chatId"
        chatWebSocket = ChatWebSocket(URI(webSocketServerUri)) { message ->
            val jsonObject = JSONObject(message)
            val newMessage = ChatMessage(
                jsonObject.getString("sender"),
                jsonObject.getString("message")
            )
            runOnUiThread {
                chatMessageAdapter.addMessages(listOf(newMessage))
                chatRecyclerView.scrollToPosition(chatMessageAdapter.itemCount - 1)
            }
        }

        chatWebSocket.connect()
        sendButton.setOnClickListener {
            sendMessage()
        }

        val backButton = findViewById<ImageView>(R.id.chat_back)
        backButton.setOnClickListener {
            finish()
        }

    }

    private fun sendMessage() {
        val messageText = messageInput.text.toString().trim()
        if (messageText.isNotEmpty()) {
            val body = JSONObject()
                .put("sender", currentUserId)
                .put("message", messageText)
                .toString()
            lifecycleScope.launch {
                if (chatWebSocket.isOpen) {
                    chatWebSocket.send(body)
                } else {
                    Log.e(TAG, "WebSocket is not connected. Message not sent.")
                }
            }
        }
        messageInput.text.clear()
    }

    private suspend fun getMessages() {
        val response = HTTPRequest.sendGetRequest(
            "${BuildConfig.SERVER_ADDRESS}/chats/$chatId",
            this@ChatActivity)

        if (response != null) {
            val messages = mutableListOf<ChatMessage>()
            val jsonArray = JSONArray(response)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                messages.add(
                    ChatMessage(
                        obj.getString("sender"),
                        obj.getString("message")
                    )
                )
            }
            runOnUiThread {
                chatMessageAdapter.updateMessages(messages) // Update RecyclerView
                // Scroll to bottom after initial load
                if (chatMessageAdapter.itemCount > 0) {
                    chatRecyclerView.scrollToPosition(chatMessageAdapter.itemCount - 1)
                }
            }
        }
    }


}
