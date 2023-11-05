package com.kos.credentials

import com.kos.credentials.repository.CredentialsRepository

class CredentialsService(private val credentialsRepository: CredentialsRepository) {
    suspend fun validateCredentials(userName: String, password: String): Boolean = credentialsRepository.validateCredentials(userName, password)
}