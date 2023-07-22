package com.kos.raiderio

import arrow.core.Either
import com.kos.characters.Character
import com.kos.common.JsonParseError

interface RaiderIoClient {
    suspend fun get(character: Character): Either<JsonParseError, RaiderIoResponse>
    suspend fun cutoff(): Either<JsonParseError, RaiderIoCutoff>
}