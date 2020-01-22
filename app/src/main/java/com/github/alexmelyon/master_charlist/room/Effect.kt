package com.github.alexmelyon.master_charlist.room

import androidx.room.*
import kotlinx.coroutines.Deferred
import java.util.*

@Entity
class Effect(var name: String = "", var worldGroup: String = "") : FirestoreDoc() {

    @Deprecated("")
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0

    override fun toString() = name
}

@Dao
interface EffectDao {
    @Query("SELECT * FROM effect")
    fun getFull(): List<Effect>

    @Query("SELECT * FROM effect WHERE worldGroup = :worldId AND archived = :archived")
    fun getAll(worldId: Long, archived: Boolean = false): List<Effect>

    @Query("SELECT * FROM effect WHERE id = :id")
    fun get(id: Long): Effect

    @Insert
    fun insert(effect: Effect): Long

    @Update
    fun update(effect: Effect)
}

fun Effect.getAvailableSkills(db: AppDatabase): List<Skill> {
    val effect = this
    val allSkills = db.skillDao().getAll(effect.worldGroup)
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

data class SkillToValue(val skill: Skill, val value: Int)
fun Effect.getSkillToValue(db: AppDatabase): List<SkillToValue> {
    val effect = this
    return db.effectSkillDao().getAllByEffect(effect.worldGroup, effect.id)
        .map { SkillToValue(db.skillDao().get(it.skillGroup), it.value) }
        .sortedBy { it.skill.name }
}

class EffectStorage : FirestoreCollection<Effect>("effects") {

    fun create(name: String, world: World): Deferred<Effect> {
        return super.create(Effect()) {
            this.name = name
            this.worldGroup = world.firestoreId
        }
    }

    fun getAll(world: World, onSuccess: (List<Effect>) -> Unit) {
//        val origins = mutableListOf(deviceService.deviceId)
//        userService.currentUserUid?.let { origins.add(it) }
//        collection.whereIn(FIELD_ORIGIN, origins)
//            .whereEqualTo(WORLD_GROUP, world.firestoreId)
//            .whereEqualTo(FIELD_ARCHIVED, false)
//            .get(firestoreService.source)
//            .addOnSuccessListener { querySnapshot ->
//                val effects = querySnapshot.map { docShapshot ->
//                    docShapshot.toObject(Effect::class.java).apply {
//                        firestoreId = docShapshot.id
//                    }
//                }
//                onSuccess(effects)
//            }

        super.getAll({ query -> query.whereEqualTo(WORLD_GROUP, world.firestoreId)}, onSuccess)
    }
}