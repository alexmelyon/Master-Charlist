package com.github.alexmelyon.master_charlist.session

import android.content.DialogInterface
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import com.github.alexmelyon.master_charlist.MainActivity
import com.github.alexmelyon.master_charlist.R
import com.github.alexmelyon.master_charlist.room.Effect
import com.github.alexmelyon.master_charlist.room.GameCharacter
import com.github.alexmelyon.master_charlist.room.Skill
import com.github.alexmelyon.master_charlist.room.Thing
import com.github.alexmelyon.master_charlist.utils.showAlertEditDialog
import org.jetbrains.anko._FrameLayout
import org.jetbrains.anko.recyclerview.v7.recyclerView
import javax.inject.Inject

class SessionView @Inject constructor(val activity: MainActivity) : _FrameLayout(activity), SessionContract.View {

    @Inject
    lateinit var controller: SessionContract.Controller

    lateinit var listAdapter: SessionDiffsAdapter

    override fun createView(container: ViewGroup): View {
        activity.supportActionBar!!.title = controller.getTitle()
        listAdapter = SessionDiffsAdapter(container.context, editable = controller.isSessionOpen()).apply {
            onItemMinus = { pos, type ->
                when (type) {
                    SessionItemType.ITEM_HP -> controller.onHpChanged(pos, -1)
                    SessionItemType.ITEM_SKILL -> controller.onSkillChanged(pos, -1)
                    SessionItemType.ITEM_THING -> controller.onThingChanged(pos, -1)
                }
            }
            onItemPlus = { pos, type ->
                when (type) {
                    SessionItemType.ITEM_HP -> controller.onHpChanged(pos, +1)
                    SessionItemType.ITEM_SKILL -> controller.onSkillChanged(pos, +1)
                    SessionItemType.ITEM_THING -> controller.onThingChanged(pos, +1)
                }
            }
            onCommentChanged = { id, comment ->
                controller.onCommentChanged(id, comment)
            }
            onItemClickListener = { pos ->
                val skillsForEffect = controller.getAvailableSkillsForEffect(pos)
                val skillNames = listOf(context.getString(R.string.create_new)) + skillsForEffect.map { context.getString(R.string.attach_something, it.name) }
                AlertDialog.Builder(activity)
                    .setItems(skillNames.toTypedArray(), DialogInterface.OnClickListener { dialog, which ->
                        if(which == 0) {
                            showCreateSkillDialog { skill ->
                                controller.attachSkillForEffect(pos, skill)
                            }
                        } else {
                            val skill = skillsForEffect[which - 1]
                            controller.attachSkillForEffect(pos, skill)
                        }
                    })
                    .show()
            }
            onItemLongClickListener = { pos ->
                val usedSkills = controller.getUsedEffectSkills(pos)
                val skillNames = usedSkills.map { it.first }
                AlertDialog.Builder(activity)
                    .setItems(skillNames.toTypedArray(), DialogInterface.OnClickListener { dialog, which ->
                        val effectSkill = usedSkills[which].second
                        controller.detachSkillForEffect(pos, effectSkill)
                    })
                    .show()
            }
            onSubitemPlus = { pos, subPos ->
                controller.onEffectSkillChanged(pos, subPos, +1)
            }
            onSubitemMinus = { pos, subPos ->
                controller.onEffectSkillChanged(pos, subPos, -1)
            }
        }
        return container.context.recyclerView {
            adapter = listAdapter
        }
    }

    override fun setData(items: MutableList<SessionItem>) {
        listAdapter.items = items
    }

    override fun itemChangedAt(pos: Int) {
        listAdapter.notifyItemChanged(pos)
    }

    override fun itemAddedAt(pos: Int, sessionItem: SessionItem) {
        listAdapter.itemAdded(pos, sessionItem)
    }

    override fun itemRemovedAt(pos: Int) {
        listAdapter.items.removeAt(pos)
        listAdapter.notifyItemRemoved(pos)
    }

    override fun showAddSomethingDialog() {
        AlertDialog.Builder(activity)
            .setItems(arrayOf(
                context.getString(R.string.add_character),
                context.getString(R.string.add_healthpoints),
                context.getString(R.string.add_skill),
                context.getString(R.string.add_thing),
                context.getString(R.string.attach_effect),
                context.getString(R.string.detach_effect),
                context.getString(R.string.add_comment)
            ), DialogInterface.OnClickListener { dialog, which ->
                controller.onAddSomethingClicked(which)
            })
            .show()
    }

    override fun showAddHpDialog(characterNames: List<String>) {
        AlertDialog.Builder(activity)
            .setTitle(context.getString(R.string.select_character))
            .setItems(characterNames.toTypedArray(), DialogInterface.OnClickListener { dialog, character ->
                controller.addHpDiff(character)
            })
            .show()
    }

