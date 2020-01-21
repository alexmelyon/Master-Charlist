package com.github.alexmelyon.master_charlist.room

import androidx.room.*
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.util.*

@Entity
class Effect(
    var origin: String = "",
    var deviceId: String = "",
    var userUid: String? = null,
    var name: String = "",
    var worldGroup: String = "",
    var lastUsed: Date = Date(),
    @Ignore
    var effectSkills: MutableList<EffectSkill> = mutableListOf(),
    var archived: Boolean = false
) {
    @Exclude
    var firestoreId = ""

    @Deprecated("Use firestoreId")
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

class EffectStorage(
    val userService: UserService,
    val deviceService: DeviceService,
    val firestoreService: FirestoreService
) {

    companion object {
        const val FIELD_EFFECT_SKILLS = "effectSkills"
    }

    private val effectsCollection by lazy {
        FirebaseFirestore.getInstance().collection("effects")
    }

    fun create(name: String, world: World, onSuccess: (Effect) -> Unit) {
        val userId = userService.currentUserUid
        val deviceId = deviceService.deviceId
        val origin = userId ?: deviceId
        val effect = Effect(origin, deviceId, userId, name, world.firestoreId, Calendar.getInstance().time)
        effectsCollection.add(effect).addOnSuccessListener {
            effect.firestoreId = it.id
            onSuccess(effect)
        }
    }

    fun rename(effect: Effect, name: String, onSuccess: () -> Unit) {
        effect.name = name
        effectsCollection.document(effect.firestoreId)
            .update(FIELD_NAME, name)
            .addOnSuccessListener {
                onSuccess()
            }
    }

    fun archive(effect: Effect, onSuccess: () -> Unit) {
        effect.archived = true
        effectsCollection.document(effect.firestoreId)
            .update(FIELD_ARCHIVED, true)
            .addOnSuccessListener {
                onSuccess()
            }
    }

    fun getAll(world: World, onSuccess: (List<Effect>) -> Unit) {
        val origins = mutableListOf(deviceService.deviceId)
        userService.currentUserUid?.let { origins.add(it) }
        effectsCollection.whereIn(FIELD_ORIGIN, origins)
            .whereEqualTo(FIELD_WORLD_GROUP, world.firestoreId)
            .whereEqualTo(FIELD_ARCHIVED, false)
            .get(firestoreService.source)
            .addOnSuccessListener { querySnapshot ->
                val effects = querySnapshot.map { docSnapshot ->
                    docSnapshot.toObject(Effect::class.java).apply {
                        firestoreId = docSnapshot.id
                    }
                }
                onSuccess(effects)
            }
    }

    fun updateLocalEffects() {
        val deviceId = deviceService.deviceId
        val userUid = userService.currentUserUid!!
        effectsCollection.whereEqualTo(FIELD_ORIGIN, deviceId)
            .get(firestoreService.source)
            .addOnSuccessListener { querySnapshot ->
                querySnapshot.forEach { docRef ->
                    effectsCollection.document(docRef.id)
                        .update(mapOf(FIELD_ORIGIN to userUid, FIELD_USER_UID to userUid))
                }
            }
    }

    fun attachSkillForEffect(effect: Effect, skill: Skill, onSuccess: () -> Unit) {
        val effectSkill = EffectSkill(skill.firestoreId, 0)
        effect.effectSkills.add(effectSkill)
        effectsCollection.document(effect.firestoreId)
            .update(FIELD_EFFECT_SKILLS, effect.effectSkills)
            .addOnSuccessListener {
                onSuccess()
            }
    }

    fun detachSkillFromEffect(effect: Effect, effectSkill: EffectSkill, onSuccess: () -> Unit) {
        effect.effectSkills.removeAll { it.skillGroup == effectSkill.skillGroup }
        effectsCollection.document(effect.firestoreId)
            .update(FIELD_EFFECT_SKILLS, effect.effectSkills)
            .addOnSuccessListener {
                onSuccess()
            }
    }

    fun updateEffectSkillValue(effect: Effect, skill: Skill, delta: Int, onSuccess: () -> Unit) {
        val effectSkill = effect.effectSkills.single { it.skillGroup == skill.firestoreId }
        effectSkill.value += delta
        effectsCollection.document(effect.firestoreId)
            .update(FIELD_EFFECT_SKILLS, effect.effectSkills)
            .addOnSuccessListener {
                onSuccess()
            }
    }

    fun get(effectGroup: String): Effect {
        return runBlocking {
            val effect = effectsCollection.document(effectGroup)
                .get()
                .await()
                .toObject(Effect::class.java)
            return@runBlocking effect!!
        }
    }
}

fun Effect.getAvailableSkills(skillStorage: SkillStorage, onSuccess: (List<Skill>) -> Unit) {
    val effect = this
    skillStorage.getAll(effect.worldGroup) { skills ->
        val usedSkills = effect.effectSkills.map { it.skillGroup }
        val possible = skills.filterNot { it.firestoreId in usedSkills }
            .sortedBy { it.name }
        onSuccess(possible)
    }
}

fun Effect.getAvailableSkills(skillStorage: SkillStorage): List<Skill> {
    val skills = mutableListOf<Skill>()
    runBlocking {
        getAvailableSkills(skillStorage) { res ->
            skills.addAll(res)
        }
    }
    return skills
}

data class SkillnameToEffectskill(val skillName: String, val effectSkill: EffectSkill)
fun Effect.getUsedEffectSkills(skillStorage: SkillStorage, onSuccess: (List<SkillnameToEffectskill>) -> Unit) {
    val effect = this
    skillStorage.getAll(effect.worldGroup) { skills ->
        val used = effect.effectSkills.map { effectSkill ->
            val skill = skills.single { it.firestoreId == effectSkill.skillGroup }
            SkillnameToEffectskill(skill.name, effectSkill)
        }.sortedBy { it.skillName }
        onSuccess(used)
    }
}

data class SkillToValue(val skill: Skill, val value: Int)
fun Effect.getSkillToValue(skillStorage: SkillStorage, onSuccess: (List<SkillToValue>) -> Unit) {
    val effect = this
    skillStorage.getAll(effect.worldGroup) { skills ->
        effect.effectSkills.map { effectSkill ->
            val skill = skills.single { it.firestoreId == effectSkill.skillGroup }
            SkillToValue(skill, effectSkill.value)
        }.sortedBy { it.skill.name }
    }
}

fun Effect.getSkillToValue(skillStorage: SkillStorage): List<SkillToValue> {
    val res = mutableListOf<SkillToValue>()
    runBlocking {
        getSkillToValue(skillStorage) { skillsToValue ->
            res.addAll(skillsToValue)
        }
    }
    return res
}