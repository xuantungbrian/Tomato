package com.example.tomato

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.google.android.gms.maps.SupportMapFragment
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class DisplayMapWithUserRejectLocationPermission {

    @Test
    fun userRejectLocationPermission() {
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
}