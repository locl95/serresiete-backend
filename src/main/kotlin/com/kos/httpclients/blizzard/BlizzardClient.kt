package com.kos.httpclients.blizzard

import arrow.core.Either
import com.kos.common.HttpError
import com.kos.httpclients.domain.GetWowCharacterResponse

interface BlizzardClient {
    suspend fun getCharacterProfile(region: String, realm: String, character: String, namespace: String): Either<HttpError, GetWowCharacterResponse>
}