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

class AtMostFourClicksToDeletePost {

    @Test
    fun navigateToDeletePost() {
        val scenario = ActivityScenario.launch(MapsActivity::class.java)
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        var stepCount = 0

        // Allow location permission
        val allowButton = device.wait(Until.findObject(By.text("Only this time")), 1000)
        allowButton.click()
        stepCount++

        // Login with Google
        Utils.loginWithGoogle()
        Thread.sleep(30000)
        stepCount++

        // Scroll to the post marker
        scenario.onActivity { activity ->
            val mapFragment = activity.supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
            mapFragment.getMapAsync { googleMap ->
                val markerLatLng = LatLng(49.2276257, -123.0075756) // Metrotown
//                val markerLatLng = LatLng(49.30425839999999, -123.1442523) // Stanley park
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(markerLatLng, 15f))
            }
        }
        stepCount++

        // Click on the post
        Thread.sleep(20000)
        device.click(540, 1120)
        Thread.sleep(30000)
        stepCount++

        // Reach delete feature
        onView(withId(R.id.delete_post_button)).check(matches(isDisplayed()))
        assert(stepCount <= 4)
    }
}