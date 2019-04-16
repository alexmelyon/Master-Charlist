package com.helloandroid.list_games

import android.content.DialogInterface
import android.support.v7.app.AlertDialog
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import android.widget.EditText
import com.helloandroid.MainActivity
import com.helloandroid.room.Game
import com.helloandroid.ui.RecyclerStringAdapter
import com.helloandroid.utils.showAlertDialog
import com.helloandroid.utils.showAlertEditDialog
import org.jetbrains.anko._FrameLayout
import org.jetbrains.anko.linearLayout
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.recyclerview.v7.recyclerView
import javax.inject.Inject

class ListGamesView @Inject constructor(val activity: MainActivity) : _FrameLayout(activity), ListGamesContract.View {

    @Inject
    lateinit var controller: ListGamesContract.Controller

    lateinit var gamesView: RecyclerView
    lateinit var gamesAdapter: RecyclerStringAdapter<Game>

    override fun createView(container: ViewGroup) = container.context.linearLayout {
        gamesAdapter = RecyclerStringAdapter(container.context) { pos, game ->
            controller.onItemClick(game)
        }
        gamesAdapter.onItemLongclickListener = { pos, game ->
            AlertDialog.Builder(activity)
                .setItems(arrayOf("Rename", "Archive"), DialogInterface.OnClickListener { dialog, which ->
                    when(which) {
                        0 -> activity.showAlertEditDialog("Rename game:", game.name) { name ->
                            controller.renameGame(pos, game, name)
                        }
                        1 -> activity.showAlertDialog("Archive game?", game.name) {
                            controller.archiveGameAt(pos)
                        }
                    }
                }).show()
        }
        gamesView = recyclerView {
            adapter = gamesAdapter
        }.lparams(matchParent, matchParent)
    }

    override fun setData(items: MutableList<Game>) {
        gamesAdapter.items = items
    }

    override fun showAddGameDialog() {
        activity.showAlertEditDialog("Game name:") { name ->
            controller.createGame(name)
        }
    }

    override fun addedAt(pos: Int, game: Game) {
        gamesAdapter.itemAddedAt(pos, game)
    }

    override fun archivedAt(pos: Int) {
        gamesAdapter.itemRemovedAt(pos)
    }

    override fun itemChangedAt(pos: Int) {
        gamesAdapter.notifyItemChanged(pos)
    }
}