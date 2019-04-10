package com.helloandroid.list_effects

import android.content.DialogInterface
import android.support.v7.app.AlertDialog
import android.view.View
import android.view.ViewGroup
import com.helloandroid.MainActivity
import com.helloandroid.room.Effect
import com.helloandroid.ui.RecyclerStringAdapter
import com.helloandroid.utils.showAlertEditDialog
import org.jetbrains.anko._FrameLayout
import org.jetbrains.anko.recyclerview.v7.recyclerView
import javax.inject.Inject

class ListEffectsView @Inject constructor(val activity: MainActivity) : _FrameLayout(activity), ListEffectsContract.View {

    @Inject
    lateinit var controller: ListEffectsContract.Controller

    private lateinit var effectsAdapter: RecyclerStringAdapter<Effect>

    override fun createView(container: ViewGroup): View {
        effectsAdapter = RecyclerStringAdapter(container.context)
        effectsAdapter.onItemLongclickListener = { pos, effect ->
            AlertDialog.Builder(activity)
                .setTitle("Archive effect?")
                .setMessage(effect.name)
                .setPositiveButton("OK", DialogInterface.OnClickListener { dialog, which ->
                    controller.archiveEffect(pos, effect)
                })
                .setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, which ->
                    dialog.dismiss()
                })
                .show()
        }
        return container.context.recyclerView {
            adapter = effectsAdapter
        }
    }

    override fun setData(items: MutableList<Effect>) {
        effectsAdapter.items = items
    }

    override fun addedAt(pos: Int, effect: Effect) {
        effectsAdapter.itemAddedAt(pos, effect)
    }

    override fun archivedAt(pos: Int) {
        effectsAdapter.itemRemovedAt(pos)
    }

    override fun showAddEffectDialog() {
        activity.showAlertEditDialog("Effect name:") { name ->
            controller.createEffect(name)
        }
    }

}