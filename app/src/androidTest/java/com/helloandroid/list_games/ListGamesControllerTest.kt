package com.helloandroid.list_games

import android.support.test.espresso.Espresso
import android.support.test.espresso.action.ViewActions
import android.support.test.espresso.matcher.ViewMatchers
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import android.support.v7.view.menu.MenuBuilder
import android.view.MenuInflater
import com.bluelinelabs.conductor.RouterTransaction
import com.helloandroid.MainActivity
import com.helloandroid.ParentTest
import com.helloandroid.R
import com.helloandroid.room.AppDatabase
import com.helloandroid.room.World
import com.helloandroid.utils.RecyclerViewItemCountAssertion
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
class ListGamesControllerTest : ParentTest() {

    @get:Rule
    var activityRule = ActivityTestRule<MainActivity>(MainActivity::class.java, false, false)

    @Inject
    lateinit var db: AppDatabase

    @Before
    fun setUp() {
        testAppComponent.inject(this)
        activityRule.launchActivity(null)
        activityRule.activity.runOnUiThread {
            val router = activityRule.activity.router
            val worldId = db.worldDao().insert(World("World", Date()))
//            val controller = WorldPagerController(worldId)
            val controller = ListGamesController(worldId)
            router.setRoot(RouterTransaction.with(controller))
            val context = controller.activity
            val menu = MenuBuilder(context)
            val inflater = MenuInflater(context)
            controller.onCreateOptionsMenu(menu, inflater)
        }
    }

    @Test
    fun testCreateGame() {
        Espresso.onView(ViewMatchers.withId(R.id.menu_add_game)).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withId(R.id.alert_edit)).perform(ViewActions.typeText("Game"))
        Espresso.onView(ViewMatchers.withId(android.R.id.button1)).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withId(R.id.list_games)).check(RecyclerViewItemCountAssertion(1))
    }
}