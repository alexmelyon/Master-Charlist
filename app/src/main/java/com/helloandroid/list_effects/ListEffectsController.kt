package com.helloandroid.list_effects

import android.content.Context
import android.os.Bundle
import android.view.*
import com.bluelinelabs.conductor.Controller
import com.helloandroid.R
import com.helloandroid.list_games.WORLD_KEY
import com.helloandroid.room.*
import ru.napoleonit.talan.di.ControllerInjector
import java.util.*
import javax.inject.Inject

class ListEffectsController(args: Bundle) : Controller(args), ListEffectsContract.Controller {

    @Inject
    lateinit var view: ListEffectsContract.View
    @Inject
    lateinit var db: AppDatabase

    lateinit var world: World
    lateinit var effectItems: MutableList<EffectRow>

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
        effectItems = db.effectDao().getAll(world.id, archived = false)
            .sortedWith(compareByDescending<Effect> { it.lastUsed }
                .thenBy { it.name }
            ).map { effect ->
                val effectSkills = effect.getSkillToValue(db)
                    .map { EffectSkillRow(it.first.name, it.second, it.first) }
                EffectRow(effect.name, effectSkills, effect)
            }.toMutableList()
        this.view.setData(effectItems)
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

    override fun createSkill(name: String): Skill {
        val skill = Skill(name, world.id, Calendar.getInstance().time)
        val id = db.skillDao().insert(skill)
        skill.id = id
        return skill
    }

    override fun createEffect(effectName: String) {
        val effect = Effect(effectName, world.id, Calendar.getInstance().time)
        val id = db.effectDao().insert(effect)
        effect.id = id

        val effectRow = EffectRow(effect.name, listOf(), effect)
        view.itemAddedAt(0, effectRow)
    }

    override fun archiveEffect(pos: Int, effect: Effect) {
        effect.archived = true
        db.effectDao().update(effect)

        view.itemArchivedAt(pos)
    }

    override fun renameEffect(pos: Int, effect: Effect, name: String) {
        effect.name = name
        db.effectDao().update(effect)

        effectItems[pos].name = name
        view.itemChangedAt(pos)
    }

    override fun getAvailableSkillsForEffect(effect: Effect): List<Skill> {
        return effect.getAvailableSkills(db)
    }

    override fun attachSkillForEffect(pos: Int, effect: Effect, skill: Skill) {
        val effectSkill = EffectSkill(0, effect.id, skill.id, world.id)
        val id = db.effectSkillDao().insert(effectSkill)
        effectSkill.id = id

        effectItems[pos].effectSkills = effect.getSkillToValue(db)
            .map { EffectSkillRow(it.first.name, it.second, it.first) }
        view.itemChangedAt(pos)
    }

    override fun getUsedEffectSkills(effect: Effect): List<Pair<String, EffectSkill>> {
        return effect.getUsedEffectSkills(db)
    }

    override fun detachSkillForEffect(pos: Int, effect: Effect, effectSkill: EffectSkill) {
        db.effectSkillDao().delete(effectSkill)
        effectItems[pos].effectSkills = effect.getSkillToValue(db)
            .map { EffectSkillRow(it.first.name, it.second, it.first) }

        view.itemChangedAt(pos)
    }

    override fun onEffectSkillChanged(pos: Int, effect: Effect, skill: Skill, delta: Int) {
        val effectSkill = db.effectSkillDao().get(world.id, effect.id, skill.id)
        effectSkill.value += delta
        db.effectSkillDao().update(effectSkill)

        effectItems[pos].effectSkills = effect.getSkillToValue(db)
            .map { EffectSkillRow(it.first.name, it.second, it.first) }
        view.itemChangedAt(pos)
    }
}