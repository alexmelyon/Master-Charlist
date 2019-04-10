package com.helloandroid.list_effects

import android.view.ViewGroup
import com.helloandroid.room.Effect

interface ListEffectsContract {
    interface View {
        fun createView(container: ViewGroup): android.view.View
        fun setData(items: MutableList<Effect>)
        fun archivedAt(pos: Int)
        fun showAddEffectDialog()
        fun addedAt(pos: Int, effect: Effect)
    }

    interface Controller {
        fun createEffect(effectName: String)
        fun archiveEffect(pos: Int, effect: Effect)
    }
}