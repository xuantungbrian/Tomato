package com.example.tomato

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
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.SupportMapFragment
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class DisplayMapOnOpen {

    @Test
    fun showMapOnOpen() {
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
}