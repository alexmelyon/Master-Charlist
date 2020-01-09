package com.github.alexmelyon.master_charlist.list_things

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import com.bluelinelabs.conductor.Controller
import com.crashlytics.android.Crashlytics
import com.github.alexmelyon.master_charlist.App
import com.github.alexmelyon.master_charlist.R
import com.github.alexmelyon.master_charlist.list_games.WORLD_KEY
import com.github.alexmelyon.master_charlist.room.AppDatabase
import com.github.alexmelyon.master_charlist.room.Thing
import com.github.alexmelyon.master_charlist.room.World
import ru.napoleonit.talan.di.ControllerInjector
import java.util.*
import javax.inject.Inject

class ListThingsController(args: Bundle) : Controller(args), ListThingsContract.Controller {

    @Inject
    lateinit var view: ListThingsContract.View
    @Inject
    lateinit var db: AppDatabase

    lateinit var world: World

    constructor(world: World) : this(Bundle().apply {
        putParcelable(WORLD_KEY, world)
    })

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View {
        Crashlytics.log(Log.INFO, javaClass.simpleName, "onCreateView")
        return view.createView(container)
    }


    override fun onContextAvailable(context: Context) {
        super.onContextAvailable(context)
        ControllerInjector.inject(this)
        world = args.getParcelable<World>(WORLD_KEY)!!
    }

    override fun onAttach(view: View) {
        super.onAttach(view)
        updateThings()
    }

    fun updateThings() {
        App.instance.thingStorage.getAll(world) { things ->
            this.view.setData(things.toMutableList())
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.list_things, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_add_thing -> {
                view.showAddThingDialog()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun archiveThing(pos: Int, thing: Thing) {
        App.instance.thingStorage.archive(thing) {
            view.archivedAt(pos)
        }
    }

    override fun createThing(thingName: String) {
        App.instance.thingStorage.create(thingName, world) { thing ->
            view.addedAt(0, thing)
        }
    }

    override fun renameThing(pos: Int, thing: Thing, name: String) {
        App.instance.thingStorage.rename(thing, name) {
            view.itemChangedAt(pos)
        }
    }
}