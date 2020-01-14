package com.github.alexmelyon.master_charlist.list_effects

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import com.bluelinelabs.conductor.Controller
import com.crashlytics.android.Crashlytics
import com.github.alexmelyon.master_charlist.App
import com.github.alexmelyon.master_charlist.R
import com.github.alexmelyon.master_charlist.list_games.WORLD_KEY
import com.github.alexmelyon.master_charlist.room.*
import ru.napoleonit.talan.di.ControllerInjector
import javax.inject.Inject

class ListEffectsController(args: Bundle) : Controller(args), ListEffectsContract.Controller {

    @Inject
    lateinit var view: ListEffectsContract.View
    @Inject
    lateinit var db: AppDatabase

    lateinit var world: World
    lateinit var effectItems: MutableList<EffectRow>

    constructor(world: World) : this(Bundle().apply {
        putParcelable(WORLD_KEY, world)
    })

    override fun onContextAvailable(context: Context) {
        super.onContextAvailable(context)
        ControllerInjector.inject(this)
        world = args.getParcelable<World>(WORLD_KEY)!!
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View {
        Crashlytics.log(Log.INFO, javaClass.simpleName, "onCreateView")
        return view.createView(container)
    }

    override fun onAttach(view: View) {
        super.onAttach(view)
        updateEffects()
    }

    fun updateEffects() {
        App.instance.effectStorage.getAll(world) { effects ->
            val effectRows = effects.map {
                val effectSkills = it.getSkillToValue(App.instance.skillStorage)
                    .map { EffectSkillRow(it.skill.name, it.value, it.skill) }
                EffectRow(it.name, effectSkills, it)
            }
            this.view.setData(effectRows.toMutableList())
        }
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

    // TODO Don't create skills in ListEffectsController
    override fun createSkill(name: String, onSuccess: (Skill) -> Unit) {
        App.instance.skillStorage.create(name, world) { skill ->
            onSuccess(skill)
        }
    }

    override fun createEffect(effectName: String) {
        App.instance.effectStorage.create(effectName, world) { effect ->
            val effectRow = EffectRow(effect.name, listOf(), effect)
            view.itemAddedAt(0, effectRow)
        }
    }

    override fun archiveEffect(pos: Int, effect: Effect) {
        App.instance.effectStorage.archive(effect) {
            view.itemArchivedAt(pos)
        }
    }

    override fun renameEffect(pos: Int, effect: Effect, name: String) {
        App.instance.effectStorage.rename(effect, name) {
            view.itemChangedAt(pos)
        }
    }

    override fun getAvailableSkillsForEffect(effect: Effect): List<Skill> {
        return effect.getAvailableSkills(App.instance.skillStorage)
    }

    override fun attachSkillForEffect(pos: Int, effect: Effect, skill: Skill) {
        App.instance.effectStorage.attachSkillForEffect(effect, skill) {
            view.itemChangedAt(pos)
        }
    }

    override fun getUsedEffectSkills(effect: Effect): List<Pair<String, EffectSkill>> {
        return effect.getUsedEffectSkills(App.instance.skillStorage)
    }

    override fun detachSkillForEffect(pos: Int, effect: Effect, effectSkill: EffectSkill) {
        App.instance.effectStorage.detachSkillFromEffect(effect, effectSkill) {
            view.itemChangedAt(pos)
        }
    }

    override fun onEffectSkillChanged(pos: Int, effect: Effect, skill: Skill, delta: Int) {
        val effectSkill = db.effectSkillDao().get(world.id, effect.id, skill.id)
        effectSkill.value += delta
        db.effectSkillDao().update(effectSkill)

        effectItems[pos].effectSkills = effect.getSkillToValue(App.instance.skillStorage)
            .map { EffectSkillRow(it.skill.name, it.value, it.skill) }
        view.itemChangedAt(pos)
    }
}