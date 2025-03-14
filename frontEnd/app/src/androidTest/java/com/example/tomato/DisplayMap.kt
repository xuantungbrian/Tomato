package com.example.tomato

import android.location.Location
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.junit.FixMethodOrder
import org.junit.runners.MethodSorters

@RunWith(AndroidJUnit4::class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class DisplayMap {

    @Test
    fun test2ShowMapOnOpen() {
        // Launch the activity
        val scenario = ActivityScenario.launch(MapsActivity::class.java)

        // Grant location permission
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        val allowButton = device.wait(Until.findObject(By.text("Only this time")), 5000)
        assertNotNull(allowButton)
        allowButton.click()

        // Verify the map is displayed
        onView(withId(R.id.mapFragment))
            .check(matches(isDisplayed()))

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
        Thread.sleep(10000)
        scenario.close()
    }

    @Test
    fun test1UserRejectLocationPermission() {
        // Launch the activity
        val scenario = ActivityScenario.launch(MapsActivity::class.java)

        // Rejects the location permission
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        val allowButton = device.wait(Until.findObject(By.textContains("allow")), 30000)
        assertNotNull(allowButton)
        allowButton.click()

        // Verify the map is displayed
        onView(withId(R.id.mapFragment))
            .check(matches(isDisplayed()))

        // Verify the camera position is set to (0, 0)
        scenario.onActivity { activity ->
            val mapFragment = activity.supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
            mapFragment.getMapAsync { googleMap ->
                val cameraPosition = googleMap.cameraPosition
                val target = cameraPosition.target
                assertEquals(0.0, target.latitude, 0.01)
                assertEquals(0.0, target.longitude, 0.01)
            }
        }
        Thread.sleep(10000)
        scenario.close()
    }

    @Test
    fun test3DisplayPostedPictureOnMap() {
        // Launch the activity
        val scenario = ActivityScenario.launch(MapsActivity::class.java)
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        try {
            val allowButton = device.wait(Until.findObject(By.textContains("Only")), 30000)
            allowButton.click()
        } catch (_: Exception) {}

        Thread.sleep(10000)

        // Scroll camera to a pre-uploaded post position
        scenario.onActivity { activity ->
            val mapFragment = activity.supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
            mapFragment.getMapAsync { googleMap ->
                val markerLatLng = LatLng(49.30425839999999, -123.1442523)
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(markerLatLng, 15f))
            }
        }

        Thread.sleep(20000)
        device.click(540, 1120)
        Thread.sleep(1000)
        onView(withId(R.id.post_activity_postLocation)).check(matches(isDisplayed()))
    }
}