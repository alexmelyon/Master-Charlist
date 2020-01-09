package com.github.alexmelyon.master_charlist.room

import android.os.Parcelable
import androidx.room.*
import kotlinx.android.parcel.Parcelize
import java.util.*

@Entity
@Parcelize
class Skill(
//    var origin: String = "",
//    var deviceId: String = "",

    var name: String, val worldGroup: Long, var lastUsed: Date, var archived: Boolean = false) : Parcelable {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
    override fun toString() = name
}

@Dao
interface SkillDao {
    @Query("SELECT * FROM skill")
    fun getFull(): List<Skill>

    @Query("SELECT * FROM skill WHERE worldGroup = :worldId AND archived = :archived")
    fun getAll(worldId: Long, archived: Boolean = false): List<Skill>

    @Query("SELECT * FROM skill WHERE id = :skillId")
    fun get(skillId: Long): Skill

    @Insert
    fun insert(skill: Skill): Long

    @Update
    fun update(skill: Skill)
}