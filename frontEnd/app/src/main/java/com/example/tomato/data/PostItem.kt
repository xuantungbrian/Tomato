package com.example.tomato.com.example.tomato.data

import android.net.Uri

data class PostItem(
    val postId: String,
    val imageData: List<Uri>,
    val location: String,
    val date: String,
    val note: String,
    val private: Boolean,
    val userId: String
)

data class PostItemRaw(
    val _id: String,
    val images: List<PostImage>,
    val latitude: Double,
    val longitude: Double,
    val date: String,
    val note: String,
    val private: Boolean,
    val userId: String
)

data class PostImage(
    val fileData: FileData,
    val fileType: String
)

data class FileData(
    val type: String,
    val data: List<Int>
)