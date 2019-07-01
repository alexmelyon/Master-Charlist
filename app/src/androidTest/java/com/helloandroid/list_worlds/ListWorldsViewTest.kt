package com.helloandroid.list_worlds

import android.content.Context
import android.content.Intent
import android.support.test.InstrumentationRegistry
import android.support.test.espresso.Espresso
import android.support.test.espresso.action.ViewActions
import android.support.test.espresso.matcher.ViewMatchers
import android.support.test.runner.AndroidJUnit4
import com.helloandroid.App
import com.helloandroid.MainActivity
import com.helloandroid.R
import com.helloandroid.dagger.DaggerTestAppComponent
import com.helloandroid.dagger.InmemoryDatabaseModule
import com.helloandroid.dagger.TestAppComponent
import com.helloandroid.utils.RecyclerViewItemCountAssertion
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

open class ParentTest {

    val app: App
    val testAppComponent: TestAppComponent

    init {
        app = InstrumentationRegistry.getTargetContext().applicationContext as App
        val dbModule = InmemoryDatabaseModule(app)
        testAppComponent = DaggerTestAppComponent.builder()
            .inmemoryDatabaseModule(dbModule)
            .build()
        app.appComponent = testAppComponent
        testAppComponent.inject(app)
    }
}

@RunWith(AndroidJUnit4::class)
class ListWorldsViewTest : ParentTest() {

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
