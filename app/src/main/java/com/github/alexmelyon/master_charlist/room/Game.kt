package com.github.alexmelyon.master_charlist.room

import android.os.Parcelable
import androidx.room.*
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.parcel.Parcelize
import java.util.*

@Entity
@Parcelize
class Game (
    var origin: String = "",
    var deviceId: String = "",
    var userUid: String? = null,
    var name: String = "",
    var worldGroup: String = "",
    var time: Date = Date(),
    var archived: Boolean = false
) : Parcelable {
    @Exclude
    var firestoreId: String = ""

    @Exclude
    @PrimaryKey(autoGenerate = true)
    @Deprecated("Deprecated since include Firestore")
    var id: Long = 0

    override fun toString() = name
}

class GameStorage(
    val userService: UserService,
    val deviceService: DeviceService,
    val firestoreService: FirestoreService
) {

    private val gamesCollection by lazy {
        FirebaseFirestore.getInstance().collection("games")
    }

    fun getAll(world: World, onSuccess: (List<Game>) -> Unit) {
        val origins = mutableListOf(deviceService.deviceId)
        userService.currentUserUid?.let { origins.add(it) }
        gamesCollection.whereIn(FIELD_ORIGIN, origins)
            .whereEqualTo(FIELD_WORLD_GROUP, world.firestoreId)
            .whereEqualTo(FIELD_ARCHIVED, false)
            .get(firestoreService.source)
            .addOnSuccessListener { querySnapshot ->
                val games = querySnapshot.map { docSnapshot ->
                    docSnapshot.toObject(Game::class.java).apply {
                        firestoreId = docSnapshot.id
                    }
                }
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

    fun archive(game: Game, onSuccess: () -> Unit) {
        game.archived = true
        gamesCollection.document(game.firestoreId)
            .update(FIELD_ARCHIVED, true)
            .addOnSuccessListener {
                onSuccess()
            }
    }

    fun rename(game: Game, name: String, onSuccess: () -> Unit) {
        game.name = name
        gamesCollection.document(game.firestoreId)
            .update(FIELD_NAME, name)
            .addOnSuccessListener {
                onSuccess()
            }
    }

    fun updateLocalGames() {
        val deviceId = deviceService.deviceId
        val userUid = userService.currentUserUid!!
        gamesCollection.whereEqualTo(FIELD_ORIGIN, deviceId)
            .get(firestoreService.source)
            .addOnSuccessListener { querySnapshot ->
                querySnapshot.forEach { docRef ->
                    gamesCollection.document(docRef.id)
                        .update(mapOf(FIELD_ORIGIN to userUid, FIELD_USER_UID to userUid))
                }
            }
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