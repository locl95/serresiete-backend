package com.kos.credentials

import com.kos.common.isDefined
import com.kos.credentials.repository.CredentialsRepository
import com.kos.roles.Role
import com.kos.roles.repository.RolesActivitiesRepository
import org.mindrot.jbcrypt.BCrypt

class CredentialsService(
    private val credentialsRepository: CredentialsRepository,
    private val rolesActivitiesRepository: RolesActivitiesRepository
) {
    suspend fun createCredentials(credentials: Credentials): Unit =
        credentialsRepository.insertCredentials(
            credentials.copy(
                password = BCrypt.hashpw(
                    credentials.password,
                    BCrypt.gensalt(12)
                )
            )
        )

    suspend fun validateCredentials(credentials: Credentials): Boolean =
        credentialsRepository.getCredentials(credentials.userName)
            ?.takeIf { BCrypt.checkpw(credentials.password, it.password) }.isDefined()


    suspend fun editCredentials(credentials: Credentials) {
        credentialsRepository.editCredentials(
            credentials.userName,
            BCrypt.hashpw(credentials.password, BCrypt.gensalt(12))
        )
    }

    suspend fun getUserRoles(userName: String): List<Role> = credentialsRepository.getUserRoles(userName)
    suspend fun addRoleToUser(userName: String, role: Role): Unit = credentialsRepository.insertRole(userName, role)
    suspend fun deleteRoleFromUser(userName: String, role: Role): Unit =
        credentialsRepository.deleteRole(userName, role)

    suspend fun getCredentials(): List<CredentialsWithRoles> =
        credentialsRepository.getCredentials().map {
            CredentialsWithRoles(it.userName, getUserRoles(it.userName))
        }

    suspend fun deleteCredentials(user: String) = credentialsRepository.deleteCredentials(user)
    suspend fun getRoleActivities(role: Role) = rolesActivitiesRepository.getActivitiesFromRole(role)
    suspend fun getCredential(user: String): CredentialsWithRoles? =
        credentialsRepository.getCredentials(user)?.let {
            CredentialsWithRoles(it.userName, getUserRoles(it.userName))
        }
}