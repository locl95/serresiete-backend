package com.kos.clients.blizzard

import arrow.core.Either
import com.kos.common.HttpError
import com.kos.clients.domain.GetWowCharacterResponse
import com.kos.clients.domain.GetWowEquipmentResponse
import com.kos.clients.domain.GetWowMediaResponse
import com.kos.clients.domain.GetWowRealmResponse

interface BlizzardClient {
    suspend fun getCharacterProfile(region: String, realm: String, character: String): Either<HttpError, GetWowCharacterResponse>
    suspend fun getCharacterMedia(region: String, realm: String, character: String): Either<HttpError, GetWowMediaResponse>
    suspend fun getCharacterEquipment(region: String, realm: String, character: String): Either<HttpError, GetWowEquipmentResponse>
    suspend fun getItemMedia(region: String, realm: String, character: String): Either<HttpError, GetWowMediaResponse>
    suspend fun getRealm(region: String, id: Long): Either<HttpError, GetWowRealmResponse>
}