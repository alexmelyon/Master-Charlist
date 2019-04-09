package com.helloandroid.list_effects

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bluelinelabs.conductor.Controller
import com.helloandroid.list_games.WORLD_KEY
import com.helloandroid.list_sessions.GAME_KEY
import org.jetbrains.anko.linearLayout

class ListEffectsController(args: Bundle) : Controller(args) {

    constructor(worldId: Long, gameId: Long) : this(Bundle().apply {
        putLong(WORLD_KEY, worldId)
        putLong(GAME_KEY, gameId)
    })

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View {
        return container.context.linearLayout {

        }
    }
}