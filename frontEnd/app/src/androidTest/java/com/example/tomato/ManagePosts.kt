package com.example.tomato

import android.view.KeyEvent
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters

@RunWith(AndroidJUnit4::class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class ManagePosts {
    private val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    private val longTimeout = 5000L
    private val mediumTimeout = 3000L
    private val shortTimeout = 500L
    private lateinit var scenario: ActivityScenario<MapsActivity>

    @Before
    fun setup() {
        // Launch the activity
        scenario = ActivityScenario.launch(MapsActivity::class.java)
        Thread.sleep(shortTimeout)

        // Rejects the location permission
        val allowButton = device.wait(Until.findObject(By.textContains("allow")), longTimeout)
        assertNotNull(allowButton)
        allowButton.click()
        Thread.sleep(shortTimeout)

        // Sign in with Google account
        Utils.signInWithGoogle()
        Thread.sleep(mediumTimeout)
    }

    @Test
    fun test1CreatePosts() {
        // Click on upload button
        onView(withId(R.id.bottom_navbar_upload_button)).perform(click())
        Thread.sleep(shortTimeout)

        // Click on upload button
        onView(withId(R.id.addPhotoButton)).perform(click())
        Thread.sleep(shortTimeout)

        // Select uploaded picture
        // TODO: This might not work on every phone
        device.swipe(200, 500, 200, 500, 100)
        val selectButton = device.wait(Until.findObject(By.textContains("SELECT")), longTimeout)
        assertNotNull(selectButton)
        selectButton.click()

        // Choose location
        onView(withId(R.id.addLocation)).perform(click())
        onView(withId(R.id.locationAutoCompleteTextView)).perform(click())
        Utils.typeString("metrot")
        val locationButton = device.wait(Until.findObject(By.text("Metrotown, Burnaby, BC, Canada")), longTimeout)
        assertNotNull(locationButton)
        locationButton.click()
        device.pressBack()
        onView(withId(R.id.submitLocationButton)).perform(click())

        // Choose visibility
        onView(withId(R.id.setVisibility)).perform(click())
        onView(withId(R.id.radioButton2)).perform(click())
        onView(withId(R.id.submitPostVisibility)).perform(click())

        // Choose date
        onView(withId(R.id.setDate)).perform(click())
        val okButton = device.wait(Until.findObject(By.text("OK")), longTimeout)
        assertNotNull(okButton)
        okButton.click()

        // Upload
        onView(withId(R.id.upload_post_button)).perform(click())
        Thread.sleep(mediumTimeout)

        // Check post layout is displayed
        onView(withId(R.id.post_activity_postLocation)).check(matches(isDisplayed()))

        // Return to map
        Thread.sleep(shortTimeout)
        onView(withId(R.id.post_back)).perform(click())
        Thread.sleep(shortTimeout)
        onView(withId(R.id.upload_post_back)).perform(click())
        Thread.sleep(shortTimeout)
        onView(withId(R.id.mapFragment)).check(matches(isDisplayed()))
    }

    @Test
    fun test2ViewPosts() {
        scenario.onActivity { activity ->
            val mapFragment = activity.supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
            mapFragment.getMapAsync { googleMap ->
                val markerLatLng = LatLng(49.2276257, -123.0075756)
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(markerLatLng, 15f))
            }
        }

        Thread.sleep(longTimeout)
        device.click(540, 1120)
        Thread.sleep(mediumTimeout)
        onView(withId(R.id.post_activity_postLocation)).check(matches(isDisplayed()))
    }

    @Test
    fun test3UpdatePosts() {
        scenario.onActivity { activity ->
            val mapFragment = activity.supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
            mapFragment.getMapAsync { googleMap ->
                val markerLatLng = LatLng(49.2276257, -123.0075756)
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(markerLatLng, 15f))
            }
        }

        Thread.sleep(longTimeout)
        device.click(540, 1120)
        Thread.sleep(mediumTimeout)
        onView(withId(R.id.post_activity_postLocation)).check(matches(isDisplayed()))
    }

    @Test
    fun test4DeletePosts() {
        scenario.onActivity { activity ->
            val mapFragment = activity.supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
            mapFragment.getMapAsync { googleMap ->
                val markerLatLng = LatLng(49.2276257, -123.0075756)
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(markerLatLng, 15f))
            }
        }

        Thread.sleep(longTimeout)
        device.click(540, 1120)
        Thread.sleep(mediumTimeout)
        onView(withId(R.id.post_activity_postLocation)).check(matches(isDisplayed()))

        // Delete post
        onView(withId(R.id.delete_post_button)).perform(click())
        val deleteButton = device.wait(Until.findObject(By.text("YES")), longTimeout)
        assertNotNull(deleteButton)
        deleteButton.click()
        Thread.sleep(longTimeout)

        // TODO: Check post exist on the frontend
        // TODO: When going back, it will ask for location permission once more
//        scenario.onActivity { activity ->
//            val mapFragment = activity.supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
//            mapFragment.getMapAsync { googleMap ->
//                val markerLatLng = LatLng(49.2276257, -123.0075756)
//                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(markerLatLng, 15f))
//            }
//        }
//
//        Thread.sleep(15000)
//        device.click(540, 1120)
//        Thread.sleep(mediumTimeout)
//        onView(withId(R.id.post_activity_postLocation)).check(matches(not(isDisplayed())))
    }
}