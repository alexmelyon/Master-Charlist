package com.github.alexmelyon.master_charlist.list_sessions

import android.view.ViewGroup
import com.github.alexmelyon.master_charlist.room.GameSession

interface ListSessionsContract {
    interface View {
        fun createView(container: ViewGroup): android.view.View
        fun setData(items: MutableList<GameSession>)
        fun addedAt(pos: Int, session: GameSession)
        fun archivedAt(pos: Int)
        fun itemChangedAt(pos: Int)
    }

    interface Controller {
        fun onItemClick(session: GameSession)
        fun getGameName(): String
        fun createSession()
        fun archiveSession(pos: Int, session: GameSession)
        fun getDescription(pos: Int): String
        fun getHeaderStringRes(pos: Int): Int
        fun renameSession(pos: Int, session: GameSession, name: String)
    }
}