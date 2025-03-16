package com.example.tomato

import android.content.Context
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.runBlocking
import org.json.JSONObject

class ChatMessageAdapter(private var messages: MutableList<ChatMessage>, context: Context) : RecyclerView.Adapter<ChatMessageAdapter.ChatViewHolder>() {
    private lateinit var currentUserId: String
    private val context = context
    private var otherUsername = ""

    // Create ViewHolder
    class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val senderTextView: TextView = itemView.findViewById(R.id.sender)
        val messageTextView: TextView = itemView.findViewById(R.id.message)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.chat_message_item, parent, false)
        currentUserId = UserCredentialManager.getUserId(parent.context) ?: ""
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chatMessage = messages[position]
        var senderName = ""


        if(chatMessage.sender == currentUserId){
            senderName = "You"
        }
        else if(otherUsername != ""){
            senderName = otherUsername
        }
        else{
            senderName = resolveName(chatMessage.sender)
            otherUsername = senderName
        }

        holder.senderTextView.text = senderName
        holder.messageTextView.text = chatMessage.message

        if (senderName == "You") {
            // Align current user's message to the right
            holder.messageTextView.updateLayoutParams<LinearLayout.LayoutParams> {
                gravity = Gravity.END
            }
            holder.senderTextView.updateLayoutParams<LinearLayout.LayoutParams> {
                gravity = Gravity.END
            }
        } else {
            // Align other user's message to the left
            holder.messageTextView.updateLayoutParams<LinearLayout.LayoutParams> {
                gravity = Gravity.START
            }
            holder.senderTextView.updateLayoutParams<LinearLayout.LayoutParams> {
                gravity = Gravity.START
            }
        }
    }

    override fun getItemCount(): Int = messages.size

    // Update messages in the adapter
    fun updateMessages(newMessages: List<ChatMessage>) {
        messages.clear()
        messages.addAll(newMessages)
        notifyDataSetChanged()
    }

    fun addMessages(newMessages: List<ChatMessage>) {
        messages.addAll(newMessages)
        notifyDataSetChanged()
    }

    private fun resolveName(id: String): String {
        return runBlocking {
            val response = HTTPRequest.sendGetRequest("${BuildConfig.SERVER_ADDRESS}/user/$id", context)
            val jsonObject = response?.let { JSONObject(it) }
            jsonObject?.getString("username") ?: ""
        }
    }
}
