package com.github.alexmelyon.master_charlist.room

import androidx.room.*
import java.util.*

@Entity
class SkillDiff(
    var value: Int,
    val time: Date,
    val characterGroup: Long,
    val skillGroup: Long,
    val sessionGroup: Long,
    val gameGroup: Long,
    val worldGroup: Long,
    var archived: Boolean = false
) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
}

@Dao
interface SkillDiffDao {
    @Query("SELECT * FROM skilldiff")
    fun getFull(): List<SkillDiff>

    @Query("SELECT * FROM skilldiff WHERE worldGroup = :worldId AND gameGroup = :gameId AND characterGroup = :characterId AND archived = :archived")
    fun getAllByCharacter(worldId: Long, gameId: Long, characterId: Long, archived: Boolean = false): List<SkillDiff>

    @Query("SELECT * FROM skilldiff WHERE worldGroup = :worldId AND gameGroup = :gameId AND sessionGroup = :sessionId AND archived = :archived")
    fun getAllBySession(worldId: Long, gameId: Long, sessionId: Long, archived: Boolean = false): List<SkillDiff>

    @Query("SELECT * FROM skilldiff WHERE worldGroup = :worldId AND gameGroup = :gameId AND sessionGroup = :sessionId AND characterGroup = :characterId AND id = :id")
    fun get(worldId: Long, gameId: Long, sessionId: Long, characterId: Long, id: Long): SkillDiff

    @Insert
    fun insert(skillDiff: SkillDiff): Long

    @Update
    fun update(skillDiff: SkillDiff)
}