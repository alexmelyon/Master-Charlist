package com.github.alexmelyon.master_charlist.list_effects

import android.content.DialogInterface
import android.support.v7.app.AlertDialog
import android.view.View
import android.view.ViewGroup
import com.github.alexmelyon.master_charlist.MainActivity
import com.github.alexmelyon.master_charlist.room.Skill
import com.github.alexmelyon.master_charlist.utils.showAlertEditDialog
import org.jetbrains.anko._FrameLayout
import org.jetbrains.anko.recyclerview.v7.recyclerView
import javax.inject.Inject

class ListEffectsView @Inject constructor(val activity: MainActivity) : _FrameLayout(activity), ListEffectsContract.View {

    @Inject
    lateinit var controller: ListEffectsContract.Controller

    private lateinit var effectsAdapter: ListEffectsAdapter

    override fun createView(container: ViewGroup): View {
        effectsAdapter = ListEffectsAdapter()
        effectsAdapter.onItemClickListener = { pos, row ->
            val skillsForEffect = controller.getAvailableSkillsForEffect(row.effect)
            val skillNames = listOf("Create new...") + skillsForEffect.map { "Attach ${it.name}" }
            AlertDialog.Builder(activity)
                .setItems(skillNames.toTypedArray(), DialogInterface.OnClickListener { dialog, which ->
                    if(which == 0) {
                        showCreateSkillDialog { skill ->
                            controller.attachSkillForEffect(pos, row.effect, skill)
                        }
                    } else {
                        val skill = skillsForEffect[which - 1]
                        controller.attachSkillForEffect(pos, row.effect, skill)
                    }
                })
                .show()
        }
        effectsAdapter.onItemLongclickListener = { pos, row ->
            val usedSkills = controller.getUsedEffectSkills(row.effect)
            val usual = listOf("Rename", "Archive effect")
            val variants = usual + usedSkills.map { "Detach ${it.first}" }
            AlertDialog.Builder(activity)
                .setItems(variants.toTypedArray(), DialogInterface.OnClickListener { dialog, which ->
                    if(which == 0) {
                        activity.showAlertEditDialog("Rename effect:", row.effect.name) { name ->
                            controller.renameEffect(pos, row.effect, name)
                        }
                    } else if(which == 1) {
                        confirmArchiveEffect(row.effect.name) {
                            controller.archiveEffect(pos, row.effect)
                        }
                    } else {
                        val effectSkill = usedSkills[which - usual.size]
                        controller.detachSkillForEffect(pos, row.effect, effectSkill.second)
                    }
                })
                .show()
        }
        effectsAdapter.onSubitemPlus = { pos, effect, skill ->
            controller.onEffectSkillChanged(pos, effect, skill, +1)
        }
        effectsAdapter.onSubitemMinus = { pos, effect, skill ->
            controller.onEffectSkillChanged(pos, effect, skill, -1)
        }

        return container.context.recyclerView {
            adapter = effectsAdapter
        }
    }

    fun showCreateSkillDialog(action: (Skill) -> Unit) {
        activity.showAlertEditDialog("Skill name:") { name ->
            val skill = controller.createSkill(name)
            action(skill)
        }
    }

    fun confirmArchiveEffect(name: String, action: () -> Unit) {
        AlertDialog.Builder(activity)
            .setTitle("Archive effect?")
            .setMessage(name)
            .setPositiveButton("OK", DialogInterface.OnClickListener { dialog, which ->
                action()
            })
            .setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, which ->
                dialog.dismiss()
            })
            .show()
    }

    override fun setData(items: MutableList<EffectRow>) {
        effectsAdapter.items = items
    }

    override fun itemAddedAt(pos: Int, effect: EffectRow) {
        effectsAdapter.items.add(pos, effect)
        effectsAdapter.notifyItemInserted(pos)
    }

    override fun itemArchivedAt(pos: Int) {
        effectsAdapter.items.removeAt(pos)
        effectsAdapter.notifyItemRemoved(pos)
    }

    override fun itemChangedAt(pos: Int) {
        effectsAdapter.notifyItemChanged(pos)
    }

    override fun showAddEffectDialog() {
        activity.showAlertEditDialog("Effect name:") { name ->
            controller.createEffect(name)
        }
    }

}