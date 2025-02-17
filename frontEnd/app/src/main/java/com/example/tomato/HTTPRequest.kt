package com.example.tomato

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException

object HTTPRequest {

//    fun sendGetRequest(url: String, context: Context){
//        val client = OkHttpClient()
//
//        // Build the POST request
//        val request = Request.Builder()
//            .url(url)
//            .build()
//
//        client.newCall(request).enqueue(object : Callback {
//            override fun onFailure(call: Call, e: IOException) {
//                // Handle request failure
//                e.printStackTrace()
//            }
//
//            override fun onResponse(call: Call, response: Response) {
//                // Handle successful response
//                if (response.isSuccessful) {
//                    val responseData = response.body?.string()
//                    val gson = Gson()
//                    val signInResponse = gson.fromJson(responseData, SignInResponse::class.java)
//                    val token = signInResponse.token
//                    JwtManager.saveToken(this@MapsActivity, token)
//
//                    Log.d(context.toString(),"Response: $responseData")
//                } else {
//                    Log.d(context.toString(), "Request failed with code: ${response.code}")
//                }
//            }
//
//    }

    /**
     * Sends a POST request to the specified URL with the provided request body.
     * @param stringifiedBody: the body in stringified json format, e.g. """{"token": "$token"}""".
     */
    suspend fun sendPostRequest(url: String, stringifiedBody: String): String? {


        val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
        val body = stringifiedBody.toRequestBody(mediaType)

        // Use IO dispatcher to perform network operations (connect to backend)
        return withContext(Dispatchers.IO){
            val client = OkHttpClient()

            val request = Request.Builder()
                .url(url)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .build()

            try {
                // Execute the request synchronously on the IO dispatcher
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        throw IOException("Unexpected code $response")
                    }
                    // Return the response data as a String
                    response.body?.string()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }


}