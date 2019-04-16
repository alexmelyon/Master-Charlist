package com.helloandroid.list_sessions

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.*
import com.bluelinelabs.conductor.Controller
import com.bluelinelabs.conductor.RouterTransaction
import com.helloandroid.R
import com.helloandroid.list_characters.ListCharactersDelegate
import com.helloandroid.list_games.WORLD_KEY
import com.helloandroid.room.AppDatabase
import com.helloandroid.room.Game
import com.helloandroid.room.GameSession
import com.helloandroid.room.World
import com.helloandroid.session.SessionController
import ru.napoleonit.talan.di.ControllerInjector
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

val GAME_KEY = "GAME_KEY"

interface ListSessionsDelegate {
    fun updateListSessionsScreen(activity: Activity)
}

class ListSessionsController(args: Bundle) : Controller(args), ListSessionsContract.Controller, ListSessionsDelegate {

    @Inject
    lateinit var view: ListSessionsContract.View
    @Inject
    lateinit var db: AppDatabase

    lateinit var world: World
    lateinit var game: Game
    var delegate: WeakReference<ListCharactersDelegate>? = null

    private lateinit var sessionsList: MutableList<GameSession>
    private var firstClosedSessionIndex = 0

    constructor(worldId: Long, gameId: Long) : this(Bundle().apply {
        putLong(WORLD_KEY, worldId)
        putLong(GAME_KEY, gameId)
    })

    override fun onContextAvailable(context: Context) {
        super.onContextAvailable(context)
        ControllerInjector.inject(this)

        world = db.worldDao().getWorldById(args.getLong(WORLD_KEY))
        game = db.gameDao().getAll(args.getLong(GAME_KEY), world.id)
        updateScreen()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View {
        return view.createView(container)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.list_sessions, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_add_session -> {
                createSession()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun updateListSessionsScreen(activity: Activity) {
        updateScreen()
        delegate?.get()?.updateCharactersScreen()
    }

    fun updateScreen() {
        sessionsList = db.gameSessionDao().getAll(world.id, game.id, archived = false)
            .sortedWith(Comparator { o1, o2 ->
                // TODO My own comparator
                if (o1.open != o2.open) {
                    return@Comparator o2.open.compareTo(o1.open)
                }
                if (o1.open && o1.startTime != o2.startTime) {
                    return@Comparator o2.startTime.compareTo(o1.startTime)
                }
                if (!o1.open && o1.endTime != o2.endTime) {
                    return@Comparator o2.endTime.compareTo(o1.endTime)
                }
                return@Comparator o1.name.compareTo(o2.name)
            })
            .toMutableList()
        firstClosedSessionIndex = sessionsList.mapIndexed { index, it -> index to it }
            .firstOrNull { !it.second.open }
            ?.first ?: -1
    }

    override fun onAttach(view: View) {
        super.onAttach(view)
        this.view.setData(sessionsList)
    }

    override fun getDescription(pos: Int): String {
        val session = sessionsList[pos]
        return session.startTime.let { SimpleDateFormat("EEEE d MMMM HH:mm yyyy", Locale.getDefault()).format(it) }
    }

    override fun getHeader(pos: Int): String {
        if (pos == 0 && sessionsList[0].open) {
            return "Open sessions"
        }
        if (pos == firstClosedSessionIndex) {
            return "Closed sessions"
        }
        return ""
    }

    override fun onItemClick(session: GameSession) {
        val router = parentController?.router ?: this.router
        router.pushController(RouterTransaction.with(SessionController(session.id, game.id, world.id).apply {
            delegate = WeakReference(this@ListSessionsController)
        }))
    }

    override fun getGameName(): String {
        return game.name
    }

    override fun createSession() {
        val now = Calendar.getInstance().time
        val name = now.let { SimpleDateFormat("EEEE d MMMM HH:mm yyyy", Locale.getDefault()).format(it) }
        val session = GameSession(name, game.id, world.id, now, open = true, endTime = now)
        val id = db.gameSessionDao().insert(session)
        session.id = id

        updateScreen()
        view.setData(sessionsList)
    }

    override fun archiveSession(pos: Int, session: GameSession) {
        session.archived = true
        db.gameSessionDao().update(session)

        view.archivedAt(pos)
        delegate?.get()?.updateCharactersScreen()
    }
}