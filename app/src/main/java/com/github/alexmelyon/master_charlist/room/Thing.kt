package com.github.alexmelyon.master_charlist.room

import android.os.Parcelable
import androidx.room.*
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.parcel.Parcelize
import java.util.*

@Entity
@Parcelize
class Thing(
    var origin: String = "",
    var deviceId: String = "",
    var userUid: String? = null,
    var name: String = "",
    var worldGroup: String = "",
    var lastUsed: Date = Date(),
    var archived: Boolean = false
) : Parcelable {

    @Exclude
    var firestoreId: String = ""

    @Exclude
    @PrimaryKey(autoGenerate = true)
    @Deprecated("Use firestoreId instead")
    var id: Long = 0

    override fun toString() = name
}

class ThingStorage(
    val userService: UserService,
    val deviceService: DeviceService,
    val firestoreService: FirestoreService
) {
    private val thingsCollection by lazy {
        FirebaseFirestore.getInstance().collection("things")
    }

    fun create(name: String, world: World, onSuccess: (Thing) -> Unit) {
        val userId = userService.currentUserUid
        val deviceId = deviceService.deviceId
        val origin = userId ?: deviceId
        val thing = Thing(origin, deviceId, userId, name, world.firestoreId, Calendar.getInstance().time)
        thingsCollection.add(thing)
            .addOnSuccessListener {
                thing.firestoreId = it.id
                onSuccess(thing)
            }
    }

    fun rename(thing: Thing, name: String, onSuccess: () -> Unit) {
        thing.name = name
        thingsCollection.document(thing.firestoreId)
            .update(FIELD_NAME, name)
            .addOnSuccessListener {
                onSuccess()
            }
    }

    fun archive(thing: Thing, onSuccess: () -> Unit) {
        thing.archived = true
        thingsCollection.document(thing.firestoreId)
            .update(FIELD_ARCHIVED, true)
            .addOnSuccessListener {
                onSuccess()
            }
    }

    fun getAll(world: World, onSuccess: (List<Thing>) -> Unit) {
        val origins = mutableListOf(deviceService.deviceId)
        userService.currentUserUid?.let { origins.add(it) }
        thingsCollection.whereIn(FIELD_ORIGIN, origins)
            .whereEqualTo(FIELD_WORLD_GROUP, world.firestoreId)
            .whereEqualTo(FIELD_ARCHIVED, false)
            .get(firestoreService.source)
            .addOnSuccessListener { querySnapshot ->
                val things = querySnapshot.map { docSnapshot ->
                    docSnapshot.toObject(Thing::class.java).apply {
                        firestoreId = docSnapshot.id
                    }
                }
                onSuccess(things)
            }
    }

    fun updateLocalThings() {
        val deviceId = deviceService.deviceId
        val userUid = userService.currentUserUid!!
        thingsCollection.whereEqualTo(FIELD_ORIGIN, deviceId)
            .get(firestoreService.source)
            .addOnSuccessListener { querySnapshot ->
                querySnapshot.forEach { docRef ->
                    thingsCollection.document(docRef.id)
                        .update(mapOf(FIELD_ORIGIN to userUid, FIELD_USER_UID to userUid))
                }
            }
    }
}

@Dao
interface ThingDao {

    @Query("SELECT * FROM thing")
    fun getFull(): List<Thing>

    @Query("SELECT * FROM thing WHERE worldGroup = :worldId AND archived = :archived")
    fun getAll(worldId: Long, archived: Boolean = false): List<Thing>

    @Insert
    fun insert(thing: Thing): Long

    @Update
    fun update(thing: Thing)
}