package com.kos.credentials.repository

import com.kos.common.WithState
import com.kos.credentials.Credentials

interface CredentialsRepository : WithState<List<Credentials>> {
    suspend fun getCredentials(userName: String): Credentials?

}