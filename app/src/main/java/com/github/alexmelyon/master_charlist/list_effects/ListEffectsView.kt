package com.github.alexmelyon.master_charlist.list_effects

import android.content.DialogInterface
import android.support.v7.app.AlertDialog
import android.view.View
import android.view.ViewGroup
import com.github.alexmelyon.master_charlist.MainActivity
import com.github.alexmelyon.master_charlist.R
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
            val skillNames = listOf(context.getString(R.string.create_new)) + skillsForEffect.map { context.getString(R.string.attach_something, it.name) }
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
            val usual = listOf(context.getString(R.string.rename), context.getString(R.string.archive_effect))
            val variants = usual + usedSkills.map { context.getString(R.string.detach_something, it.first) }
            AlertDialog.Builder(activity)
                .setItems(variants.toTypedArray(), DialogInterface.OnClickListener { dialog, which ->
                    if(which == 0) {
                        activity.showAlertEditDialog(context.getString(R.string.rename_effect_colon), row.effect.name) { name ->
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
        activity.showAlertEditDialog(context.getString(R.string.skill_name_colon)) { name ->
            val skill = controller.createSkill(name)
            action(skill)
        }
    }

    fun confirmArchiveEffect(name: String, action: () -> Unit) {
        AlertDialog.Builder(activity)
            .setTitle(context.getString(R.string.archive_effect_question))
            .setMessage(name)
            .setPositiveButton("OK", DialogInterface.OnClickListener { dialog, which ->
                action()
            })
            .setNegativeButton(context.getString(R.string.cancel), DialogInterface.OnClickListener { dialog, which ->
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
        activity.showAlertEditDialog(context.getString(R.string.effect_name_colon)) { name ->
            controller.createEffect(name)
        }
    }

}