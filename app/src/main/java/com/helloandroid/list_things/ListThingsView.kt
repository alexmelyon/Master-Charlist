package com.helloandroid.list_things

import android.content.DialogInterface
import android.support.v7.app.AlertDialog
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import com.helloandroid.MainActivity
import com.helloandroid.room.Thing
import com.helloandroid.ui.RecyclerStringAdapter
import com.helloandroid.utils.showAlertDialog
import com.helloandroid.utils.showAlertEditDialog
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
                .setItems(arrayOf("Rename", "Archive"), DialogInterface.OnClickListener { dialog, which ->
                    when(which) {
                        0 -> activity.showAlertEditDialog("Rename thing:", thing.name) { name ->
                            controller.renameThing(pos, thing, name)
                        }
                        1 -> activity.showAlertDialog("Archive thing?", thing.name) {
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
        activity.showAlertEditDialog("Thing name:") { name ->
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