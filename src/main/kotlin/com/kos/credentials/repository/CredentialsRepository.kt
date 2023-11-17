package com.kos.credentials.repository

import com.kos.common.Repository
import com.kos.common.WithState
import com.kos.credentials.Activity
import com.kos.credentials.Credentials
import com.kos.credentials.Role

interface CredentialsRepository : Repository, WithState<CredentialsRepositoryState, CredentialsRepository> {
    suspend fun getCredentials(userName: String): Credentials?
    suspend fun insertCredentials(credentials: Credentials): Unit
    suspend fun getActivities(user: String): List<Activity>
    suspend fun editCredentials(userName: String, newPassword: String): Unit
}

data class CredentialsRepositoryState(
    val users: List<Credentials>,
    val credentialsRoles: Map<String, List<Role>>,
    val rolesActivities: Map<Role, List<Activity>>
)