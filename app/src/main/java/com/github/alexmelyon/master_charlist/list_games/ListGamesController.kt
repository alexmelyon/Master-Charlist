package com.github.alexmelyon.master_charlist.list_games

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import com.bluelinelabs.conductor.Controller
import com.bluelinelabs.conductor.RouterTransaction
import com.crashlytics.android.Crashlytics
import com.github.alexmelyon.master_charlist.App
import com.github.alexmelyon.master_charlist.R
import com.github.alexmelyon.master_charlist.game_pager.GamePagerController
import com.github.alexmelyon.master_charlist.room.AppDatabase
import com.github.alexmelyon.master_charlist.room.Game
import com.github.alexmelyon.master_charlist.room.World
import ru.napoleonit.talan.di.ControllerInjector
import java.util.*
import javax.inject.Inject

val WORLD_KEY = "WORLD_KEY"

class ListGamesController(args: Bundle) : Controller(args), ListGamesContract.Controller {

    lateinit var world: World

    constructor(world: World) : this(Bundle().apply {
        putParcelable(WORLD_KEY, world)
    })

    @Inject
    lateinit var view: ListGamesContract.View
    @Inject
    lateinit var db: AppDatabase

    private val gamesSet = TreeSet<Game>(kotlin.Comparator { o1, o2 ->
        val res = o2.time.compareTo(o1.time)
        if(res == 0) {
            return@Comparator o1.name.compareTo(o2.name)
        }
        return@Comparator res
    })

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View {//2 create
        Crashlytics.log(Log.INFO, javaClass.simpleName, "onCreateView")
        return view.createView(container)
    }

    override fun onContextAvailable(context: Context) {//1 create
        super.onContextAvailable(context)
        ControllerInjector.inject(this)
        world = args.getParcelable<World>(WORLD_KEY)!!
        updateGames()
    }

    fun updateGames() {
        App.instance.gameStorage.getAll(world) { games ->
            gamesSet.addAll(games)
            this.view.setData(gamesSet.toMutableList())
        }
    }

    override fun onAttach(view: View) {//3 create
        super.onAttach(view)
//        App.instance.gameStorage.getAll(world) { games ->
//            this.view.setData(games.toMutableList())
//        }
    }

    override fun onSaveInstanceState(outState: Bundle) {//3 to back
        super.onSaveInstanceState(outState)
        outState.putParcelable(WORLD_KEY, world)
    }

    override fun onSaveViewState(view: View, outState: Bundle) {//2 to back
        super.onSaveViewState(view, outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {//1 to forw
        super.onRestoreInstanceState(savedInstanceState)
        world = savedInstanceState.getParcelable<World>(WORLD_KEY)!!
    }

    override fun onRestoreViewState(view: View, savedViewState: Bundle) {
        super.onRestoreViewState(view, savedViewState)
    }

    override fun onActivityPaused(activity: Activity) {//1 to back
        super.onActivityPaused(activity)
    }

    override fun onActivityResumed(activity: Activity) {//2 to forw
        super.onActivityResumed(activity)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.list_games, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.menu_add_game -> {
                view.showAddGameDialog()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onItemClick(game: Game) {
        val router = parentController?.router ?: router
        router.pushController(RouterTransaction.with(GamePagerController(world, game.id)))
    }

    override fun getWorldName(): String {
        return world.name
    }

    override fun createGame(name: String) {
        App.instance.gameStorage.create(name, world) { game ->
            gamesSet.add(game)
            view.addedAt(0, game)
        }
    }

    override fun archiveGameAt(pos: Int) {
        val game = gamesSet.toList()[pos]
        game.archived = true

        db.gameDao().update(game)
        gamesSet.remove(game)
        view.archivedAt(pos)
    }

    override fun renameGame(pos: Int, game: Game, name: String) {
        game.name = name
        db.gameDao().update(game)
        view.itemChangedAt(pos)
    }
}