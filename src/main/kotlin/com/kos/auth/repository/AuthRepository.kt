package com.kos.auth.repository

import arrow.core.Either
import com.kos.auth.Authorization
import com.kos.common.InsertError
import com.kos.common.WithState

interface AuthRepository : WithState<List<Authorization>, AuthRepository> {
    suspend fun insertToken(userName: String, token: String, isAccess: Boolean): Either<InsertError, Authorization?>
    suspend fun deleteTokensFromUser(userName: String): Boolean
    suspend fun getAuthorization(token:String): Authorization?
    suspend fun deleteExpiredTokens(): Int
}