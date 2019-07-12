package com.github.alexmelyon.master_charlist.list_things

import android.view.ViewGroup
import com.github.alexmelyon.master_charlist.room.Thing

interface ListThingsContract {
    interface View {
        fun createView(container: ViewGroup): android.view.View
        fun setData(items: MutableList<Thing>)
        fun archivedAt(pos: Int)
        fun showAddThingDialog()
        fun addedAt(pos: Int, thing: Thing)
        fun itemChangedAt(pos: Int)
    }

    interface Controller {
        fun archiveThing(pos: Int, thing: Thing)
        fun createThing(thingName: String)
        fun renameThing(pos: Int, thing: Thing, name: String)
    }
}
