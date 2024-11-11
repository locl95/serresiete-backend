package com.kos.credentials.repository

import com.kos.common.InMemoryRepository
import com.kos.credentials.Credentials
import com.kos.roles.Role

class CredentialsInMemoryRepository : CredentialsRepository, InMemoryRepository {
    private val users = mutableListOf<Credentials>()
    private val userRoles = mutableMapOf<String, List<Role>>()
    override suspend fun getCredentials(): List<Credentials> {
        return users
    }

    override suspend fun getCredentials(userName: String): Credentials? {
        return users.find { it.userName == userName }
    }

    override suspend fun insertCredentials(userName: String, password: String) {
        users.add(Credentials(userName, password))
    }

    override suspend fun editCredentials(userName: String, newPassword: String) {
        val index = users.map { it.userName }.indexOf(userName)
        users[index] = Credentials(userName, newPassword)
    }

    override suspend fun getUserRoles(userName: String): List<Role> =
        userRoles[userName].orEmpty()

    override suspend fun insertRole(userName: String, role: Role) {
        userRoles.compute(userName) { _, currentRoles -> (currentRoles ?: mutableListOf()) + role }
    }

    override suspend fun insertRoles(userName: String, roles: Set<Role>) {
        userRoles.compute(userName) { _, currentRoles -> (currentRoles ?: mutableListOf()) + roles }
    }

    override suspend fun deleteRole(userName: String, role: Role) {
        userRoles.computeIfPresent(userName) { _, currentRoles ->
            currentRoles.toMutableList().apply { remove(role) }
        }
    }

    override suspend fun deleteCredentials(user: String) {
        val index = users.map { it.userName }.indexOf(user)
        users.removeAt(index)
    }

    override suspend fun state(): CredentialsRepositoryState {
        return CredentialsRepositoryState(users, userRoles)
    }

    override suspend fun withState(initialState: CredentialsRepositoryState): CredentialsInMemoryRepository {
        users.addAll(initialState.users)
        userRoles.putAll(initialState.credentialsRoles)
        return this
    }

    override fun clear() {
        users.clear()
        userRoles.clear()
    }
}