package com.github.alexmelyon.master_charlist.room

import androidx.room.*
import java.util.*

@Entity
class GameSession(var name: String, val gameGroup: Long, val worldGroup: Long, val startTime: Date, var open: Boolean, var endTime: Date, var archived: Boolean = false) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0

    override fun toString() = name
}

@Dao
interface GameSessionDao {
    @Query("SELECT * FROM gamesession")
    fun getFull(): List<GameSession>

    @Query("SELECT * FROM gamesession WHERE worldGroup = :worldId AND gameGroup = :gameId AND archived = :archived")
    fun getAll(worldId: Long, gameId: Long, archived: Boolean = false): List<GameSession>

    @Query("SELECT id FROM gamesession WHERE worldGroup = :worldId AND gameGroup = :gameId AND open = 0 AND archived = 0")
    fun getClosed(worldId: Long, gameId: Long): List<Long>

    @Query("SELECT * FROM gamesession WHERE worldGroup = :worldId AND gameGroup = :gameId AND id = :id")
    fun get(worldId: Long, gameId: Long, id: Long): GameSession

    @Insert
    fun insert(gameSession: GameSession): Long

    @Update
    fun update(gameSession: GameSession)
}