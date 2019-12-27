package com.github.alexmelyon.master_charlist.room

import androidx.room.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.iid.FirebaseInstanceId
import java.util.*

/** @property origin - One of deviceId or userUid */
@Entity
class World(var uid: String = "", var origin: String = "", var deviceId: String = "", var userUid: String? = null, var name: String = "", var createTime: Date = Date(), var archived: Boolean = false) {
    var firestoreId: String = ""
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0L
    override fun toString() = name
}

class WorldStorage(val userService: UserService) {

    private val worldsCollection by lazy {
        FirebaseFirestore.getInstance().collection("worlds")
    }

    fun create(name: String): World {
        val uuid = UUID.randomUUID().toString()
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val deviceId = FirebaseInstanceId.getInstance().id
        val origin = userId ?: deviceId
        val world = World(uuid, origin, deviceId, userService.currentUser, name, Calendar.getInstance().time)
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
        val deviceId = FirebaseInstanceId.getInstance().id
        worldsCollection.whereEqualTo("deviceId", deviceId)
            .whereEqualTo("archived", false)
            .get()
            .addOnSuccessListener { documents ->
                val worlds = documents.map { docSnapshot -> docSnapshot.toObject(World::class.java).apply { firestoreId = docSnapshot.id } }
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