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
        fun showAddHpDialog(characterNames: List<String>)
        fun showAddSkillDialog(characterNames: List<String>, skillNames: List<String>)
        fun showAddThingDialog(characterNames: List<String>, thingNames: List<String>)
        fun showAttachEffectDialog(characterNames: List<String>, effectNames: List<String>)
        fun showRemoveEffectDialog(characterNames: List<String>, characterToEffectNames: Map<String, List<Effect>>)
        fun showAddComment()
        fun showCloseSessionDialog(name: String)
        fun showCreateCharacterDialog()
        fun showCreateSkillDialog()
        fun showCreateThingDialog()
        fun showCreateEffectDialog()
    }
    interface Controller {
        fun getTitle(): String
        fun onHpChanged(pos: Int, value: Int)
        fun onSkillChanged(pos: Int, value: Int)
        fun onThingChanged(pos: Int, value: Int)
        fun onCommentChanged(pos: Int, comment: String)
        fun addHpDiff(character: Int)
        fun addCharacterSkillDiff(character: Int, skill: Int)
        fun addCharacterAttachEffectDiff(character: Int, effect: Int)
        fun addCharacterDetachEffectDiff(character: Int, effect: Int)
        fun getAvailableSkillsForEffect(pos: Int): List<Skill>
        fun attachSkillForEffect(pos: Int, skill: Skill)
        fun detachSkillForEffect(pos: Int, effectSkill: EffectSkill)
        fun addCharacterThingDiff(character: Int, thing: Int)
        fun addCommentDiff()
        fun onAddItemClicked(which: Int)
        fun closeSession()
        fun isSessionOpen(): Boolean
        fun createCharacter(name: String)
        fun createSkill(name: String)
        fun createThing(name: String)
        fun createEffect(name: String)
        fun getUsedSkillEffects(pos: Int): Map<String, EffectSkill>
    }
}

enum class SessionItemType {
    ITEM_HP,
    ITEM_SKILL,
    ITEM_THING,
    ITEM_EFFECT,
    ITEM_COMMENT
}

class SessionItem(val id: Long, val time: Date, val type: SessionItemType, val title: String, val desc: String, var value: Int, val characterId: Long, var comment: String = "", var effectSkills: List<String> = listOf(), var index: Int = -1)