package com.github.alexmelyon.master_charlist.room

import android.os.Parcelable
import android.util.Log
import androidx.room.*
import com.github.alexmelyon.master_charlist.room.UserService.Companion.FIELD_USER_UID
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.android.parcel.Parcelize
import java.lang.Exception
import java.util.*


const val WORLD_GROUP = "worldGroup"
const val FIELD_NAME = "name"
const val FIELD_ARCHIVED = "archived"
const val FIELD_ORIGIN = "origin"

/** @property origin - One of deviceId or userUid */
@Entity
@Parcelize
class World(
    var origin: String = "",
    var deviceId: String = "",
    var userGroup: String? = null,
    var name: String = "",
    var createTime: Date = Date(),
    var archived: Boolean = false
): Parcelable {
    @Exclude
    var firestoreId: String = ""

    @Exclude
    @PrimaryKey(autoGenerate = true)
    @Deprecated("Deprecated since include Firestore")
    var id: Long = 0L

    override fun toString() = name
}

class WorldStorage(val userService: UserService, val deviceService: DeviceService) {

    private val worldsCollection by lazy {
        FirebaseFirestore.getInstance().collection("worlds")
    }

    fun create(name: String, onSuccess: (World) -> Unit) {
        val userId = userService.currentUserUid
        val deviceId = deviceService.deviceId
        val origin = userId ?: deviceId
        val world = World(origin, deviceId, userService.currentUserUid, name, Calendar.getInstance().time)
        worldsCollection.add(world).addOnSuccessListener { docRef ->
            world.firestoreId = docRef.id
            onSuccess(world)
        }
    }

    fun rename(world: World, name: String) {
        world.name = name
        worldsCollection.document(world.firestoreId).update(FIELD_NAME, name)
    }

    fun archive(world: World) {
        world.archived = true
        worldsCollection.document(world.firestoreId).update(FIELD_ARCHIVED, true)
    }

    fun getAll(onSuccess: (List<World>) -> Unit) {
        val origins = mutableListOf(deviceService.deviceId)
        userService.currentUserUid?.let { origins.add(it) }
        val ex = Exception()
        worldsCollection.whereIn(FIELD_ORIGIN, origins)
            .whereEqualTo(FIELD_ARCHIVED, false)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val worlds = querySnapshot.map { docSnapshot ->
                    docSnapshot.toObject(World::class.java).apply {
                        firestoreId = docSnapshot.id
                    }
                }
                try {
                    onSuccess(worlds)
                } catch (t: Throwable) {
                    t.addSuppressed(ex)
                    Log.e("JCD", "2", t)
                    throw t
                }
            }
    }

    fun updateLocalWorlds(onSuccess: () -> Unit) {
        val deviceId = deviceService.deviceId
        val userUid = userService.currentUserUid!!
        worldsCollection.whereEqualTo(FIELD_ORIGIN, deviceId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                querySnapshot.forEach { docRef ->
                    worldsCollection.document(docRef.id)
                        .update(mapOf(FIELD_ORIGIN to userUid, FIELD_USER_UID to userUid))
                }
                onSuccess()
            }
    }
}

@Dao
interface WorldDao {

    @Query("SELECT * FROM world")
    fun getFull(): List<World>

    @Query("SELECT * FROM world WHERE archived = :archived")
    fun getAll(archived: Boolean = false): List<World>

    @Query("SELECT * FROM world WHERE id = :worldId")
    fun getWorldById(worldId: Long): World

    @Insert
    fun insert(world: World): Long

    @Update
    fun update(world: World)
}