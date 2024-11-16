package com.kos.clients.raiderio

import arrow.core.Either
import com.kos.characters.WowCharacter
import com.kos.characters.WowCharacterRequest
import com.kos.common.HttpError
import com.kos.clients.domain.RaiderIoCutoff
import com.kos.clients.domain.RaiderIoResponse

interface RaiderIoClient {
    suspend fun get(wowCharacter: WowCharacter): Either<HttpError, RaiderIoResponse>

    suspend fun exists(wowCharacterRequest: WowCharacterRequest): Boolean

    suspend fun cutoff(): Either<HttpError, RaiderIoCutoff>
}