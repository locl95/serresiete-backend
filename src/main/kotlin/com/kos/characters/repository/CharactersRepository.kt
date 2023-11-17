package com.kos.characters.repository

import com.kos.characters.Character
import com.kos.characters.CharacterRequest
import com.kos.common.Repository
import com.kos.common.WithState

interface CharactersRepository: Repository, WithState<List<Character>, CharactersRepository> {

    //TODO: insert should be on conflict do nothing so we can avoid the select all + diff on service
    suspend fun insert(characters: List<CharacterRequest>): List<Character>
    suspend fun get(id: Long): Character?
    suspend fun get(): List<Character>
}