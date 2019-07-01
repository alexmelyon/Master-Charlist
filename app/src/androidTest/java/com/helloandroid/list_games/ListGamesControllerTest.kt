package com.helloandroid.list_games

import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import com.bluelinelabs.conductor.RouterTransaction
import com.helloandroid.MainActivity
import com.helloandroid.list_worlds.ParentTest
import com.helloandroid.room.AppDatabase
import com.helloandroid.room.World
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
    override fun setUp() {
        super.setUp()
        testAppComponent.inject(this)

        println("DB $db")
        // TODO Create world 0L
        activityRule.launchActivity(null)
        activityRule.activity?.runOnUiThread {
            val router = activityRule.activity.router
            val worldId = db.worldDao().insert(World("World", Date()))
            router.setRoot(RouterTransaction.with(ListGamesController(worldId)))
        }
    }

    @Test
    fun testCreateGame() {
//        Espresso.onView(ViewMatchers.withId(R.id.menu_add_game)).perform(ViewActions.click())
//        Espresso.onView(ViewMatchers.withId(R.id.alert_edit)).perform(ViewActions.typeText("Game"))
//        Espresso.onView(ViewMatchers.withId(android.R.id.button1)).perform(ViewActions.click())
//        Espresso.onView(ViewMatchers.withId(R.id.list_games)).check(RecyclerViewItemCountAssertion(1))
    }
}