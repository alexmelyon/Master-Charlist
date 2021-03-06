package com.github.alexmelyon.master_charlist.list_sessions

import android.content.DialogInterface
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import com.github.alexmelyon.master_charlist.MainActivity
import com.github.alexmelyon.master_charlist.R
import com.github.alexmelyon.master_charlist.room.GameSession
import com.github.alexmelyon.master_charlist.ui.RecyclerStringAdapter
import com.github.alexmelyon.master_charlist.utils.showAlertDialog
import com.github.alexmelyon.master_charlist.utils.showAlertEditDialog
import org.jetbrains.anko._FrameLayout
import org.jetbrains.anko.recyclerview.v7.recyclerView
import javax.inject.Inject

class ListSessionsView @Inject constructor(val activity: MainActivity) : _FrameLayout(activity), ListSessionsContract.View {

    @Inject
    lateinit var controller: ListSessionsContract.Controller

    lateinit var sessionsAdapter: RecyclerStringAdapter<GameSession>

    override fun createView(container: ViewGroup): View {
        activity.supportActionBar!!.title = controller.getGameName()
        sessionsAdapter = RecyclerStringAdapter(container.context, R.layout.list_sessions_item_with_header) { pos, session ->
            controller.onItemClick(session)
        }
        sessionsAdapter.onGetHeaderValue = { pos ->
            controller.getHeaderStringRes(pos)
        }
        sessionsAdapter.onItemLongclickListener = { pos, session ->
            AlertDialog.Builder(activity)
                .setItems(arrayOf(context.getString(R.string.rename), context.getString(R.string.archive)), DialogInterface.OnClickListener { dialog, which ->
                    when(which) {
                        0 -> activity.showAlertEditDialog(context.getString(R.string.rename_session_colon), session.name) { name ->
                            controller.renameSession(pos, session, name)
                        }
                        1 -> activity.showAlertDialog(context.getString(R.string.archive_session_question), session.name) {
                            controller.archiveSession(pos, session)
                        }
                    }
                }).show()
        }
        return container.context.recyclerView {
            adapter = sessionsAdapter
        }
    }

    override fun setData(items: MutableList<GameSession>) {
        sessionsAdapter.items = items
    }

    override fun addedAt(pos: Int, session: GameSession) {
        sessionsAdapter.itemAddedAt(pos, session)
    }

    override fun archivedAt(pos: Int) {
        sessionsAdapter.itemRemovedAt(pos)
    }

    override fun itemChangedAt(pos: Int) {
        sessionsAdapter.notifyItemChanged(pos)
    }
}