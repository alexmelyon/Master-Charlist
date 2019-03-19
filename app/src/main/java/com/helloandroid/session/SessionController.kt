package com.helloandroid.session

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import com.bluelinelabs.conductor.Controller
import com.helloandroid.*
import com.helloandroid.list_games.WORLD_KEY
import com.helloandroid.list_sessions.GAME_KEY
import ru.napoleonit.talan.di.ControllerInjector
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.Comparator

val SESSION_KEY = "SESSION_KEY"

class SessionController(args: Bundle) : Controller(args), SessionContract.Controller {

    val SESSION_ADD_HP = 0
    val SESSION_ADD_SKILL = 1
    val SESSION_ADD_THING = 2
    val SESSION_ADD_COMMENT = 3

    @Inject
    lateinit var view: SessionContract.View

    val world = App.instance.worlds.first { it.id == args.getInt(WORLD_KEY) }
    val game = App.instance.games.first { it.id == args.getInt(GAME_KEY) && it.worldGroup == world.id }
    val session = App.instance.gameSessions.first { it.id == args.getInt(SESSION_KEY) && it.gameGroup == game.id && it.worldGroup == world.id }

    val itemsWrapper = SessionItemsWrapper()

    constructor(sessionId: Int, gameId: Int, worldId: Int) : this(Bundle().apply {
        putInt(SESSION_KEY, sessionId)
        putInt(GAME_KEY, gameId)
        putInt(WORLD_KEY, worldId)
    })

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View {
        setHasOptionsMenu(true)
        return view.createView(container)
    }

    override fun onContextAvailable(context: Context) {
        super.onContextAvailable(context)
        ControllerInjector.inject(this)
    }

