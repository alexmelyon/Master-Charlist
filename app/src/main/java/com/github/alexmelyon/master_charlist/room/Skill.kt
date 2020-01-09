package com.github.alexmelyon.master_charlist.room

import android.os.Parcelable
import androidx.room.*
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.parcel.Parcelize
import java.util.*

@Entity
@Parcelize
class Skill(
    var origin: String = "",
    var deviceId: String = "",
    var userUid: String? = null,
    var name: String = "",
    var worldGroup: String = "",
    var lastUsed: Date = Date(),
    var archived: Boolean = false
) : Parcelable {
    @Exclude
    var firestoreId = ""

    @Exclude
    @PrimaryKey(autoGenerate = true)
    @Deprecated("Use firestoreId instead")
    var id: Long = 0
    override fun toString() = name
}

class SkillStorage(
    val userService: UserService,
    val deviceService: DeviceService,
    val firestoreService: FirestoreService
) {

    private val skillsCollection by lazy {
        FirebaseFirestore.getInstance().collection("skills")
    }

    fun create(name: String, world: World, onSuccess: (Skill) -> Unit) {
        val userId = userService.currentUserUid
        val deviceId = deviceService.deviceId
        val origin = userId ?: deviceId
        val skill = Skill(origin, deviceId, userId, name, world.firestoreId, Calendar.getInstance().time)
        skillsCollection.add(skill).addOnSuccessListener {
            skill.firestoreId = it.id
            onSuccess(skill)
        }
    }

    fun rename(skill: Skill, name: String, onSuccess: () -> Unit) {
        skill.name = name
        skillsCollection.document(skill.firestoreId)
            .update(FIELD_NAME, name)
            .addOnSuccessListener {
                onSuccess()
            }
    }

    fun archive(skill: Skill, onSuccess: () -> Unit) {
        skill.archived = true
        skillsCollection.document(skill.firestoreId)
            .update(FIELD_ARCHIVED, true)
            .addOnSuccessListener {
                onSuccess()
            }
    }

    fun getAll(world: World, onSuccess: (List<Skill>) -> Unit) {
        val origins = mutableListOf(deviceService.deviceId)
        userService.currentUserUid?.let { origins.add(it) }
        skillsCollection.whereIn(FIELD_ORIGIN, origins)
            .whereEqualTo(WORLD_GROUP, world.firestoreId)
            .whereEqualTo(FIELD_ARCHIVED, false)
            .get(firestoreService.source)
            .addOnSuccessListener { querySnapshot ->
                val skills = querySnapshot.map { docSnapshot ->
                    docSnapshot.toObject(Skill::class.java).apply {
                        firestoreId = docSnapshot.id
                    }
                }
                onSuccess(skills)
            }
    }

    fun updateLocalSkills() {
        val deviceId = deviceService.deviceId
        val userUid = userService.currentUserUid!!
        skillsCollection.whereEqualTo(FIELD_ORIGIN, deviceId)
            .get(firestoreService.source)
            .addOnSuccessListener { querySnapshot ->
                querySnapshot.forEach { docRef ->
                    skillsCollection.document(docRef.id)
                        .update(mapOf(FIELD_ORIGIN to userUid, FIELD_USER_UID to userUid))
                }
            }
    }
}

@Dao
interface SkillDao {
    @Query("SELECT * FROM skill")
    fun getFull(): List<Skill>

    @Query("SELECT * FROM skill WHERE worldGroup = :worldId AND archived = :archived")
    fun getAll(worldId: Long, archived: Boolean = false): List<Skill>

    @Query("SELECT * FROM skill WHERE id = :skillId")
    fun get(skillId: Long): Skill

    @Insert
    fun insert(skill: Skill): Long

    @Update
    fun update(skill: Skill)
}