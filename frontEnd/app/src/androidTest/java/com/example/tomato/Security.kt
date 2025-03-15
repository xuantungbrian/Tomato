package com.example.tomato

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import org.hamcrest.Matchers.not
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test

class Security {
    private val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

    @get:Rule
    val activityScenarioRule = ActivityScenarioRule(MapsActivity::class.java)

    @Test
    fun preventFeatureAccessWithoutLogin() {
        // Allow location permission
        val allowButton = device.wait(Until.findObject(By.text("Only this time")), 1000)
        assertNotNull(allowButton)
        allowButton.click()

        // Try click on upload button
        onView(withId(R.id.bottom_navbar_upload_button)).perform(click())
        onView(withText("Login is required to upload post")).check(matches(isDisplayed()))
        var okButton = device.wait(Until.findObject(By.text("OKAY")), 1000)
        assertNotNull(okButton)
        okButton.click()
        Thread.sleep(5000)

        // Try click on profile button
        onView(withId(R.id.profile_button_image)).perform(click())
        onView(withText("Login is required to view profile page")).check(matches(isDisplayed()))
        okButton = device.wait(Until.findObject(By.text("OKAY")), 1000)
        assertNotNull(okButton)
        okButton.click()

        // Try click on chat button
        onView(withId(R.id.map_activity_chat_button)).perform(click())
        onView(withText("Login is required to chat with others")).check(matches(isDisplayed()))
        okButton = device.wait(Until.findObject(By.text("OKAY")), 1000)
        assertNotNull(okButton)
        okButton.click()

        // Try to view the posts but cannot delete
        // Scroll to the post marker
        activityScenarioRule.scenario.onActivity { activity ->
            val mapFragment = activity.supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
            mapFragment.getMapAsync { googleMap ->
                val markerLatLng = LatLng(49.2276257, -123.0075756)
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(markerLatLng, 15f))
            }
        }

        // Click on the post
        Thread.sleep(5000)
        device.click(540, 1120)
        Thread.sleep(5000)

        // Reach delete feature
        onView(withId(R.id.delete_post_button)).check(matches(not(isDisplayed())))
    }
}