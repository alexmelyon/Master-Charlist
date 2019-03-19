package com.helloandroid.list_characters

import android.content.Context
import android.os.Bundle
import android.view.*
import com.bluelinelabs.conductor.Controller
import com.helloandroid.App
import com.helloandroid.Character
import com.helloandroid.R
import com.helloandroid.list_games.WORLD_KEY
import com.helloandroid.list_sessions.GAME_KEY
import ru.napoleonit.talan.di.ControllerInjector
import java.util.*
import javax.inject.Inject

class ListCharactersController(args: Bundle) : Controller(args), ListCharactersContract.Controller {

    @Inject
    lateinit var view: ListCharactersContract.View

    private lateinit var characterItems: TreeSet<CharacterItem>
    val world = App.instance.worlds.first { it.id == args.getInt(WORLD_KEY) }
    val game = App.instance.games.first { it.id == args.getInt(GAME_KEY) && it.worldGroup == world.id }

    constructor(worldId: Int, gameId: Int) : this(Bundle().apply {
        putInt(WORLD_KEY, worldId)
        putInt(GAME_KEY, gameId)
    })

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View {
        setHasOptionsMenu(true)
        return view.createView(container)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.character_add, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.add_character -> view.showAddCharacterDialog()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onContextAvailable(context: Context) {
        super.onContextAvailable(context)
        ControllerInjector.inject(this)

        characterItems = TreeSet(Comparator { o1, o2 ->
            return@Comparator o1.name.compareTo(o2.name)
        })
        val characters = App.instance.characters.filter { it.gameGroup == game.id && it.worldGroup == world.id && !it.archived }
        val sessionsIds = App.instance.gameSessions.filter { it.gameGroup == game.id && it.worldGroup == world.id && it.closed }.map { it.id }
        characters.forEach { character ->
            val hp = sessionsIds.fold(0) { total, next ->
                App.instance.hpDiffs.filter { it.characterGroup == character.id && sessionsIds.contains(it.sessionGroup) && it.gameGroup == game.id && it.worldGroup == world.id }
                    .sumBy { it.value }
            }
            val skills = App.instance.skills.filter { it.worldGroup == world.id }
            val skillsDiffs = App.instance.skillDiffs.filter { it.characterGroup == character.id && sessionsIds.contains(it.sessionGroup) && it.gameGroup == game.id && it.worldGroup == world.id }
                .fold(listOf<Pair<Int, Int>>()) { total, next ->
                    total + listOf(Pair(next.skillGroup, next.value))
                }.map { skillTovalue -> skills.single { it.id == skillTovalue.first }.name to skillTovalue.second }
                .groupBy { it.first }
                .map { it.key to it.value.sumBy { it.second } }
            val things = App.instance.things.filter { it.worldGroup == world.id }
            // TODO Refactor this boilerplate
            val thingDiffs = App.instance.thingDiffs.filter { it.characterGroup == character.id && sessionsIds.contains(it.sessionGroup) && it.gameGroup == game.id && it.worldGroup == world.id }
                .fold(listOf<Pair<Int, Int>>()) { total, next ->
                    total + listOf(Pair(next.thingGroup, next.value))
                }.map { skillTovalue -> things.single { it.id == skillTovalue.first }.name to skillTovalue.second }
                .groupBy { it.first }
                .map { it.key to it.value.sumBy { it.second } }
            characterItems.add(CharacterItem(character.id, character.name, hp, skillsDiffs, thingDiffs))
            characterItems.forEachIndexed { index, item ->
                item.index = index
            }
        }
    }

    override fun onAttach(view: View) {
        super.onAttach(view)
        this.view.setData(characterItems.toMutableList())
    }

    override fun createCharacter(characterName: String) {
        val maxId = App.instance.characters.filter { it.gameGroup == game.id && it.worldGroup == world.id }
            .maxBy { it.id }?.id ?: -1
        val character = Character(maxId + 1, characterName, game.id, world.id, archived = false)
        App.instance.characters.add(character)

        val item = CharacterItem(character.id, characterName, 0, listOf(), listOf())
        characterItems.add(item)
        characterItems.forEachIndexed { index, item ->
            item.index = index
        }
        view.addedAt(item.index, item)
    }

    override fun archiveCharacter(pos: Int, item: CharacterItem) {
        val character = App.instance.characters.filter { it.gameGroup == game.id && it.worldGroup == world.id }
            .single { it.id == item.id }
        character.archived = true

        characterItems.remove(item)
        characterItems.forEachIndexed { index, item ->
            item.index = index
        }
        view.removedAt(pos)
    }
}