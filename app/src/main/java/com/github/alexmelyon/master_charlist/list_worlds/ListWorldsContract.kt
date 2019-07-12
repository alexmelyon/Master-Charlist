package com.github.alexmelyon.master_charlist.list_worlds

import android.view.ViewGroup
import com.github.alexmelyon.master_charlist.room.World

interface ListWorldsContract {

    interface Controller {
        fun onItemClick(world: World)
        fun createWorld(worldName: String)
        fun archiveWorldAt(pos: Int)
        fun renameWorld(pos: Int, world: World, name: String)
    }

    interface View {
        fun createView(container: ViewGroup): android.view.View
        fun setData(items: MutableList<World>)
        fun showCreateWorldDialog()
        fun addedAt(i: Int, world: World)
        fun archivedAt(pos: Int)
        fun showAboutDialog()
        fun itemChangedAt(pos: Int)
    }
}