package com.example.tomato

import android.view.KeyEvent
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import org.junit.Assert.assertNotNull

object Utils {
    private const val googleEmail = "tungluu12302002@gmail.com"
    private const val googlePassword = "Ltv52xuantung@"
    private val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

    // Helper function to map characters to their respective keycode
    private fun getKeyCodeForChar(char: Char): Int {
        return when (char) {
            'a' -> KeyEvent.KEYCODE_A
            'b' -> KeyEvent.KEYCODE_B
            'c' -> KeyEvent.KEYCODE_C
            'd' -> KeyEvent.KEYCODE_D
            'e' -> KeyEvent.KEYCODE_E
            'f' -> KeyEvent.KEYCODE_F
            'g' -> KeyEvent.KEYCODE_G
            'h' -> KeyEvent.KEYCODE_H
            'i' -> KeyEvent.KEYCODE_I
            'j' -> KeyEvent.KEYCODE_J
            'k' -> KeyEvent.KEYCODE_K
            'l' -> KeyEvent.KEYCODE_L
            'm' -> KeyEvent.KEYCODE_M
            'n' -> KeyEvent.KEYCODE_N
            'o' -> KeyEvent.KEYCODE_O
            'p' -> KeyEvent.KEYCODE_P
            'q' -> KeyEvent.KEYCODE_Q
            'r' -> KeyEvent.KEYCODE_R
            's' -> KeyEvent.KEYCODE_S
            't' -> KeyEvent.KEYCODE_T
            'u' -> KeyEvent.KEYCODE_U
            'v' -> KeyEvent.KEYCODE_V
            'w' -> KeyEvent.KEYCODE_W
            'x' -> KeyEvent.KEYCODE_X
            'y' -> KeyEvent.KEYCODE_Y
            'z' -> KeyEvent.KEYCODE_Z
            '1' -> KeyEvent.KEYCODE_1
            '2' -> KeyEvent.KEYCODE_2
            '3' -> KeyEvent.KEYCODE_3
            '4' -> KeyEvent.KEYCODE_4
            '5' -> KeyEvent.KEYCODE_5
            '6' -> KeyEvent.KEYCODE_6
            '7' -> KeyEvent.KEYCODE_7
            '8' -> KeyEvent.KEYCODE_8
            '9' -> KeyEvent.KEYCODE_9
            '0' -> KeyEvent.KEYCODE_0
            '@' -> KeyEvent.KEYCODE_AT
            '.' -> KeyEvent.KEYCODE_PERIOD
            else -> throw IllegalArgumentException("Unsupported character: $char")
        }
    }

    // Used for typing the string into a field.
    // Note: Might be flaky since the code does not result in the same pressing order
    fun typeString(text: String) {
        for (char in text.toCharArray()) {
            if (char.isUpperCase()) {
                device.pressKeyCode(KeyEvent.KEYCODE_SHIFT_LEFT)
                Thread.sleep(1000)
                device.pressKeyCode(getKeyCodeForChar(char.lowercaseChar()))
                Thread.sleep(1000)
                device.pressKeyCode(KeyEvent.KEYCODE_SHIFT_LEFT)
            } else {
                device.pressKeyCode(getKeyCodeForChar(char))
            }
            Thread.sleep(1000)
        }
    }

    fun loginWithGoogle() {
        onView(withId(R.id.sign_in_button)).perform(click())
        val myAccountButton = device.wait(Until.findObject(By.text(googleEmail)), 10000)
        if (myAccountButton != null) {
            myAccountButton.click()
        } else {
            val emailField = device.wait(Until.findObject(By.clazz("android.widget.EditText")), 5000)
            assertNotNull(emailField)
            emailField.text = googleEmail

            var nextButton = device.wait(Until.findObject(By.text("NEXT")), 5000)
            assertNotNull(nextButton)
            nextButton.click()

            val passwordField = device.wait(Until.findObject(By.clazz("android.widget.EditText")), 5000)
            assertNotNull(passwordField)
            passwordField.click()
            typeString(googlePassword)

            nextButton = device.wait(Until.findObject(By.text("NEXT")), 5000)
            assertNotNull(nextButton)
            nextButton.click()

            val agreeButton = device.wait(Until.findObject(By.text("I agree")), 5000)
            assertNotNull(agreeButton)
            agreeButton.click()

            val acceptButton = device.wait(Until.findObject(By.text("ACCEPT")), 5000)
            assertNotNull(acceptButton)
            acceptButton.click()
        }
        Thread.sleep(10000)
    }
}