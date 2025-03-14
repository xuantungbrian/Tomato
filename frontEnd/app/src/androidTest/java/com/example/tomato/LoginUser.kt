package com.example.tomato

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class LoginUser {
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

    @After
    fun tearDown() {
        // Logout
        try {
            onView(withId(R.id.sign_out_button)).perform(click())
        } catch (_: Exception) {}
    }

    @Test
    fun userLoginFailed() {
        // Click login button and click somewhere else to make login failed
        onView(withId(R.id.sign_in_button)).perform(click())
        device.click(100, 100)

        // Check login failure:
        // - An alert pops up
        // - Login button still display
        // - Cannot use profile feature
        onView(withText(containsString("Login failed:"))).check(matches(isDisplayed()))
        onView(withText("OKAY")).perform(click())
        onView(withId(R.id.mapFragment)).check(matches(isDisplayed()))
        onView(withId(R.id.sign_in_button)).check(matches((isDisplayed())))
        onView(withId(R.id.profile_button_image)).perform(click())
        onView(withText("Login is required to view profile page")).check(matches(isDisplayed()))
        onView(withText("OKAY")).perform(click())

        // Login with correct account
        Utils.loginWithGoogle()

        // Check login success by opening profile page and no login button available
        onView(withId(R.id.mapFragment)).check(matches(isDisplayed()))
        onView(withId(R.id.sign_in_button)).check(matches(not(isDisplayed())))
        onView(withId(R.id.map_activity_profile_button)).perform(click())
        onView(withId(R.id.coordinatorLayout2)).check(matches(isDisplayed()))
    }
}