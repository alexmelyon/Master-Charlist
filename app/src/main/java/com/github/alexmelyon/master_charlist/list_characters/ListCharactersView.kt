package com.github.alexmelyon.master_charlist.list_characters

import android.content.DialogInterface
import android.support.v7.app.AlertDialog
import android.view.View
import android.view.ViewGroup
import com.github.alexmelyon.master_charlist.MainActivity
import com.github.alexmelyon.master_charlist.utils.showAlertDialog
import com.github.alexmelyon.master_charlist.utils.showAlertEditDialog
import org.jetbrains.anko._FrameLayout
import org.jetbrains.anko.recyclerview.v7.recyclerView
import javax.inject.Inject

class ListCharactersView @Inject constructor(val activity: MainActivity) : _FrameLayout(activity), ListCharactersContract.View {

    @Inject
    lateinit var controller: ListCharactersContract.Controller

    lateinit var charactersAdapter: ListCharactersAdapter

    override fun createView(container: ViewGroup): View {
        charactersAdapter = ListCharactersAdapter(activity) { pos, item ->
            AlertDialog.Builder(activity)
                .setItems(arrayOf("Rename", "Archive"), DialogInterface.OnClickListener { dialog, which ->
                    when(which) {
                        0 -> activity.showAlertEditDialog("Rename character:", item.character.name) { name ->
                            controller.renameCharacter(pos, item.character, name)
                        }
                        1 -> activity.showAlertDialog("Archive character?", item.character.name) {
                            controller.archiveCharacter(pos, item)
                        }
                    }
                }).show()
        }
        return container.context.recyclerView {
            adapter = charactersAdapter
        }
    }

    override fun setData(items: MutableList<CharacterItem>) {
        charactersAdapter.items = items
    }

    override fun showAddCharacterDialog() {
        activity.showAlertEditDialog("Character name:") { name ->
            controller.createCharacter(name)
        }
    }

    override fun addedAt(index: Int, item: CharacterItem) {
        charactersAdapter.adddedAt(index, item)
    }

    override fun archiveddAt(pos: Int) {
        charactersAdapter.removedAt(pos)
    }

    override fun itemChangedAt(pos: Int) {
        charactersAdapter.notifyItemChanged(pos)
    }
}