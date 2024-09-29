package com.kos.characters

import com.kos.characters.CharactersTestHelper.basicCharacter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CharactersDomainTest {

    @Test
    fun `i can find every wow spec`() {
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

    @Test
    fun `toCharacter should create a Character with the correct properties`() {
        val wowCharacterRequest = WowCharacterRequest("Gandalf", "Middle Earth", "Rivendell")
        val character = wowCharacterRequest.toCharacter(1L)
        assertEquals(1L, character.id)
        assertEquals("Gandalf", character.name)
        assertEquals("Middle Earth", character.region)
        assertEquals("Rivendell", character.realm)
    }

    @Test
    fun `same should return true for identical characters`() {
        val wowCharacterRequest = WowCharacterRequest("Aragorn", "Middle Earth", "Gondor")
        val character = wowCharacterRequest.toCharacter(2L)
        val result = wowCharacterRequest.same(character)
        assertTrue(result)
    }

    @Test
    fun `same should return false for characters with different properties`() {
        val wowCharacterRequest = WowCharacterRequest("Legolas", "Middle Earth", "Lothlorien")
        val character = wowCharacterRequest.toCharacter(3L)
        val result = wowCharacterRequest.same(character.copy(name = "DifferentName"))
        assertFalse(result)
    }
}