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
import org.junit.Test

class AtMostFourClicksToSearchLocation {

    @Test
    fun navigateToSearchLocation() {
        val scenario = ActivityScenario.launch(MapsActivity::class.java)
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        var stepCount = 0

        // Allow location permission
        val allowButton = device.wait(Until.findObject(By.text("Only this time")), 1000)
        allowButton.click()
        stepCount++

        // Login with Google
        Utils.loginWithGoogle()
        stepCount++

        // Reach it right away on screen
        onView(withId(R.id.locationAutoCompleteTextView)).check(matches(isDisplayed()))
        assert(stepCount < 4)
    }
}