    override fun onAttach(view: View) {
        super.onAttach(view)
        val characters = getCharacters()
        fun getCharacter(characterId: Int) = characters.single { it.id == characterId }
        val skills = getSkills()
        fun getSkill(skillId: Int) = skills.single { it.id == skillId }
        val things = getThings()
        fun getThing(thingId: Int) = things.single { it.id == thingId }

        itemsWrapper.addAll(App.instance.hpDiffs.filter { it.sessionGroup == session.id && it.gameGroup == game.id && it.worldGroup == world.id }
            .map { SessionItem(it.id, it.time, SessionItemType.ITEM_HP, "HP", getCharacter(it.characterGroup).name, it.value, it.characterGroup) })
        itemsWrapper.addAll(App.instance.skillDiffs.filter { it.sessionGroup == session.id && it.gameGroup == game.id && it.worldGroup == world.id }
            .map { SessionItem(it.id, it.time, SessionItemType.ITEM_SKILL, getSkill(it.skillGroup).name, getCharacter(it.characterGroup).name, it.value, it.characterGroup) })
        itemsWrapper.addAll(App.instance.thingDiffs.filter { it.sessionGroup == session.id && it.gameGroup == game.id && it.worldGroup == world.id }
            .map { SessionItem(it.id, it.time, SessionItemType.ITEM_THING, getThing(it.thingGroup).name, getCharacter(it.characterGroup).name, it.value, it.characterGroup) })
        itemsWrapper.addAll(App.instance.commentDiffs.filter { it.sessionGroup == session.id && it.gameGroup == game.id && it.worldGroup == world.id }
            .map { SessionItem(it.id, it.time, SessionItemType.ITEM_COMMENT, "", "", 0, -1, it.comment) })

        this.view.setData(itemsWrapper.toMutableList())
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.session_add, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.session_add -> view.showAddItemDialog()
            R.id.session_show_archived -> Log.i("JCD", "SHOW ARCHIVED") // TODO Checkbox
            R.id.session_close -> Log.i("JCD", "SESSION CLOSE")
        }
        return super.onOptionsItemSelected(item)
    }

    override fun getSessionDatetime(): String {
        return session.startTime.let { SimpleDateFormat("d MMMM HH:mm", Locale.getDefault()).format(it) }
    }

    override fun onAddItemClicked(which: Int) {
        val characterNames = getCharacters().map { it.name }
        when(which) {
            SESSION_ADD_HP -> view.showAddHpDialog(characterNames)
            SESSION_ADD_SKILL -> {
                val skillNames = getSkills().map { it.name }
                view.showAddSkillDialog(characterNames, skillNames)
            }
            SESSION_ADD_THING -> {
                val thingNames = getThings().map { it.name }
                view.showAddThingDialog(characterNames, thingNames)
            }
            SESSION_ADD_COMMENT -> view.showAddComment()
        }
    }

    override fun onHpChanged(pos: Int, value: Int) {
        val item = itemsWrapper.toList()[pos]
        item.value += value
        val hpId = item.id
        val characterId = item.characterId
        val hpDiff = App.instance.hpDiffs.single { it.id == hpId && it.sessionGroup == session.id && it.characterGroup == characterId && it.gameGroup == game.id && it.worldGroup == world.id }
        hpDiff.value += value
        this.view.itemChangedAt(pos)
    }

    override fun onSkillChanged(pos: Int, value: Int) {
        val item = itemsWrapper.toList()[pos]
        item.value += value
        val skillId = item.id
        val characterId = item.characterId
        val skillDiff = App.instance.skillDiffs.single { it.id == skillId && it.characterGroup == characterId && it.sessionGroup == session.id && it.gameGroup == game.id && it.worldGroup == world.id }
        skillDiff.value += value
        this.view.itemChangedAt(pos)
    }

    override fun onThingChanged(pos: Int, value: Int) {
        val item = itemsWrapper.toList()[pos]
        item.value += value
        val thingId = item.id
        val characterId = item.characterId
        val thingDiff = App.instance.thingDiffs.single { it.id == thingId && it.characterGroup == characterId && it.sessionGroup == session.id && it.gameGroup == game.id && it.worldGroup == world.id }
        thingDiff.value += value
        this.view.itemChangedAt(pos)
    }

    override fun onCommentChanged(pos: Int, comment: String) {
        val item = itemsWrapper.toList()[pos]
        val commentId = item.id
        item.comment = comment
        val commentDiff = App.instance.commentDiffs.single { it.id == commentId && it.sessionGroup == session.id && it.gameGroup == game.id && it.worldGroup == world.id }
        commentDiff.comment = comment
        // Do not itemChangedAt
    }

    override fun addHpDiff(which: Int) {
        val characters = getCharacters()
        val selectedCharacter = characters[which]
        val maxId = App.instance.hpDiffs.filter { it.sessionGroup == session.id && it.gameGroup == game.id && it.worldGroup == world.id }
            .maxBy { it.id }?.id ?: -1
        val hpDiff = HealthPointDiff(maxId + 1, 0, Calendar.getInstance().time, selectedCharacter.id, session.id, game.id, world.id)
        App.instance.hpDiffs.add(hpDiff)

        val item = SessionItem(hpDiff.id, hpDiff.time, SessionItemType.ITEM_HP, "HP", selectedCharacter.name, hpDiff.value, selectedCharacter.id, "")
        itemsWrapper.add(item)
        this.view.itemAddedAt(item.index, item)
    }

    override fun addCharacterSkillDiff(character: Int, skill: Int) {
        val selectedCharacter = getCharacters()[character]
        val maxId = App.instance.skillDiffs.filter { it.sessionGroup == session.id && it.gameGroup == game.id && it.worldGroup == world.id }
            .maxBy { it.id }?.id ?: -1
        val selectedSkill = getSkills()[skill]
        val skillDiff = SkillDiff(maxId + 1, 0, Calendar.getInstance().time, selectedCharacter.id, selectedSkill.id, session.id, game.id, world.id)
        App.instance.skillDiffs.add(skillDiff)

        val item = SessionItem(skillDiff.id, skillDiff.time, SessionItemType.ITEM_SKILL, selectedSkill.name, selectedCharacter.name, skillDiff.value, selectedCharacter.id)
        itemsWrapper.add(item)
        view.itemAddedAt(item.index, item)
    }

    override fun addCharacterThingDiff(character: Int, thing: Int) {
        val selectedCharacter = getCharacters()[character]
        val maxId = App.instance.thingDiffs.filter { it.sessionGroup == session.id && it.gameGroup == game.id && it.worldGroup == world.id }
            .maxBy { it.id }?.id ?: -1
        val selectedThing = getThings()[thing]
        val thingDiff = ThingDiff(maxId + 1, 0, Calendar.getInstance().time, selectedCharacter.id, selectedThing.id, session.id, game.id, world.id)
        App.instance.thingDiffs.add(thingDiff)

        val item = SessionItem(thingDiff.id, thingDiff.time, SessionItemType.ITEM_THING, selectedThing.name, selectedCharacter.name, thingDiff.value, selectedCharacter.id)
        itemsWrapper.add(item)
        view.itemAddedAt(item.index, item)
    }

    override fun addCommentDiff() {
        val comments = App.instance.commentDiffs.filter { it.sessionGroup == session.id && it.gameGroup == game.id && it.worldGroup == world.id }
        val maxId = comments.maxBy { it.id }?.id ?: -1
        val commentDiff = CommentDiff(maxId + 1, "", Calendar.getInstance().time, session.id, game.id, world.id)
        App.instance.commentDiffs.add(commentDiff)

        val thing = SessionItem(commentDiff.id, commentDiff.time, SessionItemType.ITEM_COMMENT, "", "", 0, -1, commentDiff.comment)
        itemsWrapper.add(thing)
        view.itemAddedAt(thing.index, thing)
    }

    private fun getCharacters(): List<Character> {
        return App.instance.characters.filter { it.gameGroup == game.id && it.worldGroup == world.id }.sortedBy { it.name }
    }

    private fun getSkills(): List<Skill> {
        return App.instance.skills.filter { it.worldGroup == world.id }.sortedBy { it.name }
    }

    private fun getThings(): List<Thing> {
        return App.instance.things.filter { it.worldGroup == world.id }.sortedBy { it.name }
    }

    class SessionItemsWrapper {

        private val sessionItems: TreeSet<SessionItem> = TreeSet<SessionItem>(Comparator { o1, o2 ->
            val res = o2.time.compareTo(o1.time)
            if (res == 0) {
                return@Comparator o1.type.ordinal.compareTo(o2.type.ordinal)
            }
            return@Comparator res
        })

        fun addAll(list: List<SessionItem>) {
            sessionItems.addAll(list)
            sessionItems.forEachIndexed { index, item -> item.index = index }
        }

        fun toMutableList(): MutableList<SessionItem> {
            return sessionItems.toMutableList()
        }

        fun toList(): List<SessionItem> {
            return sessionItems.toList()
        }

        fun add(item: SessionItem) {
            addAll(listOf(item))
        }
    }
}