package com.helloandroid.session

import android.view.ViewGroup
import com.helloandroid.room.*
import java.util.*

interface SessionContract {
    interface View {
        fun createView(container: ViewGroup): android.view.View
        fun setData(items: MutableList<SessionItem>)
        fun itemChangedAt(pos: Int)
        fun itemAddedAt(pos: Int, sessionItem: SessionItem)
        fun itemRemovedAt(pos: Int)
        /** Chooose Hp, Effect, Skill, Thing or Comment */
        fun showAddSomethingDialog()
        fun showCreateCharacterDialog()
        fun showAddHpDialog(characterNames: List<String>)
        fun showAddSkillDialog(characters: List<GameCharacter>, skills: List<Skill>)
        fun showAddThingDialog(characters: List<GameCharacter>, things: List<Thing>)
        fun showAttachEffectDialog(characters: List<GameCharacter>, effects: List<Effect>)
        fun showRemoveEffectDialog(characterNames: List<String>, characterToEffectNames: Map<String, List<Effect>>)
        fun showAddComment()
        fun showCloseSessionDialog(name: String)
    }
    interface Controller {
        fun getTitle(): String
        fun onHpChanged(pos: Int, value: Int)
        fun onSkillChanged(pos: Int, value: Int)
        fun onThingChanged(pos: Int, value: Int)
        fun onCommentChanged(pos: Int, comment: String)
        fun addHpDiff(character: Int)
        fun addCharacterSkillDiff(character: GameCharacter, skill: Skill)
        fun addCharacterAttachEffectDiff(character: GameCharacter, effect: Effect)
        fun addCharacterDetachEffectDiff(character: Int, effect: Int)
        fun getAvailableSkillsForEffect(pos: Int): List<Skill>
        fun attachSkillForEffect(pos: Int, skill: Skill)
        fun detachSkillForEffect(pos: Int, effectSkill: EffectSkill)
        fun onEffectSkillChanged(pos: Int, subPos: Int, value: Int)
        fun addCharacterThingDiff(character: GameCharacter, thing: Thing)
        fun addCommentDiff()
        fun onAddSomethingClicked(which: Int)
        fun closeSession()
        fun isSessionOpen(): Boolean
        fun createCharacter(name: String)
        fun createSkill(name: String): Skill
        fun createThing(name: String): Thing
        fun createEffect(name: String): Effect
        fun getUsedEffectSkills(pos: Int): List<Pair<String, EffectSkill>>
    }
}

enum class SessionItemType {
    ITEM_HP,
    ITEM_SKILL,
    ITEM_THING,
    ITEM_EFFECT,
    ITEM_COMMENT
}

class SessionItem(val id: Long, val time: Date, val type: SessionItemType, val title: String, val desc: String, var value: Int, val characterId: Long, var comment: String = "", var effectSkills: List<Pair<String, Int>> = listOf(), var index: Int = -1)