package com.helloandroid.room

import android.arch.persistence.room.*
import java.util.*
import kotlin.math.sign

@Entity
class Effect(var name: String, val worldGroup: Long, var lastUsed: Date, var archived: Boolean = false) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0

    override fun toString() = name
}

@Dao
interface EffectDao {
    @Query("SELECT * FROM effect")
    fun getFull(): List<Effect>

    @Query("SELECT * FROM effect WHERE worldGroup = :worldId AND archived = :archived")
    fun getAll(worldId: Long, archived: Boolean): List<Effect>

    @Query("SELECT * FROM effect WHERE id = :id LIMIT 1")
    fun get(id: Long): Effect

    @Insert
    fun insert(effect: Effect): Long

    @Update
    fun update(effect: Effect)
}

fun Effect.getAvailableSkills(db: AppDatabase): List<Skill> {
    val effect = this
    val allSkills = db.skillDao().getAll(effect.worldGroup, archived = false)
    val usedSkills = db.effectSkillDao().getAllByEffect(effect.worldGroup, effect.id)
        .map { db.skillDao().get(it.skillGroup) }
        .map { it.id }
    val possible = allSkills.filterNot { it.id in usedSkills }
        .sortedBy { it.name }
    return possible
}

fun Effect.getUsedEffectSkills(db: AppDatabase): List<Pair<String, EffectSkill>> {
    val effect = this
    val used = db.effectSkillDao().getAllByEffect(effect.worldGroup, effect.id)
        .map { db.skillDao().get(it.skillGroup).name to it }
        .sortedBy { it.first }
    return used
}

fun Effect.getSkillToValue(db: AppDatabase): List<Pair<Skill, Int>> {
    val effect = this
    return db.effectSkillDao().getAllByEffect(effect.worldGroup, effect.id)
        .map { db.skillDao().get(it.skillGroup) to it.value }
        .sortedBy { it.first.name }
}