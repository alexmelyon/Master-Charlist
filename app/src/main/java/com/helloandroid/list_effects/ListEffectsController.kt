package com.helloandroid.list_effects

import android.content.Context
import android.os.Bundle
import android.view.*
import com.bluelinelabs.conductor.Controller
import com.helloandroid.R
import com.helloandroid.list_games.WORLD_KEY
import com.helloandroid.room.AppDatabase
import com.helloandroid.room.Effect
import com.helloandroid.room.World
import ru.napoleonit.talan.di.ControllerInjector
import java.util.*
import javax.inject.Inject

class ListEffectsController(args: Bundle) : Controller(args), ListEffectsContract.Controller {

    @Inject
    lateinit var view: ListEffectsContract.View
    @Inject
    lateinit var db: AppDatabase

    lateinit var world: World

    constructor(worldId: Long) : this(Bundle().apply {
        putLong(WORLD_KEY, worldId)
    })

    override fun onContextAvailable(context: Context) {
        super.onContextAvailable(context)
        ControllerInjector.inject(this)
        world = db.worldDao().getWorldById(args.getLong(WORLD_KEY))
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View {
        return view.createView(container)
    }

    override fun onAttach(view: View) {
        super.onAttach(view)
        val effects = db.effectDao().getAll(world.id, archived = false)
            .sortedWith(
                compareByDescending<Effect> { it.lastUsed }
                .thenBy { it.name }
            ).toMutableList()
        this.view.setData(effects)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.list_effects, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.menu_add_effect -> {
                view.showAddEffectDialog()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun createEffect(effectName: String) {
        val effect = Effect(effectName, world.id, Calendar.getInstance().time)
        val id = db.effectDao().insert(effect)
        effect.id = id

        view.addedAt(0, effect)
    }

    override fun archiveEffect(pos: Int, effect: Effect) {
        effect.archived = true
        db.effectDao().update(effect)

        view.archivedAt(pos)
    }
}