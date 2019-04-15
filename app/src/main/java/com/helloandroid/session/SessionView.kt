package com.helloandroid.session

import android.content.DialogInterface
import android.support.v7.app.AlertDialog
import android.view.View
import android.view.ViewGroup
import com.helloandroid.MainActivity
import com.helloandroid.room.Effect
import com.helloandroid.room.GameCharacter
import com.helloandroid.room.Skill
import com.helloandroid.room.Thing
import com.helloandroid.utils.alertNoAvailableSkills
import com.helloandroid.utils.showAlertEditDialog
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
                val skillNames = skillsForEffect.map { "Attach ${it.name}" }
                if(skillNames.isEmpty()) {
                    activity.alertNoAvailableSkills()
                } else {
                    AlertDialog.Builder(activity)
                        .setItems(skillNames.toTypedArray(), DialogInterface.OnClickListener { dialog, which ->
                            val skill = skillsForEffect[which]
                            controller.attachSkillForEffect(pos, skill)
                        })
                        .show()
                }
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
            .setItems(arrayOf(/* TODO Add Character */ "Add Healthpoints", "Add Skill", "Add Thing", "Attach Effect", "Detach effect", "Add Comment"), DialogInterface.OnClickListener { dialog, which ->
                controller.onAddItemClicked(which)
            })
            .show()
    }

    override fun showAddHpDialog(characterNames: List<String>) {
        AlertDialog.Builder(activity)
            .setTitle("Select character")
            .setItems(characterNames.toTypedArray(), DialogInterface.OnClickListener { dialog, character ->
                controller.addHpDiff(character)
            })
            .show()
    }

    override fun showAddSkillDialog(characters: List<GameCharacter>, skills: List<Skill>) {
        val characterNames = characters.map { it.name }
        AlertDialog.Builder(activity)
            .setTitle("Select character")
            .setItems(characterNames.toTypedArray(), DialogInterface.OnClickListener { dialog, whichCharacter ->
                val character = characters[whichCharacter]
                val skillNames = listOf("Create new...") + skills.map { it.name }
                AlertDialog.Builder(activity)
                    .setTitle("Select skill")
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
            .setTitle("Select character")
            .setItems(characterNames.toTypedArray(), DialogInterface.OnClickListener { dialog, whichCharacter ->
                val character = characters[whichCharacter]
                val thingNames = listOf("Create new...") + things.map { it.name }
                AlertDialog.Builder(activity)
                    .setTitle("Select thing")
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

    override fun showAttachEffectDialog(characters: List<GameCharacter>, effects: List<Effect>) {
        val characterNames = characters.map { it.name }
        AlertDialog.Builder(activity)
            .setTitle("Select character")
            .setItems(characterNames.toTypedArray(), DialogInterface.OnClickListener { dialog, whichCharacter ->
                val character = characters[whichCharacter]
                val effectNames = listOf("Create new...") + effects.map { it.name }
                AlertDialog.Builder(activity)
                    .setTitle("Select effect")
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
            .setTitle("Select character")
            .setItems(characterNames.toTypedArray(), DialogInterface.OnClickListener { dialog, character ->
                val characterName = characterNames[character]
                val usedEffects = characterToEffect[characterName]?.map { it.name } ?: listOf()
                AlertDialog.Builder(activity)
                    .setTitle("Select effect")
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

    fun showCreateCharacterDialog() {
        activity.showAlertEditDialog("Character name:") { name ->
            controller.createCharacter(name)
        }
    }

    fun showCreateSkillDialog(action: (Skill) -> Unit) {
        activity.showAlertEditDialog("Skill name:") { name ->
            val skill = controller.createSkill(name)
            action(skill)
        }
    }

    fun showCreateThingDialog(action: (Thing) -> Unit) {
        activity.showAlertEditDialog("Thing name:") { name ->
            val thing = controller.createThing(name)
            action(thing)
        }
    }

    fun showCreateEffectDialog(action: (Effect) -> Unit) {
        activity.showAlertEditDialog("Effect name:") { name ->
            val effect = controller.createEffect(name)
            action(effect)
        }
    }

    override fun showCloseSessionDialog(name: String) {
        AlertDialog.Builder(activity)
            .setTitle("Close this session?")
            .setMessage(name)
            .setPositiveButton("OK", DialogInterface.OnClickListener { dialog, which ->
                controller.closeSession()
            })
            .setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, which ->
                dialog.dismiss()
            })
            .show()
    }
}