package com.helloandroid.list_worlds

import android.arch.persistence.room.Room
import android.content.Context
import android.content.Intent
import android.support.test.InstrumentationRegistry
import android.support.test.espresso.Espresso
import android.support.test.espresso.action.ViewActions
import android.support.test.espresso.matcher.ViewMatchers
import android.support.test.runner.AndroidJUnit4
import android.util.Log
import com.helloandroid.*
import com.helloandroid.dagger.AppComponent
import com.helloandroid.dagger.MainActivityModule
import com.helloandroid.room.AppDatabase
import com.helloandroid.utils.RecyclerViewItemCountAssertion
import dagger.Component
import dagger.Module
import dagger.Provides
import dagger.android.AndroidInjectionModule
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ListWorldsViewTest {

    lateinit var context: Context

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getTargetContext()
        val app = context.applicationContext as App
        val testAppComponent = DaggerTestAppComponent.builder()
            .inmemoryDatabaseModule(InmemoryDatabaseModule(app))
            .build()
        app.appComponent = testAppComponent
//        testAppComponent.inject(this)
        testAppComponent.inject(app)
    }

    @Test
    fun testCreateWorld() {
        context.startActivity(Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        })
        Espresso.onView(ViewMatchers.withId(R.id.menu_add_world)).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withId(R.id.alert_edit)).perform(ViewActions.typeText("World"))
        Espresso.onView(ViewMatchers.withId(android.R.id.button1)).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withId(R.id.list_worlds)).check(RecyclerViewItemCountAssertion(1))
    }
}

@Component(
    modules = [
        MainActivityModule::class,
        AndroidInjectionModule::class,
        InmemoryDatabaseModule::class]
)
interface TestAppComponent : AppComponent {
    fun inject(activity: ListWorldsViewTest)
}

@Module
class InmemoryDatabaseModule(val context: Context) {

    @Provides
    fun provideDb(): AppDatabase {
        val dbName = BuildConfig.ROOM_DB_NAME
        Log.i("ROOM", "Room using '$dbName'")
        return Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }
}