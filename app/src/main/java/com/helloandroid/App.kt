package com.helloandroid

import android.app.Activity
import android.app.Application
import com.helloandroid.dagger.AppComponent
import com.helloandroid.dagger.DaggerAppComponent
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import java.util.*
import javax.inject.Inject


class App : Application(), HasActivityInjector {

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Activity>

    override fun activityInjector(): AndroidInjector<Activity> {
        println("GET ACTIVITY INJECTOR")
        return dispatchingAndroidInjector
    }

    companion object {
        lateinit var instance: App
            private set
        lateinit var appComponent: AppComponent
            private set
    }

    val worlds = mutableListOf<World>()
    val games = mutableListOf<Game>()
    val gameSessions = mutableListOf<GameSession>()
    val characters = mutableListOf<Character>()
    val skills = mutableListOf<Skill>()
    val things = mutableListOf<Thing>()
    val hpDiffs = mutableListOf<HealthPointDiff>()
    val skillDiffs = mutableListOf<SkillDiff>()
    val thingDiffs = mutableListOf<ThingDiff>()
    val commentDiffs = mutableListOf<CommentDiff>()

    override fun onCreate() {
        super.onCreate()
        instance = this

        appComponent = DaggerAppComponent.builder()
            .build()
        appComponent.inject(this)

        initWorld()
    }

    fun initWorld() {
        val now = Calendar.getInstance().time
        (1..3).forEach { worldId ->
            worlds.add(World(worldId, "$worldId world", now))
            val skillId = 0
            skills.add(Skill(skillId, "First skill", worldId))
            skills.add(Skill(1, "Second skill", worldId))
            skills.add(Skill(2, "Third skill", worldId))
            skills.add(Skill(3, "4 skill", worldId))
            skills.add(Skill(4, "5 skill", worldId))
            skills.add(Skill(5, "6 skill", worldId))
            skills.add(Skill(6, "7 skill", worldId))
            skills.add(Skill(7, "8 skill", worldId))
            skills.add(Skill(8, "9 skill", worldId))
            skills.add(Skill(9, "10 skill", worldId))
            skills.add(Skill(10, "11 skill", worldId))
            skills.add(Skill(11, "12 skill", worldId))
            skills.add(Skill(12, "13 skill", worldId))
            val thingId = 0
            things.add(Thing(thingId, "First thing", worldId))
            things.add(Thing(1, "Second thing", worldId))
            things.add(Thing(2, "Third thing", worldId))
            (1..3).forEach { gameId ->
                games.add(Game(gameId, "$gameId game", worldId, now))
                val characterId = 0
                characters.add(Character(characterId, "First Character", gameId, worldId))
                characters.add(Character(1, "Second Character", gameId, worldId))
                characters.add(Character(2, "Third Character", gameId, worldId))
                (1..6).forEach { sessionId ->
                    val minusHour = Calendar.getInstance().apply {
                        add(Calendar.HOUR, -sessionId)
                    }
                    gameSessions.add(GameSession(sessionId, "$sessionId session", worldId, gameId, minusHour.time, true, now))
                    hpDiffs += HealthPointDiff(1, 1, now, characterId, sessionId, gameId, worldId)
                    skillDiffs += SkillDiff(2, 2, now, characterId, skillId, sessionId, gameId, worldId)
                    skillDiffs += SkillDiff(22, 3, now, characterId, skillId, sessionId, gameId, worldId)
                    thingDiffs += ThingDiff(3, 3, now, characterId, thingId, sessionId, gameId, worldId)
                    thingDiffs += ThingDiff(33, 4, now, characterId, thingId, sessionId, gameId, worldId)
                    commentDiffs += CommentDiff(4, "Comment", now, sessionId, gameId, worldId)
                }
            }
        }
    }
}

class World(val id: Int, var name: String, val time: Date, var archived: Boolean = false) {
    override fun toString() = name
}

class Game(val id: Int, var name: String, val worldGroup: Int, val time: Date, var archived: Boolean = false)

class GameSession(val id: Int, var name: String, val gameGroup: Int, val worldGroup: Int, val startTime: Date, var open: Boolean, var endTime: Date, var archived: Boolean = false) {
    override fun toString() = "$name ${if(open) "Open" else "Closed"}"
}

class Character(val id: Int, var name: String, val gameGroup: Int, val worldGroup: Int, var archived: Boolean = false)

class Skill(val id: Int, var name: String, val worldGroup: Int, var archived: Boolean = false)

class Thing(val id: Int, var name: String, val worldGroup: Int, var archived: Boolean = false)

class HealthPointDiff(val id: Int, var value: Int, val time: Date, val characterGroup: Int, val sessionGroup: Int, val gameGroup: Int, val worldGroup: Int, var archived: Boolean = false)

class SkillDiff(val id: Int, var value: Int, val time: Date, val characterGroup: Int, val skillGroup: Int, val sessionGroup: Int, val gameGroup: Int, val worldGroup: Int, var archived: Boolean = false)

class ThingDiff(val id: Int, var value: Int, val time: Date, val characterGroup: Int, val thingGroup: Int, val sessionGroup: Int, val gameGroup: Int, val worldGroup: Int, var archived: Boolean = false)

class CommentDiff(val id: Int, var comment: String, val time: Date, val sessionGroup: Int, val gameGroup: Int, val worldGroup: Int, var archived: Boolean = false)
// TODO Состояния, особенности (плюсы минусы), дополнительные скиллы, заклинания, баффы, дебаффы, ачивки
