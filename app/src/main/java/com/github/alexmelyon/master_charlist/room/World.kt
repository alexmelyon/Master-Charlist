package com.github.alexmelyon.master_charlist.room

import androidx.room.*
import java.util.*

// TODO
inline class WorldId(val id: Long);
@Entity
class World(var name: String, val createTime: Date, var archived: Boolean = false) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
    override fun toString() = name
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