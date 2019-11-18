package com.github.alexmelyon.master_charlist.world_pager

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.view.*
import android.widget.LinearLayout
import com.bluelinelabs.conductor.Controller
import com.bluelinelabs.conductor.Router
import com.bluelinelabs.conductor.RouterTransaction
import com.bluelinelabs.conductor.support.RouterPagerAdapter
import com.github.alexmelyon.master_charlist.MainActivity
import com.github.alexmelyon.master_charlist.R
import com.github.alexmelyon.master_charlist.list_effects.ListEffectsController
import com.github.alexmelyon.master_charlist.list_games.ListGamesController
import com.github.alexmelyon.master_charlist.list_games.WORLD_KEY
import com.github.alexmelyon.master_charlist.list_skills.ListSkillsController
import com.github.alexmelyon.master_charlist.list_things.ListThingsController
import com.github.alexmelyon.master_charlist.room.AppDatabase
import com.github.alexmelyon.master_charlist.room.World
import org.jetbrains.anko.design.tabLayout
import org.jetbrains.anko.linearLayout
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.support.v4.viewPager
import org.jetbrains.anko.wrapContent
import ru.napoleonit.talan.di.ControllerInjector
import javax.inject.Inject

class WorldPagerController(args: Bundle) : Controller(args) {

    @Inject
    lateinit var db: AppDatabase

    private lateinit var world: World

    constructor(worldId: Long) : this(Bundle().apply {
        putLong(WORLD_KEY, worldId)
    })

    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager
    private val pagerAdapter: PagerAdapter
    private lateinit var screenToController: List<Pair<String, Controller>>
    private lateinit var menu: Menu
    private lateinit var menuInflater: MenuInflater

    init {
        pagerAdapter = object : RouterPagerAdapter(this) {
            override fun configureRouter(router: Router, position: Int) {
                if (!router.hasRootController()) {
                    val page = screenToController[position].second
                    router.setRoot(RouterTransaction.with(page))
                }
            }

            override fun getCount(): Int {
                return screenToController.size
            }

            override fun getPageTitle(position: Int): CharSequence? {
                return screenToController[position].first
            }
        }
    }

    override fun onContextAvailable(context: Context) {
        super.onContextAvailable(context)
        ControllerInjector.inject(this)
        world = db.worldDao().getWorldById(args.getLong(WORLD_KEY))
        screenToController = listOf(
            "Games" to ListGamesController(world.id),
            "Skills" to ListSkillsController(world.id),
            "Things" to ListThingsController(world.id),
            "Effects" to ListEffectsController(world.id))
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View {
        (activity as MainActivity).supportActionBar!!.title = world.name
        setHasOptionsMenu(true)

        val view = container.context.linearLayout {
            orientation = LinearLayout.VERTICAL
            this@WorldPagerController.tabLayout = tabLayout {
                minimumHeight = R.attr.actionBarSize
                setTabTextColors(Color.BLACK, Color.BLACK)
            }.lparams(matchParent, wrapContent)
            this@WorldPagerController.viewPager = viewPager {
                adapter = pagerAdapter
            }.lparams(matchParent, matchParent)
        }
        tabLayout.setupWithViewPager(viewPager)
        return view
    }

    override fun onDestroy() {
        if(activity?.isChangingConfigurations() ?: false) {
            viewPager.adapter = null
        }
        tabLayout.setupWithViewPager(null)
        super.onDestroy()
    }

    val tabselectedListener = object : TabLayout.OnTabSelectedListener {
        override fun onTabSelected(tab: TabLayout.Tab) {
            screenToController[tab.position].second.onCreateOptionsMenu(menu, menuInflater)
        }

        override fun onTabReselected(tab: TabLayout.Tab) {
            screenToController[tab.position].second.onCreateOptionsMenu(menu, menuInflater)
        }

        override fun onTabUnselected(tab: TabLayout.Tab?) { }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        this.menu = menu
        this.menuInflater = inflater

        tabLayout.addOnTabSelectedListener(tabselectedListener)
        tabLayout.getTabAt(tabLayout.selectedTabPosition)?.select()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return screenToController[tabLayout.selectedTabPosition].second.onOptionsItemSelected(item)
    }
}