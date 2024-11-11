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
    suspend fun createCredentials(createCredentialRequest: CreateCredentialRequest): Unit {
        credentialsRepository.insertCredentials(
            createCredentialRequest.userName,
            password = BCrypt.hashpw(
                createCredentialRequest.password,
                BCrypt.gensalt(12)
            )
        )
        credentialsRepository.insertRoles(createCredentialRequest.userName, createCredentialRequest.roles)
    }

    suspend fun validateCredentials(credentials: Credentials): Boolean =
        credentialsRepository.getCredentials(credentials.userName)
            ?.takeIf { BCrypt.checkpw(credentials.password, it.password) }.isDefined()


    suspend fun editCredential(userName: String, request: EditCredentialRequest) {
        credentialsRepository.editCredentials(
            userName,
            BCrypt.hashpw(request.password, BCrypt.gensalt(12))
        )
        credentialsRepository.deleteRoles(userName)
        credentialsRepository.insertRoles(userName, request.roles)
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

    suspend fun patchCredential(userName: String, request: PatchCredentialRequest) {
        credentialsRepository.patch(
            userName,
            request.copy(password = request.password?.let { BCrypt.hashpw(it, BCrypt.gensalt(12)) })
        )
    }
}