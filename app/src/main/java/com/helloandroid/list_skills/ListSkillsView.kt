package com.helloandroid.list_skills

import android.content.DialogInterface
import android.support.v7.app.AlertDialog
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import com.helloandroid.MainActivity
import com.helloandroid.room.Skill
import com.helloandroid.ui.RecyclerStringAdapter
import com.helloandroid.utils.showAlertDialog
import com.helloandroid.utils.showAlertEditDialog
import org.jetbrains.anko._FrameLayout
import org.jetbrains.anko.matchParent
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