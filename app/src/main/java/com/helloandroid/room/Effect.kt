package com.helloandroid.room

import android.arch.persistence.room.*
import java.util.*

@Entity
class Effect(var name: String, val worldGroup: Long, var lastUsed: Date, var archived: Boolean = false) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0

    override fun toString() = name
}

@Dao
interface EffectDao {
    @Query("SELECT * FROM effect")
    fun getFull(): List<Effect>

    @Query("SELECT * FROM effect WHERE worldGroup = :worldId AND archived = :archived")
    fun getAll(worldId: Long, archived: Boolean): List<Effect>

    @Query("SELECT * FROM effect WHERE id = :id LIMIT 1")
    fun get(id: Long): Effect

    @Insert
    fun insert(effect: Effect): Long

    @Update
    fun update(effect: Effect)
}