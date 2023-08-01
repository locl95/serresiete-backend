package com.kos.auth.repository

import arrow.core.Either
import com.kos.auth.Authorization
import com.kos.auth.TokenNotFound
import com.kos.auth.User
import com.kos.common.WithState

interface AuthRepository : WithState<Pair<List<User>, List<Authorization>>> {
    suspend fun insertToken(userName: String): Authorization?
    suspend fun deleteToken(token: String): Boolean
    suspend fun validateCredentials(userName: String, password: String): Boolean
    suspend fun validateToken(token: String): Either<TokenNotFound, String>
}