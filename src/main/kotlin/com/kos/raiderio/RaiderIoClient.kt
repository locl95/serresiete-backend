package com.kos.raiderio

import arrow.core.Either
import com.kos.characters.Character
import com.kos.characters.CharacterRequest
import com.kos.common.HttpError

interface RaiderIoClient {
    suspend fun get(character: Character): Either<HttpError, RaiderIoResponse>

    suspend fun exists(characterRequest: CharacterRequest): Boolean

    suspend fun cutoff(): Either<HttpError, RaiderIoCutoff>
}