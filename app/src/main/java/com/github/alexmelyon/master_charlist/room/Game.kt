package com.github.alexmelyon.master_charlist.room

import android.util.Log
import androidx.room.*
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

@Entity
class Game(
    var origin: String = "",
    var deviceId: String = "",
    var userGroup: String? = null,
    var name: String = "",
    var worldGroup: String = "",
    var time: Date = Date(),
    var archived: Boolean = false
) {
    @Exclude
    var firestoreId: String = ""

    @Exclude
    @PrimaryKey(autoGenerate = true)
    @Deprecated("Deprecated since include Firestore")
    var id: Long = 0

    override fun toString() = name
}

class GameStorage(val userService: UserService, val deviceService: DeviceService) {

    private val gamesCollection by lazy {
        FirebaseFirestore.getInstance().collection("games")
    }

    fun getAll(world: World, onSuccess: (List<Game>) -> Unit) {
        gamesCollection.whereEqualTo(WORLD_GROUP, world.firestoreId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val games = querySnapshot.toObjects(Game::class.java)
                onSuccess(games)
            }
    }

    fun create(name: String, world: World, onSuccess: (Game) -> Unit) {
        val userId = userService.currentUserUid
        val deviceId = deviceService.deviceId
        val origin = userId ?: deviceId
        val game = Game(origin, deviceId, userId, name, world.firestoreId, Calendar.getInstance().time)
        gamesCollection.add(game).addOnSuccessListener {
            game.firestoreId = it.id
            onSuccess(game)
        }
    }

    fun archive(game: Game) {
        game.archived = true
        gamesCollection.document(game.firestoreId).update(FIELD_ARCHIVED, true)
    }

    fun rename(game: Game, name: String) {
        game.name = name
        gamesCollection.document(game.firestoreId).update(FIELD_NAME, name)
    }
}

@Dao
interface GameDao {

    @Query("SELECT * FROM game")
    fun getFull(): List<Game>

    @Query("SELECT * FROM game WHERE worldGroup = :worldId AND archived = :archived")
    fun getAll(worldId: Long, archived: Boolean = false): List<Game>

    @Query("SELECT * FROM game WHERE id = :gameId AND worldGroup = :worldId")
    fun getAll(gameId: Long, worldId: Long): Game

    @Insert
    fun insert(game: Game): Long

    @Update
    fun update(game: Game)
}