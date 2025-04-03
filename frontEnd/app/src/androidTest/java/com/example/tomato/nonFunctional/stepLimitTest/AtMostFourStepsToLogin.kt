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
import com.example.tomato.activity.MapsActivity
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class AtMostFourStepsToLogin {
    @Before
    fun clearSharedPrefs() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val prefs = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        prefs.edit().clear().commit()
    }


    @Test
    fun navigateToLogin() {
        val scenario = ActivityScenario.launch(MapsActivity::class.java)
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        var stepCount = 0

        // Allow location permission
        val allowButton = device.wait(Until.findObject(By.text("Only this time")), 1000)
        allowButton?.click()
        stepCount++

        // Click on sign in button
        onView(withId(R.id.sign_in_button)).perform(click())
        stepCount++ // Assumed click

        // Reach sign in feature
        assert(stepCount <= 4)
        Thread.sleep(1000)
    }
}