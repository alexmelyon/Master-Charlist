package com.helloandroid.list_worlds

import android.content.Intent
import android.support.test.espresso.Espresso
import android.support.test.espresso.action.ViewActions
import android.support.test.espresso.matcher.ViewMatchers
import android.support.test.runner.AndroidJUnit4
import com.helloandroid.MainActivity
import com.helloandroid.ParentTest
import com.helloandroid.R
import com.helloandroid.utils.RecyclerViewItemCountAssertion
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ListWorldsControllerTest : ParentTest() {

    @Test
    fun testCreateWorld() {
        app.startActivity(Intent(app, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        })
        Espresso.onView(ViewMatchers.withId(R.id.menu_add_world)).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withId(R.id.alert_edit)).perform(ViewActions.typeText("World"))
        Espresso.onView(ViewMatchers.withId(android.R.id.button1)).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withId(R.id.list_worlds)).check(RecyclerViewItemCountAssertion(1))
    }

    // TODO Archive, Rename
}
