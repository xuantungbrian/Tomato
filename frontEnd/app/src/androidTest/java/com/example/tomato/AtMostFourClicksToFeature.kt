package com.example.tomato

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AtMostFourClicksToFeature {
    private val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

    @get:Rule
    val activityScenarioRule = ActivityScenarioRule(MapsActivity::class.java)

    @Test
    fun navigateLogin() {
        var stepCount = 0

        // Allow location permission
        val allowButton = device.wait(Until.findObject(By.text("Only this time")), 1000)
        allowButton.click()
        stepCount++

        // Reach sign in feature
        assert(stepCount < 4)
        onView(withId(R.id.sign_in_button)).check(matches(isDisplayed()))
    }

    @Test
    fun navigateToCreatePost() {
        var stepCount = 0

        // Allow location permission
        val allowButton = device.wait(Until.findObject(By.text("Only this time")), 1000)
        allowButton.click()
        stepCount++

        // Open upload view
        onView(withId(R.id.bottom_navbar_upload_button)).perform(click())
        Thread.sleep(1000)

        // Reach upload feature
        assert(stepCount < 4)
        onView(withId(R.id.addPhotoButton)).check(matches(isDisplayed()))
    }

    @Test
    fun navigateToDeletePost() {
        var stepCount = 0

        // Allow location permission
        val allowButton = device.wait(Until.findObject(By.text("Only this time")), 1000)
        allowButton.click()
        stepCount++

        // Scroll to the post marker
        activityScenarioRule.scenario.onActivity { activity ->
            val mapFragment = activity.supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
            mapFragment.getMapAsync { googleMap ->
                val markerLatLng = LatLng(49.2276257, -123.0075756)
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(markerLatLng, 15f))
            }
        }
        stepCount++

        // Click on the post
        Thread.sleep(5000)
        device.click(540, 1120)
        stepCount++
        Thread.sleep(5000)

        // Reach delete feature
        assert(stepCount < 4)
        onView(withId(R.id.delete_post_button)).check(matches(isDisplayed()))
    }

    @Test
    fun navigateToViewPost() {
        var stepCount = 0

        // Allow location permission
        val allowButton = device.wait(Until.findObject(By.text("Only this time")), 1000)
        allowButton.click()
        stepCount++

        // Scroll to the post marker
        activityScenarioRule.scenario.onActivity { activity ->
            val mapFragment = activity.supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
            mapFragment.getMapAsync { googleMap ->
                val markerLatLng = LatLng(49.2276257, -123.0075756)
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(markerLatLng, 15f))
            }
        }
        stepCount++

        // Click on the post
        Thread.sleep(5000)
        device.click(540, 1120)
        stepCount++
        Thread.sleep(5000)

        // Reach view feature
        assert(stepCount < 4)
        onView(withId(R.id.post_activity_postLocation)).check(matches(isDisplayed()))
    }

    @Test
    fun navigateToSearchLocation() {
        var stepCount = 0

        // Reach it right away on screen
        assert(stepCount < 4)
        onView(withId(R.id.locationAutoCompleteTextView)).check(matches(isDisplayed()))
    }

    @Test
    fun navigateToLocationRecommendation() {
        var stepCount = 0

        Utils.loginWithGoogle()

        // Click on profile button
        onView(withId(R.id.profile_button_image)).perform(click())
        stepCount++

        // Find recomemndation part
        assert(stepCount < 4)
    }

    @Test
    fun navigateToChat() {
        var stepCount = 0

        Utils.loginWithGoogle()


        // Click on chat
        stepCount++

        // Find recomemndation part
        assert(stepCount < 4)
        // Find chat part
    }
}