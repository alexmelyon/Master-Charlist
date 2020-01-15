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
import java.util.concurrent.CountDownLatch
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
            val countDownLatch = CountDownLatch(effects.size)
            val effectRows = mutableListOf<EffectRow>()
            effects.forEach { effect ->
                effect.getSkillToValue(App.instance.skillStorage) { skillToValueList ->
                    val effectSkillRows = skillToValueList.map { EffectSkillRow(it.skill.name, it.value, it.skill) }
                    val effectRow = EffectRow(effect.name, effectSkillRows, effect)
                    effectRows.add(effectRow)
                    countDownLatch.countDown()
                }
            }
            countDownLatch.await()
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

    override fun getAvailableSkillsForEffect(effect: Effect, onSuccess: (List<Skill>) -> Unit) {
        effect.getAvailableSkills(App.instance.skillStorage) { skills ->
            onSuccess(skills)
        }
    }

    override fun getUsedEffectSkills(effect: Effect, onSuccess: (List<SkillnameToEffectskill>) -> Unit) {
        effect.getUsedEffectSkills(App.instance.skillStorage) { list ->
            onSuccess(list)
        }
    }

    override fun attachSkillForEffect(pos: Int, effect: Effect, skill: Skill) {
        App.instance.effectStorage.attachSkillForEffect(effect, skill) {
            updateEffectRow(effectItems[pos], effect) {
                view.itemChangedAt(pos)
            }
        }
    }

    override fun detachSkillForEffect(pos: Int, effect: Effect, effectSkill: EffectSkill) {
        App.instance.effectStorage.detachSkillFromEffect(effect, effectSkill) {
            updateEffectRow(effectItems[pos], effect) {
                view.itemChangedAt(pos)
            }
        }
    }

    override fun onEffectSkillChanged(pos: Int, effect: Effect, skill: Skill, delta: Int) {
        App.instance.effectStorage.updateEffectSkillValue(effect, skill, delta) {
            updateEffectRow(effectItems[pos], effect) {
                view.itemChangedAt(pos)
            }
        }
    }

    fun updateEffectRow(effectRow: EffectRow, effect: Effect, onSuccess: () -> Unit) {
        effect.getSkillToValue(App.instance.skillStorage) { skillToValue ->
            effectRow.effectSkills = skillToValue.map { EffectSkillRow(it.skill.name, it.value, it.skill) }
            onSuccess()
        }
    }
}