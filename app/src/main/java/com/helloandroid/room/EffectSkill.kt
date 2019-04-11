package com.helloandroid.room

import android.arch.persistence.room.*

@Entity
class EffectSkill(var value: Int, var effectGroup: Long, var skillGroup: Long, var worldGroup: Long) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
}

@Dao
interface EffectSkillDao {

    @Query("SELECT * FROM effectskill")
    fun getFull(): List<EffectSkill>

    @Query("SELECT * FROM effectskill WHERE worldGroup = :worldId")
    fun getAll(worldId: Long): List<EffectSkill>

    @Query("SELECT * FROM effectskill WHERE worldGroup = :worldId AND effectGroup = :effectId")
    fun getAll(worldId: Long, effectId: Long): List<EffectSkill>

    @Insert
    fun insert(effectSkill: EffectSkill): Long
}