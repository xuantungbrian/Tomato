package com.example.tomato

import android.net.Uri

/**
 * Post representation with images translated to list of URIs
 */
data class PostItem(
    val postId: String,
    val imageData: List<Uri>,
    val location: String,
    val date: String,
    val note: String,
    val private: Boolean,
    val userId: String
)

/**
 * Post representation with images still in base64 strings
 */
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

/**
 * Representation of a post image
 */
data class PostImage(
    val fileData: FileData,
    val fileType: String
)

/**
 * Representation of image file
 */
data class FileData(
    val type: String,
    val data: List<Int>
)