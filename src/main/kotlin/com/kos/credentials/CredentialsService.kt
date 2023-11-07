package com.kos.credentials

import com.kos.common.isDefined
import com.kos.credentials.repository.CredentialsRepository

class CredentialsService(private val credentialsRepository: CredentialsRepository) {
    suspend fun createCredentials(credentials: Credentials): Unit =
        credentialsRepository.insertCredentials(credentials)

    suspend fun validateCredentials(credentials: Credentials): Boolean =
       credentialsRepository.getCredentials(credentials.userName)?.takeIf { it.password == credentials.password }.isDefined()

    suspend fun hasPermissions(user: String, requiredActivity: String): Boolean =
        credentialsRepository.getActivities(user).contains(requiredActivity)

}