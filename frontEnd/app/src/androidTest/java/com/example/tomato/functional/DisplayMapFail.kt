package com.example.tomato.functional

import android.content.Context
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until
import com.example.tomato.R
import com.example.tomato.activity.MapsActivity
import com.google.android.gms.maps.SupportMapFragment
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import java.util.Locale

class DisplayMapFail {
    private val scenario = ActivityScenario.launch(MapsActivity::class.java)
    @Before
    fun clearSharedPrefs() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val prefs = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        prefs.edit().clear().commit()
    }

    @Test
    fun userRejectLocationPermission() {
        // Rejects the location permission
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        Thread.sleep(500)
        val candidateButtons = device.wait(Until.findObjects(By.textContains("allow")), 2000)
        var declineButton: UiObject2? = null

        for (button in candidateButtons) {
            // Use a regex on the actual text of the button to match "Don't allow" (accounting for possible variations)
            if (button.text.lowercase(Locale.getDefault())?.matches(Regex(".*don['’]?t allow.*")) == true) {
                declineButton = button
                break
            }
        }
        assertNotNull(declineButton)
        if (declineButton != null) {
            declineButton.click()
        }

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
        Thread.sleep(1500)
        scenario.close()
    }
}