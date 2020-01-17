package com.github.alexmelyon.master_charlist.room

import com.github.alexmelyon.master_charlist.list_characters.CharacterItem
import java.util.*

class CharacterStorage {
    fun getSummaryHP(character: GameCharacter): Int {

//        val hp = db.hpDiffDao().getAllByCharacter(world.id, game.id, character.id)
//            .filter { closedSessions.contains(it.sessionGroup) }
//            .sumBy { it.value }
        return 0
    }

    fun getSummaryEffects(character: GameCharacter): List<String> {

//        val effects = db.effectDao().getAll(world.id)
//        val closedEffectDiffs = db.effectDiffDao().getAllByCharacter(world.id, game.id, character.id)
//            .filter { it.sessionGroup in closedSessions }
//        val effectDiffs = getUsedEffectsFor(closedEffectDiffs, effects)
//        val effectDiffNames = effectDiffs.map { it.name }
        return listOf()
    }

    fun getSummarySkills(): List<String> {

//            val skillIdToModifier = db.effectDiffDao().getAllByCharacter(world.id, game.id, character.id)
//                .filter { it.sessionGroup in closedSessions }
//                .map { it.effectGroup to if(it.value) +1 else -1 }
//                .groupBy { it.first }
//                .map { it.key to it.value.sumBy { it.second } }
//                .flatMap { (effectId, amount) ->
//                    val effectSkills = db.effectSkillDao().getAllByEffect(world.id, effectId)
//                        .map { db.skillDao().get(it.skillGroup) to it.value }
//                    effectSkills.map { it.first to it.second * amount }
//                }.groupBy { it.first }
//                .map { it.key.id to it.value.sumBy { it.second } }
//                .toMap()
//
//            val skills = db.skillDao().getAll(world.id)
//            data class SkillToValue(val skill: Skill, val value: Int)
//            val skillDiffs = db.skillDiffDao().getAllByCharacter(world.id, game.id, character.id)
//                .asSequence()
//                .filter { closedSessions.contains(it.sessionGroup) }
//                .map { skill -> SkillToValue(skills.single { it.id == skill.skillGroup },skill.value) }
//                .toMutableList()
//                .apply {
//                    val existingSkillIds = this.map { it.skill.id }
//                    val missedSkills = skillIdToModifier.filter { it.key !in existingSkillIds }
//                    addAll(missedSkills.map { missed -> SkillToValue(skills.single { it.id == missed.key }, 0) })
//                }
//                .groupBy { it.skill }
//                .map { SkillToValue(it.key, it.value.sumBy { it.value }) }
//                .map { SkillValueModifier(it.skill, it.value, skillIdToModifier[it.skill.id] ?: 0) }
//                .filter { it.value != 0 || it.modifier != 0 }
//                .toList()
//            val skillDiffNames = skillDiffs.map {
//                if(it.modifier == 0) {
//                    "%s: %d".format(it.skill.name, it.value)
//                } else {
//                    "%s: %d (%+d) %d".format(
//                        it.skill.name,
//                        it.value,
//                        it.modifier,
//                        it.value + it.modifier
//                    )
//                }
//            }
        return listOf()
    }

    fun getSummary(game: Game, onSuccess: (List<CharacterItem>) -> Unit) {
//        val characters = db.characterDao().getAll(world.id, game.id)
//        val closedSessions = db.gameSessionDao().getAll(world.id, game.id)
//            .filterNot { it.open }
//            .map { it.id }
//        characters.forEach { character ->
//            val hp = App.instance.characterStorage.getSummaryHP(character)
//
//            val effectDiffNames = App.instance.characterStorage.getSummaryEffects(character)
//
//            val skillDiffNames = App.instance.characterStorage.getSummarySkills()
//
//            val things = db.thingDao().getAll(world.id)
//            // TODO Refactor this boilerplate
//            val thingDiffs = db.thingDiffDao().getAllByCharacter(world.id, game.id, character.id)
//                .asSequence()
//                .filter { closedSessions.contains(it.sessionGroup) }
//                .map { thing -> things.single { it.id == thing.thingGroup } to thing.value }
//                .groupBy { it.first }
//                .map { it.key to it.value.sumBy { it.second } }
//                .filter { it.second != 0 }
//                .toList()
//            val thingDiffNames = thingDiffs.map { it.first.name to it.second }
//
//            val lastUsed = (skillDiffs.map { it.skill.lastUsed } + thingDiffs.map { it.first.lastUsed })
//                .min() ?: Calendar.getInstance().time
//            characterItems.add(CharacterItem(character, hp, lastUsed, effectDiffNames, skillDiffNames, thingDiffNames))
//        }
        onSuccess(listOf())
    }
}