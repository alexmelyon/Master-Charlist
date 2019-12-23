package com.github.alexmelyon.master_charlist.room

import androidx.room.*

@Entity
class GameCharacter(var name: String, val gameGroup: Long, val worldGroup: Long, var archived: Boolean = false) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
}

@Dao
interface CharacterDao {
    @Query("SELECT * FROM gamecharacter")
    fun getFull(): List<GameCharacter>

    @Query("SELECT * FROM gamecharacter WHERE worldGroup = :worldId AND gameGroup = :gameId AND archived = :archived")
    fun getAll(worldId: Long, gameId: Long, archived: Boolean = false): List<GameCharacter>

    @Query("SELECT * FROM gamecharacter WHERE worldGroup = :worldId AND gameGroup = :gameId AND id = :characterId")
    fun get(worldId: Long, gameId: Long, characterId: Long): GameCharacter

    @Insert
    fun insert(character: GameCharacter): Long

    @Update
    fun update(character: GameCharacter)
}

fun getUsedEffectsFor(diffs: List<EffectDiff>, effects: List<Effect>): List<Effect> {
    return diffs.groupBy { it.effectGroup }
        .map { it.key to it.value.map { if(it.value) 1 else -1 }.sum() }
        .filter { it.second > 0 }
        .map { e -> effects.single { it.id == e.first } }
        .sortedBy { it.name }
}