package com.github.alexmelyon.master_charlist.list_worlds

import android.content.DialogInterface
import android.text.method.LinkMovementMethod
import android.text.util.Linkify
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.github.alexmelyon.master_charlist.MainActivity
import com.github.alexmelyon.master_charlist.R
import com.github.alexmelyon.master_charlist.room.World
import com.github.alexmelyon.master_charlist.ui.RecyclerStringAdapter
import com.github.alexmelyon.master_charlist.utils.showAlertDialog
import com.github.alexmelyon.master_charlist.utils.showAlertEditDialog
import org.jetbrains.anko._FrameLayout
import org.jetbrains.anko.matchParent
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
            AlertDialog.Builder(activity)
                .setItems(arrayOf(context.getString(R.string.rename), context.getString(R.string.archive)), DialogInterface.OnClickListener { dialog, which ->
                    when(which) {
                        0 -> activity.showAlertEditDialog(context.getString(R.string.rename_world_colon), world.name) { name ->
                            controller.renameWorld(pos, world, name)
                        }
                        1 -> activity.showAlertDialog(context.getString(R.string.archive_world_question), world.name) {
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
        activity.showAlertEditDialog(context.getString(R.string.world_name_colon)) { name ->
            controller.createWorld(name)
        }
    }

    override fun showAboutDialog() {
        val dialog = AlertDialog.Builder(activity)
            .setTitle(context.getString(R.string.app_name))
            .setMessage((context.getString(R.string.about_app, activity.packageManager.getPackageInfo(activity.packageName, 0).versionName)).trimMargin())
            .setPositiveButton("OK", DialogInterface.OnClickListener { dialog, which ->
                dialog.dismiss()
            })
            .create()

        dialog.show()
        dialog.findViewById<TextView>(android.R.id.message)?.let { message ->
            Linkify.addLinks(message, Linkify.WEB_URLS)
            message.movementMethod = LinkMovementMethod.getInstance()
        }
    }
}