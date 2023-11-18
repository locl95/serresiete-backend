package com.kos.characters

import com.kos.characters.CharactersTestHelper.basicCharacter
import org.junit.Test
import kotlin.test.assertEquals

class CharactersDomainTest {

    @Test
    fun ICanFindEveryClassSpecs() {
        val warriorSpecs = basicCharacter.specsWithName("Warrior").map { it.name }.toSet()
        val paladinSpecs = basicCharacter.specsWithName("Paladin").map { it.name }.toSet()
        val hunterSpecs = basicCharacter.specsWithName("Hunter").map { it.name }.toSet()
        val rogueSpecs = basicCharacter.specsWithName("Rogue").map { it.name }.toSet()
        val priestSpecs = basicCharacter.specsWithName("Priest").map { it.name }.toSet()
        val shamanSpecs = basicCharacter.specsWithName("Shaman").map { it.name }.toSet()
        val mageSpecs = basicCharacter.specsWithName("Mage").map { it.name }.toSet()
        val warlockSpecs = basicCharacter.specsWithName("Warlock").map { it.name }.toSet()
        val monkSpecs = basicCharacter.specsWithName("Monk").map { it.name }.toSet()
        val druidSpecs = basicCharacter.specsWithName("Druid").map { it.name }.toSet()
        val demonHunterSpecs = basicCharacter.specsWithName("Demon Hunter").map { it.name }.toSet()
        val deathKnightSpecs = basicCharacter.specsWithName("Death Knight").map { it.name }.toSet()
        val evokerSpecs = basicCharacter.specsWithName("Evoker").map { it.name }.toSet()

        val expectedWarriorSpecs = setOf("Protection Warrior", "Arms", "Fury")
        val expectedPaladinSpecs = setOf("Protection Paladin", "Retribution", "Holy Paladin")
        val expectedHunterSpecs = setOf("Beast Mastery", "Survival", "Marksmanship")
        val expectedRogueSpecs = setOf("Outlaw", "Assassination", "Subtlety")
        val expectedPriestSpecs = setOf("Shadow", "Holy Priest", "Discipline")
        val expectedShamanSpecs = setOf("Enhancement", "Elemental", "Restoration Shaman")
        val expectedMageSpecs = setOf("Arcane", "Fire", "Frost Mage")
        val expectedWarlockSpecs = setOf("Affliction", "Demonology", "Destruction")
        val expectedMonkSpecs = setOf("Wind Walker", "Brew Master", "Mist Weaver")
        val expectedDruidSpecs = setOf("Guardian", "Balance", "Feral", "Restoration Druid")
        val expectedDemonHunterSpecs = setOf("Havoc", "Vengeance")
        val expectedDeathKnightSpecs = setOf("Blood", "Frost Death Knight", "Unholy")
        val expectedEvokerSpecs = setOf("Devastation", "Preservation", "Augmentation")

        val specs = listOf(
            warriorSpecs,
            paladinSpecs,
            hunterSpecs,
            rogueSpecs,
            priestSpecs,
            shamanSpecs,
            mageSpecs,
            warlockSpecs,
            monkSpecs,
            druidSpecs,
            demonHunterSpecs,
            deathKnightSpecs,
            evokerSpecs
        )

        val expectedSpecs = listOf(
            expectedWarriorSpecs,
            expectedPaladinSpecs,
            expectedHunterSpecs,
            expectedRogueSpecs,
            expectedPriestSpecs,
            expectedShamanSpecs,
            expectedMageSpecs,
            expectedWarlockSpecs,
            expectedMonkSpecs,
            expectedDruidSpecs,
            expectedDemonHunterSpecs,
            expectedDeathKnightSpecs,
            expectedEvokerSpecs
        )

        expectedSpecs.zip(specs).forEach {
            assertEquals(it.first, it.second)
        }

    }
}