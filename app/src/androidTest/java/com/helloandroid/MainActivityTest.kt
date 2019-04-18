package com.helloandroid

import android.app.Activity
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import com.helloandroid.room.AppDatabaseModule
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.DispatchingAndroidInjector_Factory
import org.junit.Before
import org.junit.BeforeClass
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
            .appDatabaseModule(AppDatabaseModule(app))
            .build()
        app.appComponent = testAppComponent
        testAppComponent.inject(this)
    }

    @Test
    fun test() {

    }
}
fun createFakeMainActivityInjector(block : MainActivity.() -> Unit): DispatchingAndroidInjector<Activity> {
    val injector = AndroidInjector<Activity> { instance ->
        if (instance is MainActivity) {
            instance.block()
        }
    }
    val factory = AndroidInjector.Factory<Activity> { injector }
    val map = mapOf(Pair<Class <out Activity>, Provider<AndroidInjector.Factory<out Activity>>>(MainActivity::class.java, Provider { factory }))
    return DispatchingAndroidInjector_Factory.newDispatchingAndroidInjector(map)
}