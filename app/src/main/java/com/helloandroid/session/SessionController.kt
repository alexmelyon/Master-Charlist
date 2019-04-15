package com.helloandroid.session

import android.content.Context
import android.os.Bundle
import android.view.*
import com.bluelinelabs.conductor.Controller
import com.helloandroid.*
import com.helloandroid.list_games.WORLD_KEY
import com.helloandroid.list_sessions.GAME_KEY
import com.helloandroid.list_sessions.ListSessionsDelegate
import com.helloandroid.room.*
import ru.napoleonit.talan.di.ControllerInjector
import java.lang.ref.WeakReference
import java.util.*
import javax.inject.Inject
import kotlin.Comparator

val SESSION_KEY = "SESSION_KEY"

class SessionController(args: Bundle) : Controller(args), SessionContract.Controller {

    val SESSION_ADD_HP = 0
    val SESSION_ADD_SKILL = 1
    val SESSION_ADD_THING = 2
    val SESSION_ADD_EFFECT = 3
    val SESSION_REMOVE_EFFECT = 4
    val SESSION_ADD_COMMENT = 5

    @Inject
    lateinit var view: SessionContract.View
    @Inject
    lateinit var db: AppDatabase

    lateinit var world: World
    lateinit var game: Game
    lateinit var session: GameSession
    var delegate: WeakReference<ListSessionsDelegate>? = null

    val itemsWrapper = SessionItemsWrapper()

    constructor(sessionId: Long, gameId: Long, worldId: Long) : this(Bundle().apply {
        putLong(SESSION_KEY, sessionId)
        putLong(GAME_KEY, gameId)
        putLong(WORLD_KEY, worldId)
    })

    override fun onContextAvailable(context: Context) {
        super.onContextAvailable(context)
        ControllerInjector.inject(this)

        world = db.worldDao().getWorldById(args.getLong(WORLD_KEY))
        game = db.gameDao().getAll(args.getLong(GAME_KEY), world.id)
        session = db.gameSessionDao().get(world.id, game.id, args.getLong(SESSION_KEY))

        itemsWrapper.addAll(db.hpDiffDao().getAllBySession(world.id, game.id, session.id, archived = false)
            .map { SessionItem(it.id, it.time, SessionItemType.ITEM_HP, "HP", getCharacter(it.characterGroup).name, it.value, it.characterGroup) })
        itemsWrapper.addAll(db.skillDiffDao().getAllBySession(world.id, game.id, session.id, archived = false)
            .map { SessionItem(it.id, it.time, SessionItemType.ITEM_SKILL, getSkill(it.skillGroup).name, getCharacter(it.characterGroup).name, it.value, it.characterGroup) })
        itemsWrapper.addAll(db.thingDiffDao().getAllBySession(world.id, game.id, session.id, archived = false)
            .map { SessionItem(it.id, it.time, SessionItemType.ITEM_THING, getThing(it.thingGroup).name, getCharacter(it.characterGroup).name, it.value, it.characterGroup) })
        itemsWrapper.addAll(db.effectDiffDao().getAllBySession(world.id, game.id, session.id, archived = false)
            .map { effectDiff ->
                val intValue = if(effectDiff.value) 1 else -1
                val skillToValue = skillNamesToValue(effectDiff)
                SessionItem(effectDiff.id, effectDiff.time, SessionItemType.ITEM_EFFECT, getEffect(effectDiff.effectGroup).name, getCharacter(effectDiff.characterGroup).name, intValue, effectDiff.characterGroup, effectSkills = skillToValue)
            })
        itemsWrapper.addAll(db.commentDiffDao().getAll(world.id, game.id, session.id, archived = false)
            .map { SessionItem(it.id, it.time, SessionItemType.ITEM_COMMENT, "", "", 0, -1, it.comment) })
    }

    private fun skillToValue(effectDiff: EffectDiff): List<Pair<Skill, Int>> {
        val effect = db.effectDao().get(effectDiff.effectGroup)
        return effect.getSkillToValue(db)
    }

    private fun skillNamesToValue(effectDiff: EffectDiff): List<Pair<String, Int>> {
        return skillToValue(effectDiff).map { it.first.name to it.second }
    }

