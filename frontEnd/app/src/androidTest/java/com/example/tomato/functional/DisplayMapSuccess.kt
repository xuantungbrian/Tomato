package com.example.tomato.functional

import android.content.Context
import android.location.Location
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.example.tomato.R
import com.example.tomato.activity.MapsActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class DisplayMapSuccess {
    private val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    @Before
    fun clearSharedPrefs() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val prefs = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        prefs.edit().clear().commit()
    }

    fun setUp() {
        // Grant location permission
        try {
            val allowButton = device.wait(Until.findObject(By.text("Only this time")), 1000)
            allowButton.click()
        } catch (_: Exception) {}
    }

    @Test
    fun displayPostedPictureOnMap() {
        // Launch the activity
        val scenario = ActivityScenario.launch(MapsActivity::class.java)

        setUp()

        // Verify the camera position is set to the current location
        scenario.onActivity { activity ->
            val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(activity)
            val mapFragment = activity.supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
            mapFragment.getMapAsync { googleMap ->
                val cameraPosition = googleMap.cameraPosition
                val target = cameraPosition.target
                fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                    assertNotNull(location)
                    location?.let {
                        assertEquals(location.latitude, target.latitude, 0.01)
                        assertEquals(location.longitude, target.longitude, 0.01)
                    }
                }
            }
        }
        Thread.sleep(5000)

        // Scroll camera to a pre-uploaded post position
        scenario.onActivity { activity ->
            val mapFragment = activity.supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
            mapFragment.getMapAsync { googleMap ->
                val markerLatLng = LatLng(49.2276257, -123.0075756) // Metrotown
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(markerLatLng, 15f))
            }
        }

        Thread.sleep(4000)
        device.click(540, 1120)
        Thread.sleep(1000)
        onView(withId(R.id.post_activity_postLocation)).check(matches(isDisplayed()))
    }
}