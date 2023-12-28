package com.kos.credentials

import com.kos.common.isDefined
import com.kos.credentials.repository.CredentialsRepository
import io.ktor.server.auth.*
import org.mindrot.jbcrypt.BCrypt

class CredentialsService(private val credentialsRepository: CredentialsRepository) {
    suspend fun createCredentials(credentials: Credentials): Unit =
        credentialsRepository.insertCredentials(credentials.copy(password = BCrypt.hashpw(credentials.password, BCrypt.gensalt(12))))

    suspend fun validateCredentials(credentials: Credentials): Boolean =
       credentialsRepository.getCredentials(credentials.userName)?.takeIf { BCrypt.checkpw(credentials.password, it.password) }.isDefined()

    suspend fun hasPermissions(user: String, requiredActivity: String): Boolean =
        credentialsRepository.getActivities(user).contains(requiredActivity)

    suspend fun editCredentials(credentials: Credentials) {
        credentialsRepository.editCredentials(credentials.userName, BCrypt.hashpw(credentials.password, BCrypt.gensalt(12)))
    }

    suspend fun getRoles(userName: String): List<Role> = credentialsRepository.getRoles(userName)
}