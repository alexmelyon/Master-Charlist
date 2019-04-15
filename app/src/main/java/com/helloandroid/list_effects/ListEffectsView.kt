package com.helloandroid.list_effects

import android.content.DialogInterface
import android.support.v7.app.AlertDialog
import android.view.View
import android.view.ViewGroup
import com.helloandroid.MainActivity
import com.helloandroid.utils.showAlertEditDialog
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
            val skillNames = skillsForEffect.map { "Attach ${it.name}" }.toTypedArray()
            AlertDialog.Builder(activity)
                .setItems(skillNames, DialogInterface.OnClickListener { dialog, which ->
                    val skill = skillsForEffect[which]
                    controller.attachSkillForEffect(pos, row.effect, skill)
                })
                .show()
        }
        effectsAdapter.onItemLongclickListener = { pos, row ->
            val usedSkills = controller.getUsedEffectSkills(row.effect)
            val variants = listOf("Archive effect") + usedSkills.map { "Detach ${it.first}" }
            AlertDialog.Builder(activity)
                .setItems(variants.toTypedArray(), DialogInterface.OnClickListener { dialog, which ->
                    if(which == 0) {
                        confirmArchiveEffect(row.effect.name) {
                            controller.archiveEffect(pos, row.effect)
                        }
                    } else {
                        val effectSkill = usedSkills[which - 1]
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