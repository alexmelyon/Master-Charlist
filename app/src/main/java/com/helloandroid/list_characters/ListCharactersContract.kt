package com.helloandroid.list_characters

import android.view.ViewGroup
import com.helloandroid.room.GameCharacter
import java.util.*

interface ListCharactersContract {

    interface View {
        fun createView(container: ViewGroup): android.view.View
        fun setData(items: MutableList<CharacterItem>)
        fun showAddCharacterDialog()
        fun addedAt(index: Int, item: CharacterItem)
        fun archiveddAt(pos: Int)
        fun itemChangedAt(pos: Int)
    }

    interface Controller {
        fun createCharacter(characterName: String)
        fun archiveCharacter(pos: Int, item: CharacterItem)
        fun renameCharacter(pos: Int, character: GameCharacter, name: String)
    }
}

class CharacterItem(val character: GameCharacter, val hp: Int, val lastUsed: Date, val effects: List<String>, val skills: List<String>, val things: List<Pair<String, Int>>, var index: Int = -1)
