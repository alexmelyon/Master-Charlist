package com.helloandroid.list_worlds

import android.content.DialogInterface
import android.support.v7.app.AlertDialog
import android.support.v7.widget.RecyclerView
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.util.Linkify
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import com.helloandroid.App
import com.helloandroid.MainActivity
import com.helloandroid.R
import com.helloandroid.room.World
import com.helloandroid.ui.RecyclerStringAdapter
import com.helloandroid.utils.showAlertEditDialog
import org.jetbrains.anko._FrameLayout
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.padding
import org.jetbrains.anko.recyclerview.v7.recyclerView
import org.jetbrains.anko.verticalLayout
import javax.inject.Inject

class ListWorldsView @Inject constructor(val activity: MainActivity) : _FrameLayout(activity), ListWorldsContract.View {

    @Inject
    lateinit var controller: ListWorldsContract.Controller

    lateinit var worldsView: RecyclerView
    lateinit var worldsAdapter: RecyclerStringAdapter<World>

    override fun createView(container: ViewGroup) = container.context.verticalLayout {
        activity.supportActionBar!!.title = container.context.getString(R.string.app_name)
        worldsAdapter = RecyclerStringAdapter(container.context) { pos, world ->
            controller.onItemClick(world)
        }
        worldsAdapter.onItemLongclickListener = { pos, world ->
            // TODO Rename
            AlertDialog.Builder(activity)
                .setItems(arrayOf("Rename", "Archive"), DialogInterface.OnClickListener { dialog, which ->
                    when(which) {
                        0 -> activity.showAlertEditDialog("Rename world:", world.name) { name ->
                            controller.renameWorld(pos, world, name)
                        }
                        1 -> activity.showAlertEditDialog("Archive world?", world.name) {
                            controller.archiveWorldAt(pos)
                        }
                    }
                }).show()
        }
        worldsView = recyclerView {
            adapter = worldsAdapter
        }.lparams(matchParent, matchParent)
    }

    override fun setData(items: MutableList<World>) {
        worldsAdapter.items = items
    }

    override fun addedAt(i: Int, world: World) {
        worldsAdapter.itemAddedAt(i, world)
    }

    override fun archivedAt(pos: Int) {
        worldsAdapter.itemRemovedAt(pos)
    }

    override fun itemChangedAt(pos: Int) {
        worldsAdapter.notifyItemChanged(pos)
    }

    override fun showCreateWorldDialog() {
        activity.showAlertEditDialog("World name:") { name ->
            controller.createWorld(name)
        }
    }

    override fun showAboutDialog() {
        val dialog = AlertDialog.Builder(activity)
            .setTitle("Master Charlist")
            .setMessage("""
            |Sketchpad for game masters
            |Version ${activity.packageManager.getPackageInfo(activity.packageName, 0).versionName}
            |github.com/alexmelyon/Master-Charlist""".trimMargin())
            .setPositiveButton("OK", DialogInterface.OnClickListener { dialog, which ->
                dialog.dismiss()
            })
            .create()

        dialog.show()
        val message = dialog.findViewById<TextView>(android.R.id.message)
        Linkify.addLinks(message, Linkify.WEB_URLS)
        message?.movementMethod = LinkMovementMethod.getInstance()
    }
}