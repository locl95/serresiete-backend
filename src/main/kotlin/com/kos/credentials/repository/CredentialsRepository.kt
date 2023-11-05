package com.kos.credentials.repository

import com.kos.common.WithState
import com.kos.credentials.User

interface CredentialsRepository : WithState<List<User>> {
    suspend fun validateCredentials(userName: String, password: String): Boolean

}