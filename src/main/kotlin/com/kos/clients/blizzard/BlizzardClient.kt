package com.kos.clients.blizzard

import arrow.core.Either
import com.kos.characters.WowCharacterRequest
import com.kos.common.HttpError
import com.kos.clients.domain.GetWowCharacterResponse

interface BlizzardClient {
    suspend fun getCharacterProfile(region: String, realm: String, character: String): Either<HttpError, GetWowCharacterResponse>
    suspend fun exists(wowCharacterRequest: WowCharacterRequest): Boolean

}