    fun getCharacter(characterId: Long): GameCharacter {
        val characters = getCharacters()
        return characters.single { it.id == characterId }
    }
    fun getSkill(skillId: Long): Skill {
        val skills = getSkills()
        return skills.single { it.id == skillId }
    }
    fun getThing(thingId: Long): Thing {
        val things = getThings()
        return things.single { it.id == thingId }
    }
    fun getEffect(effectid: Long): Effect {
        val effects = getEffects()
        return effects.single { it.id == effectid }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View {
        setHasOptionsMenu(true)
        return view.createView(container)
    }

    override fun onAttach(view: View) {
        super.onAttach(view)

        this.view.setData(itemsWrapper.toMutableList())
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.session, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.menu_add_session_item -> {
                view.showAddSomethingDialog()
                return true
            }
            R.id.session_close -> {
                view.showCloseSessionDialog(session.name)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun getTitle(): String {
        return session.name
    }

    override fun isSessionOpen(): Boolean {
        return session.open
    }

    override fun onAddItemClicked(which: Int) {
        val characterNames = getCharacters().map { it.name }
        when(which) {
            SESSION_ADD_HP -> view.showAddHpDialog(characterNames)
            SESSION_ADD_SKILL -> {
                // TODO Create new
//                val skillNames = getSkills().map { it.name }
                view.showAddSkillDialog(getCharacters(), getSkills())
            }
            SESSION_ADD_THING -> {
                // TODO Create new
                val thingNames = getThings().map { it.name }
                view.showAddThingDialog(characterNames, thingNames)
            }
            SESSION_ADD_EFFECT -> {
                // TODO Create new
                val effectNames = getEffects().map { it.name }
                view.showAttachEffectDialog(characterNames, effectNames)
            }
            SESSION_REMOVE_EFFECT -> {
                val characterToEffects = getUsedEffects()
                view.showRemoveEffectDialog(characterNames, characterToEffects)
            }
            SESSION_ADD_COMMENT -> view.showAddComment()
        }
    }

    override fun onHpChanged(pos: Int, value: Int) {
        val item = itemsWrapper.toList()[pos]
        item.value += value
        val hpId = item.id
        val characterId = item.characterId
        val hpDiff = db.hpDiffDao().get(world.id, game.id, session.id, characterId, hpId)
        hpDiff.value += value
        db.hpDiffDao().update(hpDiff)
        this.view.itemChangedAt(pos)
    }

    override fun onSkillChanged(pos: Int, value: Int) {
        val item = itemsWrapper.toList()[pos]
        item.value += value
        val skillId = item.id
        val characterId = item.characterId
        val skillDiff = db.skillDiffDao().get(world.id, game.id, session.id, characterId, skillId)
        skillDiff.value += value
        db.skillDiffDao().update(skillDiff)
        this.view.itemChangedAt(pos)
    }

    override fun onThingChanged(pos: Int, value: Int) {
        val item = itemsWrapper.toList()[pos]
        item.value += value
        val thingId = item.id
        val characterId = item.characterId
        val thingDiff = db.thingDiffDao().get(world.id, game.id, session.id, characterId, thingId)
        thingDiff.value += value
        db.thingDiffDao().update(thingDiff)
        this.view.itemChangedAt(pos)
    }

    override fun onCommentChanged(pos: Int, comment: String) {
        val item = itemsWrapper.toList()[pos]
        val commentId = item.id
        item.comment = comment
        val commentDiff = db.commentDiffDao().get(commentId)
        commentDiff.comment = comment
        db.commentDiffDao().update(commentDiff)
        // Do not itemChangedAt
    }

    override fun addHpDiff(character: Int) {
        val characters = getCharacters()
        val selectedCharacter = characters[character]
        val hpDiff = HealthPointDiff(0, Calendar.getInstance().time, selectedCharacter.id, session.id, game.id, world.id)
        val id = db.hpDiffDao().insert(hpDiff)
        hpDiff.id = id

        val item = SessionItem(hpDiff.id, hpDiff.time, SessionItemType.ITEM_HP, "HP", selectedCharacter.name, hpDiff.value, selectedCharacter.id, "")
        itemsWrapper.add(item)
        this.view.itemAddedAt(item.index, item)
    }

    override fun addCharacterSkillDiff(character: GameCharacter, skill: Skill) {
//        val character = getCharacters()[character]
//        val skill = getSkills()[skill]
        skill.lastUsed = Calendar.getInstance().time
        db.skillDao().update(skill)
        val skillDiff = SkillDiff(0, Calendar.getInstance().time, character.id, skill.id, session.id, game.id, world.id)
        val id = db.skillDiffDao().insert(skillDiff)
        skillDiff.id = id

        val item = SessionItem(skillDiff.id, skillDiff.time, SessionItemType.ITEM_SKILL, skill.name, character.name, skillDiff.value, character.id)
        itemsWrapper.add(item)
        view.itemAddedAt(item.index, item)
    }

    override fun addCharacterThingDiff(character: Int, thing: Int) {
        val selectedCharacter = getCharacters()[character]
        val selectedThing = getThings()[thing]
        selectedThing.lastUsed = Calendar.getInstance().time
        db.thingDao().update(selectedThing)
        val thingDiff = ThingDiff(0, Calendar.getInstance().time, selectedCharacter.id, selectedThing.id, session.id, game.id, world.id)
        val id = db.thingDiffDao().insert(thingDiff)
        thingDiff.id = id

        val item = SessionItem(thingDiff.id, thingDiff.time, SessionItemType.ITEM_THING, selectedThing.name, selectedCharacter.name, thingDiff.value, selectedCharacter.id)
        itemsWrapper.add(item)
        view.itemAddedAt(item.index, item)
    }

    override fun addCharacterAttachEffectDiff(character: Int, effect: Int) {
        val selectedCharacter = getCharacters()[character]
        val selectedEffect = getEffects()[effect]
        selectedEffect.lastUsed = Calendar.getInstance().time
        db.effectDao().update(selectedEffect)
        val effectDiff = EffectDiff(true, Calendar.getInstance().time, selectedCharacter.id, selectedEffect.id, session.id, game.id, world.id)
        val id = db.effectDiffDao().insert(effectDiff)
        effectDiff.id = id

        val skillToValue = skillNamesToValue(effectDiff)
        val item = SessionItem(effectDiff.id, effectDiff.time, SessionItemType.ITEM_EFFECT, selectedEffect.name, selectedCharacter.name, 1, selectedCharacter.id, effectSkills = skillToValue)
        itemsWrapper.add(item)
        view.itemAddedAt(item.index, item)
    }

    override fun addCharacterDetachEffectDiff(character: Int, effect: Int) {
        val selectedCharacter = getCharacters()[character]
        val usedEffects = getUsedEffects().getValue(selectedCharacter.name)
        val selectedEffect = usedEffects[effect]
        selectedEffect.lastUsed = Calendar.getInstance().time
        db.effectDao().update(selectedEffect)
        val effectDiff = EffectDiff(false, Calendar.getInstance().time, selectedCharacter.id, selectedEffect.id, session.id, game.id, world.id)
        val id = db.effectDiffDao().insert(effectDiff)
        effectDiff.id = id

        val item = SessionItem(effectDiff.id, effectDiff.time, SessionItemType.ITEM_EFFECT, selectedEffect.name, selectedCharacter.name, -1, selectedCharacter.id)
        itemsWrapper.add(item)
        view.itemAddedAt(item.index, item)
    }

    override fun getAvailableSkillsForEffect(pos: Int): List<Skill> {
        val effectDiff = getEffectDiffAt(pos)
        val effect = db.effectDao().get(effectDiff.effectGroup)
        return effect.getAvailableSkills(db)
    }

    override fun attachSkillForEffect(pos: Int, skill: Skill) {
        val effectDiff = getEffectDiffAt(pos)
        val effectSkill = EffectSkill(0, effectDiff.effectGroup, skill.id, world.id)
        val id = db.effectSkillDao().insert(effectSkill)
        effectSkill.id = id

        itemsWrapper.toList()[pos].effectSkills = skillNamesToValue(effectDiff)
        view.itemChangedAt(pos)
    }

    override fun getUsedEffectSkills(pos: Int): List<Pair<String, EffectSkill>> {

        val effectDiff = getEffectDiffAt(pos)
        val effect = db.effectDao().get(effectDiff.effectGroup)
        return effect.getUsedEffectSkills(db)
    }

    private fun getEffectDiffAt(pos: Int): EffectDiff {
        val effectDiffId = itemsWrapper.toList()[pos].id
        val effectDiff = db.effectDiffDao().get(effectDiffId)
        return effectDiff
    }

    override fun detachSkillForEffect(pos: Int, effectSkill: EffectSkill) {
        db.effectSkillDao().delete(effectSkill)
        val effectDiff = getEffectDiffAt(pos)
        itemsWrapper.toList()[pos].effectSkills = skillNamesToValue(effectDiff)

        view.itemChangedAt(pos)
    }

    override fun onEffectSkillChanged(pos: Int, subPos: Int, value: Int) {
        val effectDiff = getEffectDiffAt(pos)
        val skill = skillToValue(effectDiff)[subPos].first
        val effectSkill = db.effectSkillDao().get(world.id, effectDiff.effectGroup, skill.id)
        effectSkill.value += value
        db.effectSkillDao().update(effectSkill)

        itemsWrapper.toList()[pos].effectSkills = skillNamesToValue(effectDiff)
        view.itemChangedAt(pos)
    }

    override fun addCommentDiff() {
        val commentDiff = CommentDiff("", Calendar.getInstance().time, session.id, game.id, world.id)
        val id = db.commentDiffDao().insert(commentDiff)
        commentDiff.id = id

        val thing = SessionItem(commentDiff.id, commentDiff.time, SessionItemType.ITEM_COMMENT, "", "", 0, -1, commentDiff.comment)
        itemsWrapper.add(thing)
        view.itemAddedAt(thing.index, thing)
    }

    override fun createCharacter(name: String) {
        val character = GameCharacter(name, game.id, world.id, archived = false)
        val id = db.characterDao().insert(character)
        character.id = id
    }

    override fun createSkill(name: String): Skill {
        val skill = Skill(name, world.id, Calendar.getInstance().time, archived = false)
        val id = db.skillDao().insert(skill)
        skill.id = id
        return skill
    }

    override fun createThing(name: String) {
        val thing = Thing(name, world.id, Calendar.getInstance().time, archived = false)
        val id = db.thingDao().insert(thing)
        thing.id = id
    }

    override fun createEffect(name: String) {
        val effect = Effect(name, world.id, Calendar.getInstance().time, archived = false)
        val id = db.effectDao().insert(effect)
        effect.id = id
    }

    private fun getCharacters(): List<GameCharacter> {
        return db.characterDao().getAll(world.id, game.id, archived = false)
            .sortedBy { it.name }
    }

    private fun getSkills(): List<Skill> {
        return db.skillDao().getAll(world.id, archived = false)
            .sortedBy { it.name }
    }

    private fun getThings(): List<Thing> {
        return db.thingDao().getAll(world.id, archived = false)
            .sortedBy { it.name }
    }

    private fun getEffects(): List<Effect> {
        return db.effectDao().getAll(world.id, archived = false)
            .sortedBy { it.name }
    }

    private fun getUsedEffects(): Map<String, List<Effect>> {
        val closedSessions = db.gameSessionDao().getClosed(world.id, game.id)
        val effects = db.effectDao().getAll(world.id, archived = false)
        val res = db.characterDao().getAll(world.id, game.id, archived = false).map { character ->
            val closed = db.effectDiffDao().getAllByCharacter(world.id, game.id, character.id, archived = false)
                .filter { it.sessionGroup in closedSessions }
            val current = db.effectDiffDao().getAllBySession(world.id, game.id, session.id, archived = false)
            val usedEffects = getUsedEffectsFor(closed + current, effects)
            character.name to usedEffects
        }.toMap()
        return res
    }

    class SessionItemsWrapper {

        private val sessionItems: TreeSet<SessionItem> = TreeSet<SessionItem>(Comparator { o1, o2 ->
            var res = o2.time.compareTo(o1.time)
            if (res == 0) {
                res = o1.type.ordinal.compareTo(o2.type.ordinal)
                if(res == 0) {
                    res = o1.id.compareTo(o2.id)
                }
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

    override fun closeSession() {
        session.open = false
        session.endTime = Calendar.getInstance().time
        db.gameSessionDao().update(session)
        delegate?.get()?.updateListSessionsScreen(activity!!)

        router.popCurrentController()
    }
}