package com.github.alexmelyon.master_charlist.room

import androidx.room.*

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
    fun getAllByEffect(worldId: String, effectId: Long): List<EffectSkill>

    @Query("SELECT * FROM effectskill WHERE worldGroup = :worldId AND effectGroup = :effectId AND skillGroup = :skillId")
    fun get(worldId: Long, effectId: Long, skillId: Long): EffectSkill

    @Query("SELECT * FROM effectskill WHERE id = :id")
    fun get(id: Long): EffectSkill

    @Insert
    fun insert(effectSkill: EffectSkill): Long

    @Delete
    fun delete(effectSkill: EffectSkill)

    @Update
    fun update(effectSkill: EffectSkill)
}