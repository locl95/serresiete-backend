package com.kos.credentials

import com.kos.common.isDefined
import com.kos.credentials.repository.CredentialsRepository

class CredentialsService(private val credentialsRepository: CredentialsRepository) {
    suspend fun validateCredentials(credentials: Credentials): Boolean =
       credentialsRepository.getCredentials(credentials.userName)?.takeIf { it.password == credentials.password }.isDefined()
}