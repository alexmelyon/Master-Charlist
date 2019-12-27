package com.github.alexmelyon.master_charlist.room

import androidx.room.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.iid.FirebaseInstanceId
import java.util.*

/** @property origin - One of deviceId or userUid */
@Entity
class World(
    var uid: String = "",
    var origin: String = "",
    var deviceId: String = "",
    var userUid: String? = null,
    var name: String = "",
    var createTime: Date = Date(),
    var archived: Boolean = false
) {
    @Exclude
    var firestoreId: String = ""
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0L

    override fun toString() = name
}

class WorldStorage(val userService: UserService, val deviceService: DeviceService) {

    private val worldsCollection by lazy {
        FirebaseFirestore.getInstance().collection("worlds")
    }

    fun create(name: String): World {
        val uuid = UUID.randomUUID().toString()
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val deviceId = deviceService.deviceId
        val origin = userId ?: deviceId
        val world = World(uuid, origin, deviceId, userService.currentUserUid, name, Calendar.getInstance().time)
        worldsCollection.add(world).addOnSuccessListener { docRef -> world.firestoreId = docRef.id }
        return world
    }

    fun rename(world: World, name: String) {
        world.name = name
        worldsCollection.document(world.firestoreId).update("name", name)
    }

    fun archive(world: World) {
        world.archived = true
        worldsCollection.document(world.firestoreId).update("archived", true)
    }

    fun getAll(onSuccess: (List<World>) -> Unit) {
        val origins = mutableListOf(FirebaseInstanceId.getInstance().id)
        userService.currentUserUid?.let { origins.add(it) }
        worldsCollection.whereEqualTo("archived", false)
            .whereIn("origin", origins)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val worlds = querySnapshot.map { docSnapshot ->
                    docSnapshot.toObject(World::class.java).apply {
                        firestoreId = docSnapshot.id
                    }
                }
                onSuccess(worlds)
            }
    }

    fun updateLocalWorlds(onSuccess: (List<World>) -> Unit) {
        val deviceId = deviceService.deviceId
        val userUid = userService.currentUserUid!!
        worldsCollection.whereEqualTo("origin", deviceId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                querySnapshot.forEach { docRef ->
                    worldsCollection.document(docRef.id).update(mapOf("origin" to userUid, "userUid" to userUid))
                }
                val worlds = querySnapshot.map { docSnapshot ->
                    docSnapshot.toObject(World::class.java).apply {
                        firestoreId = docSnapshot.id
                    }
                }.filter { !it.archived }
                onSuccess(worlds)
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