package com.kos.auth.repository

import arrow.core.Either
import com.kos.auth.Authorization
import com.kos.auth.TokenError
import com.kos.common.WithState

interface AuthRepository : WithState<List<Authorization>> {
    suspend fun insertToken(userName: String): Authorization?
    suspend fun deleteToken(token: String): Boolean
    suspend fun validateToken(token: String): Either<TokenError, String>
}