package com.kos.credentials.repository

import com.kos.common.InMemoryRepository
import com.kos.credentials.Activity
import com.kos.credentials.Credentials
import com.kos.credentials.Role

class CredentialsInMemoryRepository : CredentialsRepository, InMemoryRepository {
    private val users = mutableListOf<Credentials>()
    private val userRoles = mutableMapOf<String, List<Role>>()
    private val roleActivities = mutableMapOf<String, List<Activity>>()

    override suspend fun getCredentials(userName: String): Credentials? {
        return users.find { it.userName == userName }
    }

    override suspend fun insertCredentials(credentials: Credentials): Unit {
        users.add(credentials)
    }

    override suspend fun getActivities(user: String): List<Activity> {
        return userRoles[user]?.flatMap { roleActivities[it].orEmpty() }.orEmpty()
    }

    override suspend fun editCredentials(userName: String, newPassword: String): Unit {
        val index = users.map { it.userName }.indexOf(userName)
        users[index] = Credentials(userName, newPassword)
    }

    override suspend fun state(): CredentialsRepositoryState {
        return CredentialsRepositoryState(users, userRoles, roleActivities)
    }

    override suspend fun withState(initialState: CredentialsRepositoryState): CredentialsInMemoryRepository {
        users.addAll(initialState.users)
        userRoles.putAll(initialState.credentialsRoles)
        roleActivities.putAll(initialState.rolesActivities)
        return this
    }

    override fun clear() {
        users.clear()
        userRoles.clear()
        roleActivities.clear()
    }
}