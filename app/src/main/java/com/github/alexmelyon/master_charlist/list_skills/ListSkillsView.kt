package com.github.alexmelyon.master_charlist.list_skills

import android.content.DialogInterface
import android.support.v7.app.AlertDialog
import android.view.View
import android.view.ViewGroup
import com.github.alexmelyon.master_charlist.MainActivity
import com.github.alexmelyon.master_charlist.room.Skill
import com.github.alexmelyon.master_charlist.ui.RecyclerStringAdapter
import com.github.alexmelyon.master_charlist.utils.showAlertDialog
import com.github.alexmelyon.master_charlist.utils.showAlertEditDialog
import org.jetbrains.anko._FrameLayout
import org.jetbrains.anko.recyclerview.v7.recyclerView
import javax.inject.Inject

class ListSkillsView @Inject constructor(val activity: MainActivity) : _FrameLayout(activity), ListSkillsContract.View {

    @Inject
    lateinit var controller: ListSkillsContract.Controller

    private lateinit var skillsAdapter: RecyclerStringAdapter<Skill>

    override fun createView(container: ViewGroup): View {
        skillsAdapter = RecyclerStringAdapter(container.context)
        skillsAdapter.onItemLongclickListener = { pos, skill ->
            AlertDialog.Builder(activity)
                .setItems(arrayOf("Rename", "Archive"), DialogInterface.OnClickListener { dialog, which ->
                    when(which) {
                        0 -> activity.showAlertEditDialog("Rename skill:", skill.name) { name ->
                            controller.renameSkill(pos, skill, name)
                        }
                        1 -> activity.showAlertDialog("Archive skill?", skill.name) {
                            controller.archiveSkill(pos, skill)
                        }
                    }
                }).show()
        }

        return container.context.recyclerView {
            adapter = skillsAdapter
        }
    }

    override fun setData(items: MutableList<Skill>) {
        skillsAdapter.items = items
    }

    override fun archivedAt(pos: Int) {
        skillsAdapter.itemRemovedAt(pos)
    }

    override fun showAddSkillDialog() {
        activity.showAlertEditDialog("Skill name:") { name ->
            controller.createSkill(name)
        }
    }

    override fun addedAt(pos: Int, skill: Skill) {
        skillsAdapter.itemAddedAt(pos, skill)
    }

    override fun itemChangedAt(pos: Int) {
        skillsAdapter.notifyItemChanged(pos)
    }
}