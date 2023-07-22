package com.kos.characters.repository

import com.kos.characters.Character
import com.kos.characters.CharacterRequest

interface CharactersRepository {
    fun insert(character: CharacterRequest): Character
    fun get(id: Long): Character?
}