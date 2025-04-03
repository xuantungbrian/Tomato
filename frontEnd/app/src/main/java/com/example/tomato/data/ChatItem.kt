package com.example.tomato.com.example.tomato.data

import com.google.gson.annotations.SerializedName

data class ChatItem(
    @SerializedName("_id") val chatId: String,
    val member_1: String,
    val member_2: String
)

