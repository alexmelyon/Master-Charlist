package com.github.alexmelyon.master_charlist.list_characters

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import com.bluelinelabs.conductor.Controller
import com.crashlytics.android.Crashlytics
import com.github.alexmelyon.master_charlist.R
import com.github.alexmelyon.master_charlist.list_games.WORLD_KEY
import com.github.alexmelyon.master_charlist.list_sessions.GAME_KEY
import com.github.alexmelyon.master_charlist.room.*
import ru.napoleonit.talan.di.ControllerInjector
import java.util.*
import javax.inject.Inject

interface ListCharactersDelegate {
    fun updateCharactersScreen()
}

class ListCharactersController(args: Bundle) : Controller(args), ListCharactersContract.Controller, ListCharactersDelegate {

    data class SkillValueModifier(val skill: Skill, val value: Int, val modifier: Int)

    @Inject
    lateinit var view: ListCharactersContract.View
    @Inject
    lateinit var db: AppDatabase

    lateinit var world: World
    lateinit var game: Game
    private val characterItems = TreeSet(Comparator<CharacterItem> { o1, o2 ->
        if (o1.lastUsed != o2.lastUsed) {
            return@Comparator o1.lastUsed.compareTo(o2.lastUsed)
        }
        return@Comparator o1.character.name.compareTo(o2.character.name)
    })

    constructor(world: World, game: Game) : this(Bundle().apply {
        putParcelable(WORLD_KEY, world)
        putParcelable(GAME_KEY, game)
    })

    override fun onContextAvailable(context: Context) {
        super.onContextAvailable(context)
        ControllerInjector.inject(this)
        world = args.getParcelable<World>(WORLD_KEY)!!
        game = args.getParcelable<Game>(GAME_KEY)!!
        updateScreen()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View {
        Crashlytics.log(Log.INFO, javaClass.simpleName, "onCreateView")
        return view.createView(container)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.list_characters, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_add_character -> {
                view.showAddCharacterDialog()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun updateCharactersScreen() {
        updateScreen()
    }

    fun updateScreen() {
        characterItems.clear()
        val characters = db.characterDao().getAll(world.id, game.id)
        val closedSessions = db.gameSessionDao().getAll(world.id, game.id)
            .filterNot { it.open }
            .map { it.id }
        characters.forEach { character ->
            val hp = db.hpDiffDao().getAllByCharacter(world.id, game.id, character.id)
                .filter { closedSessions.contains(it.sessionGroup) }
                .sumBy { it.value }

            val effects = db.effectDao().getAll(world.id)
            val closedEffectDiffs = db.effectDiffDao().getAllByCharacter(world.id, game.id, character.id)
                .filter { it.sessionGroup in closedSessions }
            val effectDiffs = getUsedEffectsFor(closedEffectDiffs, effects)
            val effectDiffNames = effectDiffs.map { it.name }

            val skillIdToModifier = db.effectDiffDao().getAllByCharacter(world.id, game.id, character.id)
                .filter { it.sessionGroup in closedSessions }
                .map { it.effectGroup to if(it.value) +1 else -1 }
                .groupBy { it.first }
                .map { it.key to it.value.sumBy { it.second } }
                .flatMap { (effectId, amount) ->
                    val effectSkills = db.effectSkillDao().getAllByEffect(world.firestoreId, effectId)
                        .map { db.skillDao().get(it.skillGroup) to it.value }
                    effectSkills.map { it.first to it.second * amount }
                }.groupBy { it.first }
                .map { it.key.id to it.value.sumBy { it.second } }
                .toMap()

            val skills = db.skillDao().getAll(world.firestoreId)
            data class SkillToValue(val skill: Skill, val value: Int)
            val skillDiffs = db.skillDiffDao().getAllByCharacter(world.id, game.id, character.id)
                .asSequence()
                .filter { closedSessions.contains(it.sessionGroup) }
                .map { skill -> SkillToValue(skills.single { it.id == skill.skillGroup },skill.value) }
                .toMutableList()
                .apply {
                    val existingSkillIds = this.map { it.skill.id }
                    val missedSkills = skillIdToModifier.filter { it.key !in existingSkillIds }
                    addAll(missedSkills.map { missed -> SkillToValue(skills.single { it.id == missed.key }, 0) })
                }
                .groupBy { it.skill }
                .map { SkillToValue(it.key, it.value.sumBy { it.value }) }
                .map { SkillValueModifier(it.skill, it.value, skillIdToModifier[it.skill.id] ?: 0) }
                .filter { it.value != 0 || it.modifier != 0 }
                .toList()
            val skillDiffNames = skillDiffs.map {
                if(it.modifier == 0) {
                    "%s: %d".format(it.skill.name, it.value)
                } else {
                    "%s: %d (%+d) %d".format(
                        it.skill.name,
                        it.value,
                        it.modifier,
                        it.value + it.modifier
                    )
                }
            }

            val things = db.thingDao().getAll(world.id)
            // TODO Refactor this boilerplate
            val thingDiffs = db.thingDiffDao().getAllByCharacter(world.id, game.id, character.id)
                .asSequence()
                .filter { closedSessions.contains(it.sessionGroup) }
                .map { thing -> things.single { it.id == thing.thingGroup } to thing.value }
                .groupBy { it.first }
                .map { it.key to it.value.sumBy { it.second } }
                .filter { it.second != 0 }
                .toList()
            val thingDiffNames = thingDiffs.map { it.first.name to it.second }

            val lastUsed = (skillDiffs.map { it.skill.lastUsed } + thingDiffs.map { it.first.lastUsed })
                .min() ?: Calendar.getInstance().time
            characterItems.add(CharacterItem(character, hp, lastUsed, effectDiffNames, skillDiffNames, thingDiffNames))
            characterItems.forEachIndexed { index, item ->
                item.index = index
            }
        }
        if (isAttached) {
            this.view.setData(characterItems.toMutableList())
        }
    }

    override fun onAttach(view: View) {
        super.onAttach(view)
        this.view.setData(characterItems.toMutableList())
    }

    override fun createCharacter(characterName: String) {
        val character = GameCharacter(characterName, game.id, world.id)
        val id = db.characterDao().insert(character)
        character.id = id

        val item = CharacterItem(character, 0, Calendar.getInstance().time, listOf(), listOf(), listOf())
        characterItems.add(item)
        characterItems.forEachIndexed { index, item ->
            item.index = index
        }
        view.addedAt(item.index, item)
    }

    override fun archiveCharacter(pos: Int, item: CharacterItem) {
        val character = db.characterDao().get(world.id, game.id, item.character.id)
        character.archived = true
        db.characterDao().update(character)

        characterItems.remove(item)
        characterItems.forEachIndexed { index, item ->
            item.index = index
        }
        view.archiveddAt(pos)
    }

    override fun renameCharacter(pos: Int, character: GameCharacter, name: String) {
        character.name = name
        db.characterDao().update(character)
        view.itemChangedAt(pos)
    }
}