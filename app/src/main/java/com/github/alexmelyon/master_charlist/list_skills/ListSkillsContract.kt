package com.github.alexmelyon.master_charlist.list_skills

import android.view.ViewGroup
import com.github.alexmelyon.master_charlist.room.Skill

interface ListSkillsContract {

    interface View {
        fun createView(container: ViewGroup): android.view.View
        fun setData(items: MutableList<Skill>)
        fun archivedAt(pos: Int)
        fun showAddSkillDialog()
        fun addedAt(pos: Int, skill: Skill)
        fun itemChangedAt(pos: Int)
    }

    interface Controller {
        fun archiveSkill(pos: Int, skill: Skill)
        fun createSkill(skillName: String)
        fun renameSkill(pos: Int, skill: Skill, name: String)
    }
}
