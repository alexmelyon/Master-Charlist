package com.github.alexmelyon.master_charlist.room

import androidx.room.*
import java.util.*

@Entity
data class EffectDiff(
    var value: Boolean,
    var time: Date,
    var characterGroup: Long,
    var effectGroup: String,
    var sessionGroup: Long,
    var gameGroup: String,
    var worldGroup: String,
    var archived: Boolean = false
) {

    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
}

@Dao
interface EffectDiffDao {
    @Query("SELECT * FROM effectdiff")
    fun getFull(): List<EffectDiff>

    @Query("SELECT * FROM effectdiff WHERE worldGroup = :worldId AND gameGroup = :gameId AND sessionGroup = :sessionId AND archived = :archived")
    fun getAllBySession(worldId: Long, gameId: Long, sessionId: Long, archived: Boolean = false): List<EffectDiff>

    @Query("SELECT * FROM effectdiff WHERE worldGroup = :worldId AND gameGroup = :gameId AND characterGroup = :characterId AND archived = :archived")
    fun getAllByCharacter(worldId: Long, gameId: Long, characterId: Long, archived: Boolean = false): List<EffectDiff>

    @Query("SELECT * FROM effectdiff WHERE id = :id")
    fun get(id: Long): EffectDiff

    @Insert
    fun insert(effectDiff: EffectDiff): Long
}