package com.helloandroid

import android.content.Context
import android.content.Intent
import android.support.test.InstrumentationRegistry
import android.support.test.espresso.Espresso
import android.support.test.espresso.action.ViewActions
import android.support.test.espresso.matcher.ViewMatchers
import android.support.test.runner.AndroidJUnit4
import com.helloandroid.utils.RecyclerViewItemCountAssertion
import org.jetbrains.anko.startActivity

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.junit.BeforeClass

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {

    companion object {

        lateinit var context: Context

        @BeforeClass
        @JvmStatic
        fun start() {
            context = InstrumentationRegistry.getTargetContext()
        }
    }

    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getTargetContext()
        assertEquals("com.helloandroid", appContext.packageName)
    }

    @Test
    fun createWorld() {
        context.startActivity(Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        })
        Espresso.onView(ViewMatchers.withId(R.id.menu_add_world)).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withId(R.id.alert_edit)).perform(ViewActions.typeText("World"))
        Espresso.onView(ViewMatchers.withId(android.R.id.button1)).perform(ViewActions.click())
        // TODO Clear Room storage
        Espresso.onView(ViewMatchers.withId(R.id.list_worlds)).check(RecyclerViewItemCountAssertion(1))
    }
}
