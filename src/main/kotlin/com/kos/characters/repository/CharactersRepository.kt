package com.kos.characters.repository

import com.kos.characters.Character
import com.kos.characters.CharacterRequest
import com.kos.common.WithState

interface CharactersRepository: WithState<List<Character>> {
    suspend fun insert(character: CharacterRequest): Character?
    suspend fun get(id: Long): Character?
}