package com.github.alexmelyon.master_charlist.list_effects

import android.view.ViewGroup
import com.github.alexmelyon.master_charlist.room.Effect
import com.github.alexmelyon.master_charlist.room.EffectSkill
import com.github.alexmelyon.master_charlist.room.Skill

interface ListEffectsContract {
    interface View {
        fun createView(container: ViewGroup): android.view.View
        fun setData(items: MutableList<EffectRow>)
        fun showAddEffectDialog()
        fun itemAddedAt(pos: Int, effect: EffectRow)
        fun itemArchivedAt(pos: Int)
        fun itemChangedAt(pos: Int)
    }

    interface Controller {
        fun createEffect(effectName: String)
        fun archiveEffect(pos: Int, effect: Effect)
        fun getAvailableSkillsForEffect(effect: Effect): List<Skill>
        fun getUsedEffectSkills(effect: Effect): List<Pair<String, EffectSkill>>
        fun attachSkillForEffect(pos: Int, effect: Effect, skill: Skill)
        fun detachSkillForEffect(pos: Int, effect: Effect, effectSkill: EffectSkill)
        fun onEffectSkillChanged(pos: Int, effect: Effect, skill: Skill, delta: Int)
        fun renameEffect(pos: Int, effect: Effect, name: String)
        fun createSkill(name: String): Skill
    }
}

class EffectSkillRow(val name: String, val value: Int, val skill: Skill)

class EffectRow(var name: String, var effectSkills: List<EffectSkillRow>, val effect: Effect)