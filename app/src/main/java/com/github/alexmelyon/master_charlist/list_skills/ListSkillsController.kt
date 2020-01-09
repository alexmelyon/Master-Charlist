package com.github.alexmelyon.master_charlist.list_skills

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
import com.github.alexmelyon.master_charlist.room.Skill
import com.github.alexmelyon.master_charlist.room.World
import ru.napoleonit.talan.di.ControllerInjector
import java.util.*
import javax.inject.Inject

class ListSkillsController(args: Bundle) : Controller(args), ListSkillsContract.Controller {

    @Inject
    lateinit var view: ListSkillsContract.View
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
        val skills = db.skillDao().getAll(world.id)
            .sortedWith(kotlin.Comparator { o1, o2 ->
                var res = o2.lastUsed.compareTo(o1.lastUsed)
                if(res == 0) {
                    res =  o1.name.compareTo(o2.name)
                }
                return@Comparator res
            })
            .toMutableList()
        this.view.setData(skills)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.list_skills, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.menu_add_skill -> {
                view.showAddSkillDialog()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun archiveSkill(pos: Int, skill: Skill) {
        App.instance.skillStorage.archive(skill) {
            view.archivedAt(pos)
        }
    }

    override fun createSkill(skillName: String) {
        App.instance.skillStorage.create(skillName, world) { skill ->
            view.addedAt(0, skill)
        }
    }

    override fun renameSkill(pos: Int, skill: Skill, name: String) {
        App.instance.skillStorage.rename(skill, name) {
            view.itemChangedAt(pos)
        }
    }
}