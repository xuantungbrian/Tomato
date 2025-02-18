package com.example.tomato

import JwtManager
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.LinearLayout
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.lifecycleScope

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.tomato.databinding.ActivityMapsBinding
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.android.libraries.places.api.Places
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.security.MessageDigest
import java.util.UUID

data class SignInResponse(val token: String, val userID: String)

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    private val activityScope = CoroutineScope(Dispatchers.Main)

    companion object {
        private const val TAG = "MapsActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // Initialize Places SDK if not already done.
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), BuildConfig.WEB_CLIENT_ID);
        }

        // Initialize the Map Fragment
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)


        // Add event listener for sign in button
        findViewById<Button>(R.id.sign_in_button).setOnClickListener {
            val credentialManager = CredentialManager.create(this)
            val signInWithGoogleOption: GetSignInWithGoogleOption = GetSignInWithGoogleOption
                .Builder(BuildConfig.WEB_CLIENT_ID)
                .setNonce(generateHashedNonce())
            .build()

            val request: GetCredentialRequest = GetCredentialRequest.Builder()
                .addCredentialOption(signInWithGoogleOption)
                .build()

            activityScope.launch {
                try {
                    val result = credentialManager.getCredential(
                        request = request,
                        context = this@MapsActivity,
                    )
                    handleSignIn(result)
                } catch (e: GetCredentialException) {
                    Log.d(TAG, "Get credential exception", e)
                }
            }
        }

        // Event listener to upload post button
        findViewById<FloatingActionButton>(R.id.bottom_navbar_upload_button).setOnClickListener {
            val intent = Intent(this, UploadPostActivity::class.java)
            startActivity(intent)
        }

        // Event listener to home button
        findViewById<LinearLayout>(R.id.bottom_navbar_home_button).setOnClickListener {
            lifecycleScope.launch {
                val response = HTTPRequest.sendGetRequest("http://10.0.2.2:3000/test1", this@MapsActivity)
                Log.d(TAG, "onCreate: $response")
            }
        }

        // Event listener to profile button
        findViewById<LinearLayout>(R.id.bottom_navbar_profile_button).setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

    }


    private fun handleSignIn(result: GetCredentialResponse) {
        // Handle the successfully returned credential.
        val credential = result.credential

        when (credential) {
            is CustomCredential -> {
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        // Use googleIdTokenCredential and extract id to validate and
                        // authenticate on your server.
                        val googleIdTokenCredential = GoogleIdTokenCredential
                            .createFrom(credential.data).idToken
                        sendSignInRequest(googleIdTokenCredential)
                    } catch (e: GoogleIdTokenParsingException) {
                        Log.e(TAG, "Received an invalid google id token response", e)
                    }
                }

            }

            else -> {
                // Catch any unrecognized credential type here.
                Log.e(TAG, "Unexpected type of credential")
            }
        }
    }

    private fun sendSignInRequest(token: String) {
        // JSON string to send in the POST request
        val body = JSONObject().put("token", token).toString()

        lifecycleScope.launch {
            val response = HTTPRequest.sendPostRequest("${BuildConfig.SERVER_ADDRESS}/user/auth",
                body, this@MapsActivity)
            Log.d(TAG, "sendPostRequest: $response")

            if (response != null) {
                val signInResponse = Gson().fromJson(response, SignInResponse::class.java)
                JwtManager.saveToken(this@MapsActivity, signInResponse.token)
                Log.d(TAG, "sendSignInResponse: $signInResponse")
            }
        }
    }


    private fun generateHashedNonce(): String {
        val rawNonce = UUID.randomUUID().toString()
        val bytes = rawNonce.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker in Sydney and move the camera
        val sydney = LatLng(-34.0, 151.0)
        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }
}