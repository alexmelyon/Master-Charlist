package com.github.alexmelyon.master_charlist.list_things

import android.content.DialogInterface
import android.support.v7.app.AlertDialog
import android.view.View
import android.view.ViewGroup
import com.github.alexmelyon.master_charlist.MainActivity
import com.github.alexmelyon.master_charlist.R
import com.github.alexmelyon.master_charlist.room.Thing
import com.github.alexmelyon.master_charlist.ui.RecyclerStringAdapter
import com.github.alexmelyon.master_charlist.utils.showAlertDialog
import com.github.alexmelyon.master_charlist.utils.showAlertEditDialog
import org.jetbrains.anko._FrameLayout
import org.jetbrains.anko.recyclerview.v7.recyclerView
import javax.inject.Inject

class ListThingsView @Inject constructor(val activity: MainActivity) : _FrameLayout(activity), ListThingsContract.View {

    @Inject
    lateinit var controller: ListThingsContract.Controller

    private lateinit var thingsAdapter: RecyclerStringAdapter<Thing>

    override fun createView(container: ViewGroup): View {
        thingsAdapter = RecyclerStringAdapter(container.context)
        thingsAdapter.onItemLongclickListener = { pos, thing ->
            AlertDialog.Builder(activity)
                .setItems(arrayOf(context.getString(R.string.rename), context.getString(R.string.archive)), DialogInterface.OnClickListener { dialog, which ->
                    when(which) {
                        0 -> activity.showAlertEditDialog(context.getString(R.string.rename_thing_colon), thing.name) { name ->
                            controller.renameThing(pos, thing, name)
                        }
                        1 -> activity.showAlertDialog(context.getString(R.string.archive_thing_question), thing.name) {
                            controller.archiveThing(pos, thing)
                        }
                    }
                }).show()
        }

        return container.context.recyclerView {
            adapter = thingsAdapter
        }
    }

    override fun setData(items: MutableList<Thing>) {
        thingsAdapter.items = items
    }

    override fun archivedAt(pos: Int) {
        thingsAdapter.itemRemovedAt(pos)
    }

    override fun showAddThingDialog() {
        activity.showAlertEditDialog(context.getString(R.string.thing_name_colon)) { name ->
            controller.createThing(name)
        }
    }

    override fun addedAt(pos: Int, thing: Thing) {
        thingsAdapter.itemAddedAt(pos, thing)
    }

    override fun itemChangedAt(pos: Int) {
        thingsAdapter.notifyItemChanged(pos)
    }
}