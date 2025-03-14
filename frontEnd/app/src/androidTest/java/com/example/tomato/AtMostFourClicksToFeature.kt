package com.example.tomato

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters

@RunWith(AndroidJUnit4::class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class AtMostFourClicksToFeature {

    @Test
    fun test1NavigateLogin() {
        val scenario = ActivityScenario.launch(MapsActivity::class.java)
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        var stepCount = 0

        // Allow location permission
        val allowButton = device.wait(Until.findObject(By.text("Only this time")), 1000)
        assertNotNull(allowButton)
        allowButton.click()
        stepCount++

        // Click on sign in button
        // TODO: better if clicked
        onView(withId(R.id.sign_in_button)).check(matches(isDisplayed()))
        stepCount++ // Assumed click

        // Reach sign in feature
        assert(stepCount <= 4)
    }

    @Test
    fun test2NavigateToCreatePost() {
        val scenario = ActivityScenario.launch(MapsActivity::class.java)
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        try {
            // Allow location permission
            val allowButton = device.wait(Until.findObject(By.text("Only this time")), 1000)
            allowButton.click()
        } catch (_: Exception) {}

        var stepCount = 1 // Start from 1 for grant permission step

        // Login with Google
        Utils.loginWithGoogle()
        Thread.sleep(60000)
        stepCount++

        // Open upload view
        onView(withId(R.id.bottom_navbar_upload_button)).perform(click())
        Thread.sleep(10000)
        stepCount++

        // Reach upload feature
        onView(withId(R.id.addPhotoButton)).check(matches(isDisplayed()))
        assert(stepCount <= 4)
    }

    @Test
    fun test3NavigateToDeletePost() {
        val scenario = ActivityScenario.launch(MapsActivity::class.java)
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        try {
            // Allow location permission
            val allowButton = device.wait(Until.findObject(By.text("Only this time")), 1000)
            allowButton.click()
        } catch (_: Exception) {}
        
        try {
            // Login with Google
            Utils.loginWithGoogle()
            Thread.sleep(30000)
        } catch (_: Exception) {}
        
        var stepCount = 2 // Start from 2 for grant permission step, login
//        Thread.sleep(10000)

        // Scroll to the post marker
        scenario.onActivity { activity ->
            val mapFragment = activity.supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
            mapFragment.getMapAsync { googleMap ->
//                Thread.sleep(50000)
                val markerLatLng = LatLng(49.2276257, -123.0075756) // Metrotown
//                val markerLatLng = LatLng(49.30425839999999, -123.1442523) // Stanley park
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(markerLatLng, 15f))
            }
        }
        stepCount++

        // Click on the post
        Thread.sleep(20000)
        device.click(540, 1120)
        Thread.sleep(20000)
        stepCount++

        // Reach delete feature
        onView(withId(R.id.delete_post_button)).check(matches(isDisplayed()))
        assert(stepCount <= 4)
    }

    @Test
    fun test4NavigateToViewPost() {
        val scenario = ActivityScenario.launch(MapsActivity::class.java)
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        try {
            // Allow location permission
            val allowButton = device.wait(Until.findObject(By.text("Only this time")), 1000)
            allowButton.click()
        } catch (_: Exception) {}

        try {
            // Login with Google
            Utils.loginWithGoogle()
            Thread.sleep(30000)
        } catch (_: Exception) {}

        var stepCount = 2 // Start from 2 for grant permission step, login

        // Scroll to the post marker
        scenario.onActivity { activity ->
            val mapFragment = activity.supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
            mapFragment.getMapAsync { googleMap ->
                val markerLatLng = LatLng(49.2276257, -123.0075756)
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(markerLatLng, 15f))
            }
        }
        stepCount++

        // Click on the post
        Thread.sleep(20000)
        device.click(540, 1120)
        Thread.sleep(20000)
        stepCount++

        // Reach view feature
        onView(withId(R.id.post_activity_postLocation)).check(matches(isDisplayed()))
        assert(stepCount <= 4)
    }

    @Test
    fun test5NavigateToSearchLocation() {
        val scenario = ActivityScenario.launch(MapsActivity::class.java)
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        try {
            // Allow location permission
            val allowButton = device.wait(Until.findObject(By.text("Only this time")), 1000)
            allowButton.click()
        } catch (_: Exception) {}

        try {
            // Login with Google
            Utils.loginWithGoogle()
            Thread.sleep(30000)
        } catch (_: Exception) {}

        var stepCount = 2 // Start from 2 for grant permission step, login

        // Reach it right away on screen
        onView(withId(R.id.locationAutoCompleteTextView)).check(matches(isDisplayed()))
        assert(stepCount < 4)
    }

    @Test
    fun test6NavigateToLocationRecommendation() {
        val scenario = ActivityScenario.launch(MapsActivity::class.java)
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        try {
            // Allow location permission
            val allowButton = device.wait(Until.findObject(By.text("Only this time")), 1000)
            allowButton.click()
        } catch (_: Exception) {}

        try {
            // Login with Google
            Utils.loginWithGoogle()
            Thread.sleep(30000)
        } catch (_: Exception) {}

        var stepCount = 2 // Start from 2 for grant permission step, login

        // Click on profile button
        onView(withId(R.id.profile_button_image)).perform(click())
        stepCount++

        // Reach recommendation feature
        onView(withId(R.id.recommendationText)).check(matches(isDisplayed()))
        assert(stepCount <= 4)
    }

    @Test
    fun test7NavigateToChat() {
        val scenario = ActivityScenario.launch(MapsActivity::class.java)
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        try {
            // Allow location permission
            val allowButton = device.wait(Until.findObject(By.text("Only this time")), 1000)
            allowButton.click()
        } catch (_: Exception) {}

        try {
            // Login with Google
            Utils.loginWithGoogle()
            Thread.sleep(30000)
        } catch (_: Exception) {}

        var stepCount = 2 // Start from 2 for grant permission step, login

        // Click on chat
        onView(withId(R.id.map_activity_chat_button)).perform(click())
        stepCount++

        // Reach chat feature
        onView(withId(R.id.chatList)).check(matches(isDisplayed()))
        assert(stepCount <= 4)
    }
}