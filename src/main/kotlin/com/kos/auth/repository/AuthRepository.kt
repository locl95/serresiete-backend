package com.kos.auth.repository

import com.kos.auth.Authorization
import com.kos.common.Repository
import com.kos.common.WithState

interface AuthRepository : Repository, WithState<List<Authorization>> {
    suspend fun insertToken(userName: String): Authorization?
    suspend fun deleteToken(token: String): Boolean
    suspend fun getAuthorization(token:String): Authorization?
}