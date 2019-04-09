package com.helloandroid.room

import android.arch.persistence.room.*
import java.util.*

@Entity
data class EffectDiff(var value: Boolean, var time: Date, val characterGroup: Long, var effectGroup: Long, var sessionGroup: Long, var gameGroup: Long, var worldGroup: Long, var archived: Boolean = false) {
    @PrimaryKey
    var id: Long = 0
}

@Dao
interface EffectDiffDao {
    @Query("SELECT * FROM effectdiff")
    fun getFull(): List<EffectDiff>

    @Query("SELECT * FROM effectdiff WHERE worldGroup = :worldId AND gameGroup = :gameId AND sessionGroup = :sessionId AND archived = :archived")
    fun getAllBySession(worldId: Long, gameId: Long, sessionId: Long, archived: Boolean): List<EffectDiff>

    @Insert
    fun insert(effectDiff: EffectDiff): Long
}