    override fun showAddSkillDialog(characters: List<GameCharacter>, skills: List<Skill>) {
        val characterNames = characters.map { it.name }
        AlertDialog.Builder(activity)
            .setTitle(context.getString(R.string.select_character))
            .setItems(characterNames.toTypedArray(), DialogInterface.OnClickListener { dialog, whichCharacter ->
                val character = characters[whichCharacter]
                val skillNames = listOf(context.getString(R.string.create_new)) + skills.map { it.name }
                AlertDialog.Builder(activity)
                    .setTitle(context.getString(R.string.select_skill))
                    .setItems(skillNames.toTypedArray(), DialogInterface.OnClickListener { dialog, whichSkill ->
                        if(whichSkill == 0) {
                            showCreateSkillDialog { skill ->
                                controller.addCharacterSkillDiff(character, skill)
                            }
                        } else {
                            val skill = skills[whichSkill - 1]
                            controller.addCharacterSkillDiff(character, skill)
                        }
                    })
                    .show()
            }).show()
    }

    override fun showAddThingDialog(characters: List<GameCharacter>, things: List<Thing>) {
        val characterNames = characters.map { it.name }
        AlertDialog.Builder(activity)
            .setTitle(context.getString(R.string.select_character))
            .setItems(characterNames.toTypedArray(), DialogInterface.OnClickListener { dialog, whichCharacter ->
                val character = characters[whichCharacter]
                val thingNames = listOf(context.getString(R.string.create_new)) + things.map { it.name }
                AlertDialog.Builder(activity)
                    .setTitle(context.getString(R.string.select_thing))
                    .setItems(thingNames.toTypedArray(), DialogInterface.OnClickListener { dialog, whichThing ->
                        if(whichThing == 0) {
                            showCreateThingDialog { thing ->
                                controller.addCharacterThingDiff(character, thing)
                            }
                        } else {
                            val thing = things[whichThing - 1]
                            controller.addCharacterThingDiff(character, thing)
                        }
                    }).show()
            }).show()
    }

    override fun showAddEffectDialog(characters: List<GameCharacter>, effects: List<Effect>) {
        val characterNames = characters.map { it.name }
        AlertDialog.Builder(activity)
            .setTitle(context.getString(R.string.select_character))
            .setItems(characterNames.toTypedArray(), DialogInterface.OnClickListener { dialog, whichCharacter ->
                val character = characters[whichCharacter]
                val effectNames = listOf(context.getString(R.string.create_new)) + effects.map { it.name }
                AlertDialog.Builder(activity)
                    .setTitle(context.getString(R.string.select_effect))
                    .setItems(effectNames.toTypedArray(), DialogInterface.OnClickListener { dialog, whichEffect ->
                        if(whichEffect == 0) {
                            showCreateEffectDialog { effect ->
                                controller.addCharacterAttachEffectDiff(character, effect)
                            }
                        } else {
                            val effect = effects[whichEffect - 1]
                            controller.addCharacterAttachEffectDiff(character, effect)
                        }
                    })
                    .show()
            })
            .show()
    }

    override fun showRemoveEffectDialog(characterNames: List<String>, characterToEffect: Map<String, List<Effect>>) {
        AlertDialog.Builder(activity)
            .setTitle(context.getString(R.string.select_character))
            .setItems(characterNames.toTypedArray(), DialogInterface.OnClickListener { dialog, character ->
                val characterName = characterNames[character]
                val usedEffects = characterToEffect[characterName]?.map { it.name } ?: listOf()
                AlertDialog.Builder(activity)
                    .setTitle(context.getString(R.string.select_effect))
                    .setItems(usedEffects.toTypedArray(), DialogInterface.OnClickListener { dialog, effect ->
                        controller.addCharacterDetachEffectDiff(character, effect)
                    })
                    .show()
            })
            .show()
    }

    override fun showAddComment() {
        controller.addCommentDiff()
    }

    override fun showCreateCharacterDialog() {
        activity.showAlertEditDialog(context.getString(R.string.character_name_colon)) { name ->
            controller.createCharacter(name)
            AlertDialog.Builder(activity)
                .setTitle(context.getString(R.string.character_created))
                .setMessage(context.getString(R.string.name_something, name))
                .setPositiveButton("OK", DialogInterface.OnClickListener { dialog, which ->  })
                .show()
        }
    }

    fun showCreateSkillDialog(action: (Skill) -> Unit) {
        activity.showAlertEditDialog(context.getString(R.string.skill_name_colon)) { name ->
            controller.createSkill(name) { skill ->
                action(skill)
            }
        }
    }

    fun showCreateThingDialog(action: (Thing) -> Unit) {
        activity.showAlertEditDialog(context.getString(R.string.thing_name_colon)) { name ->
            val thing = controller.createThing(name)
            action(thing)
        }
    }

    fun showCreateEffectDialog(action: (Effect) -> Unit) {
        activity.showAlertEditDialog(context.getString(R.string.effect_name_colon)) { name ->
            val effect = controller.createEffect(name)
            action(effect)
        }
    }

    override fun showCloseSessionDialog(name: String) {
        AlertDialog.Builder(activity)
            .setTitle(context.getString(R.string.close_this_session_question))
            .setMessage(name)
            .setPositiveButton("OK", DialogInterface.OnClickListener { dialog, which ->
                controller.closeSession()
            })
            .setNegativeButton(context.getString(R.string.cancel), DialogInterface.OnClickListener { dialog, which ->
                dialog.dismiss()
            })
            .show()
    }
}