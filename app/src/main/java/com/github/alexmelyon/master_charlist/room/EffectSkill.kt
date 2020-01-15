package com.github.alexmelyon.master_charlist.room

import androidx.room.*
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

@Entity
class EffectSkill(
    var skillGroup: String = "",
    var value: Int = 0
) {
    @Deprecated("")
    var worldGroup: Long = 0L
    @Deprecated("")
    var effectGroup: String = ""
    @Exclude
    @Deprecated("Use firestoreId instead")
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
    fun getAllByEffect(worldId: Long, effectId: Long): List<EffectSkill>

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