package com.example.tomato.functional

import android.view.KeyEvent
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.example.tomato.R
import com.example.tomato.Utils
import com.example.tomato.activity.MapsActivity
import com.google.android.gms.maps.SupportMapFragment
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SearchLocations {
    private val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

    @get:Rule
    val activityScenarioRule = ActivityScenarioRule(MapsActivity::class.java)

    @Before
    fun setUp() {
        // Grant location permission
        try {
            val allowButton = device.wait(Until.findObject(By.text("Only this time")), 1000)
            allowButton.click()
        } catch (_: Exception) {}
    }

    @Test
    fun searchLocations() {
        // Click search bar and enter location
        onView(withId(R.id.locationAutoCompleteTextView)).perform(click())
        Utils.typeString("metrot")
        val locationButton = device.wait(Until.findObject(By.text("Metrotown, Burnaby, BC, Canada")), 5000)
        locationButton?.click()
        device.pressKeyCode(KeyEvent.KEYCODE_ENTER)
        Thread.sleep(3500)
        activityScenarioRule.scenario.onActivity { activity ->
            val mapFragment =
                activity.supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
            mapFragment.getMapAsync { googleMap ->
                val cameraPosition = googleMap.cameraPosition
                val target = cameraPosition.target
                assertEquals(49.2276257, target.latitude, 0.001)
                assertEquals(-123.0075756, target.longitude, 0.001)
            }
        }
        Thread.sleep(1000)
    }
}