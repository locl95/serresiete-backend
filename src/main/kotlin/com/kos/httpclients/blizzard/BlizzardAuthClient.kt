package com.kos.httpclients.blizzard

import arrow.core.Either
import com.kos.common.HttpError
import com.kos.httpclients.domain.TokenResponse

interface BlizzardAuthClient {
    suspend fun getAccessToken(): Either<HttpError, TokenResponse>
}