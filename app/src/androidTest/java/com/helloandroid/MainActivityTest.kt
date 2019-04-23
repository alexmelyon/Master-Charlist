package com.helloandroid

import android.app.Activity
import android.arch.persistence.room.Room
import android.content.Context
import android.support.test.InstrumentationRegistry
import android.support.test.espresso.Espresso
import android.support.test.espresso.action.ViewActions
import android.support.test.espresso.matcher.ViewMatchers
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import android.util.Log
import com.helloandroid.dagger.AppComponent
import com.helloandroid.dagger.MainActivityModule
import com.helloandroid.list_worlds.DaggerTestAppComponent
import com.helloandroid.list_worlds.InmemoryDatabaseModule
import com.helloandroid.room.AppDatabase
import dagger.Component
import dagger.Module
import dagger.Provides
import dagger.android.AndroidInjectionModule
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.DispatchingAndroidInjector_Factory
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Provider

@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @Before
    fun setUp() {

        val context = InstrumentationRegistry.getTargetContext()
        val app = context.applicationContext as App

        val testAppComponent = DaggerTestAppComponent.builder()
            .inmemoryDatabaseModule(InmemoryDatabaseModule(app))
            .build()
        app.appComponent = testAppComponent
//        testAppComponent.inject(this)
    }

    @get:Rule
    val activityTestRule = object : ActivityTestRule<MainActivity>(MainActivity::class.java, true, true) {
        override fun beforeActivityLaunched() {
            super.beforeActivityLaunched()
            val myApp = InstrumentationRegistry.getTargetContext().applicationContext as App
//            myApp.dispatchingActivityInjector = createFakeMainActivityInjector {
//                userAction = mockUserAction
//            }
            myApp.dispatchingAndroidInjector = createFakeMainActivityInjector {
                // Replace anything
            }
        }
    }

    @Test
    fun clickOnFabCallToCreateTopic() {
        Espresso.onView(ViewMatchers.withId(R.id.menu_add_world)).perform(ViewActions.click())

        Espresso.onView(ViewMatchers.withId(R.id.alert_edit)).perform(ViewActions.typeText("World"))
//        verify(mockUserAction).createTopic(view = activityTestRule.activity)

    }
}

fun createFakeMainActivityInjector(block: MainActivity.() -> Unit): DispatchingAndroidInjector<Activity> {
    val injector = AndroidInjector<Activity> { instance ->
        if (instance is MainActivity) {
            instance.block()
        }
    }
    val factory = AndroidInjector.Factory<Activity> { injector }
    val map = mapOf(Pair<Class<out Activity>, Provider<AndroidInjector.Factory<out Activity>>>(MainActivity::class.java, Provider { factory }))
    return DispatchingAndroidInjector_Factory.newDispatchingAndroidInjector(map)
}