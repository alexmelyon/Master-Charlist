package com.github.alexmelyon.master_charlist.room

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

@Entity
class Effect(
    var origin: String = "",
    var deviceId: String = "",
    var userUid: String? = null,
    var name: String = "",
    val worldGroup: String = "",
    var lastUsed: Date = Date(),
    var archived: Boolean = false
) {
    @Exclude
    var firestoreId = ""

    @Deprecated("Use firestoreId")
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0

    override fun toString() = name
}

class EffectStorage(
    val userService: UserService,
    val deviceService: DeviceService,
    val firestoreService: FirestoreService
) {

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
            .whereEqualTo(WORLD_GROUP, world.firestoreId)
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
//        val effectSkill = EffectSkill(0, effect.id, skill.id, world.id)
//        val id = db.effectSkillDao().insert(effectSkill)
//        effectSkill.id = id
//        effectItems[pos].effectSkills = effect.getSkillToValue(App.instance.skillStorage)
//            .map { EffectSkillRow(it.skill.name, it.value, it.skill) }
    }

    fun detachSkillFromEffect(effect: Effect, effectSkill: EffectSkill, onSuccess: () -> Unit) {
//        db.effectSkillDao().delete(effectSkill)
//        effectItems[pos].effectSkills = effect.getSkillToValue(App.instance.skillStorage)
//            .map { EffectSkillRow(it.skill.name, it.value, it.skill) }
    }
}

fun Effect.getAvailableSkills(skillStorage: SkillStorage): List<Skill> {
    val effect = this
//    val allSkills = skillStorage.skillDao().getAll(effect.worldGroup)
    val allSkills = skillStorage.skillDao().getAll(effect.worldGroup)
    val usedSkills = skillStorage.effectSkillDao().getAllByEffect(effect.id)
        .map { skillStorage.skillDao().get(it.skillGroup) }
        .map { it.id }
    val possible = allSkills.filterNot { it.id in usedSkills }
        .sortedBy { it.name }
    return possible
}

fun Effect.getUsedEffectSkills(skillStorage: SkillStorage): List<Pair<String, EffectSkill>> {
    val effect = this
    val used = skillStorage.effectSkillDao().getAllByEffect(effect.worldGroup, effect.id)
        .map { skillStorage.skillDao().get(it.skillGroup).name to it }
        .sortedBy { it.first }
    return used
}

data class SkillToValue(val skill: Skill, val value: Int)
fun Effect.getSkillToValue(skillStorage: SkillStorage): List<SkillToValue> {
    val effect = this
    return skillStorage.effectSkillDao().getAllByEffect(effect.worldGroup, effect.id)
        .map { skillStorage.skillDao().get(it.skillGroup) to it.value }
        .sortedBy { it.first.name }
}