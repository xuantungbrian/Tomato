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
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import org.junit.Test

class DisplayMapWithPost {

    @Test
    fun displayPostedPictureOnMap() {
        // Launch the activity
        val scenario = ActivityScenario.launch(MapsActivity::class.java)
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        // Grant location permission
        val allowButton = device.wait(Until.findObject(By.textContains("Only")), 30000)
        allowButton.click()
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