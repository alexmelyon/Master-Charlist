package com.github.alexmelyon.master_charlist.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    version = 1,
    entities = [
        World::class,
        Game::class,
        Skill::class,
        Effect::class,
        Thing::class,
        GameCharacter::class,
        GameSession::class,
        HealthPointDiff::class,
        SkillDiff::class,
        ThingDiff::class,
        CommentDiff::class,
        EffectDiff::class,
        EffectSkill::class
    ]
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun worldDao(): WorldDao
    abstract fun gameDao(): GameDao
    abstract fun skillDao(): SkillDao
    abstract fun thingDao(): ThingDao
    abstract fun effectDao(): EffectDao
    abstract fun characterDao(): CharacterDao
    abstract fun gameSessionDao(): GameSessionDao
    abstract fun hpDiffDao(): HpDiffDao
    abstract fun skillDiffDao(): SkillDiffDao
    abstract fun thingDiffDao(): ThingDiffDao
    abstract fun commentDiffDao(): CommentDiffDao
    abstract fun effectDiffDao(): EffectDiffDao
    abstract fun effectSkillDao(): EffectSkillDao
}