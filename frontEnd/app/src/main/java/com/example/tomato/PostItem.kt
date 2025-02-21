package com.example.tomato

import android.net.Uri

data class PostItem(
    val imageData: List<Uri>,
    val location: String,
    val date: String,
    val note: String,
    val private: Boolean,
)

data class PostItemRaw(
    val images: List<PostImage>,
    val latitude: Double,
    val longitude: Double,
    val date: String,
    val note: String,
    val private: Boolean,
)

data class PostImage(
    val fileData: FileData,
    val fileType: String
)

data class FileData(
    val type: String,
    val data: List<Int>
)