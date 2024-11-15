package com.kos.clients.blizzard

import arrow.core.Either
import com.kos.common.HttpError
import com.kos.clients.domain.TokenResponse

interface BlizzardAuthClient {
    suspend fun getAccessToken(): Either<HttpError, TokenResponse>
}