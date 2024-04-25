package com.kos.credentials.repository

import com.kos.common.WithState
import com.kos.credentials.Credentials
import com.kos.credentials.Role

interface CredentialsRepository : WithState<CredentialsRepositoryState, CredentialsRepository> {
    suspend fun getCredentials(): List<Credentials>
    suspend fun getCredentials(userName: String): Credentials?
    suspend fun insertCredentials(credentials: Credentials): Unit
    suspend fun editCredentials(userName: String, newPassword: String): Unit
    suspend fun getUserRoles(userName: String): List<Role>
    suspend fun insertRole(userName: String, role: Role): Unit
    suspend fun deleteRole(userName: String, role: String): Unit
    suspend fun deleteCredentials(user: String): Unit
}

data class CredentialsRepositoryState(
    val users: List<Credentials>,
    val credentialsRoles: Map<String, List<Role>>,
)