package com.example.tomato.nonFunctional.stepLimitTest

import android.content.Context
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.example.tomato.R
import com.example.tomato.Utils
import com.example.tomato.activity.MapsActivity
import org.junit.Before
import org.junit.Test

class AtMostFourStepsToRecommendation {
    @Before
    fun clearSharedPrefs() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val prefs = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        prefs.edit().clear().commit()
    }


    @Test
    fun navigateToLocationRecommendation() {
        val scenario = ActivityScenario.launch(MapsActivity::class.java)
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        var stepCount = 0

        // Allow location permission
        val allowButton = device.wait(Until.findObject(By.text("Only this time")), 1000)
        allowButton?.click()
        stepCount++

        // Login with Google
        Utils.loginWithGoogle()
        stepCount++

        // Click on profile button
        onView(withId(R.id.profile_button_image)).perform(click())
        stepCount++

        // Reach recommendation feature
        onView(withId(R.id.recommendationText)).check(matches(isDisplayed()))
        assert(stepCount <= 4)
    }
}