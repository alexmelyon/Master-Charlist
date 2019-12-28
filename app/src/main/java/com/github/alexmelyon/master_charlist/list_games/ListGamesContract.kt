package com.github.alexmelyon.master_charlist.list_games

import android.view.ViewGroup
import com.github.alexmelyon.master_charlist.room.Game

interface ListGamesContract {
    interface View {
        fun createView(container: ViewGroup): android.view.View
        fun setData(items: MutableList<Game>)
        fun showAddGameDialog()
        fun addedAt(pos: Int, game: Game)
        fun archivedAt(pos: Int)
        fun itemChangedAt(pos: Int)
    }

    interface Controller {
        fun onItemClick(game: Game)
        fun getWorldName(): String
        fun createGame(name: String)
        fun archiveGameAt(pos: Int)
        fun renameGame(pos: Int, game: Game, name: String)
    }
}