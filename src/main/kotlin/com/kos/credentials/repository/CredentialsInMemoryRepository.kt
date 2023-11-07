package com.kos.credentials.repository

import com.kos.credentials.Activity
import com.kos.credentials.Credentials
import com.kos.credentials.Role

class CredentialsInMemoryRepository(initialState: List<Credentials> = mutableListOf()) : CredentialsRepository {
    private val users = mutableListOf<Credentials>()
    private val userRoles = mutableMapOf<String, List<Role>>()
    private val roleActivities = mutableMapOf<String, List<Activity>>()

    init {
        users.addAll(initialState)
    }

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

    override suspend fun state(): List<Credentials> {
        return users
    }
}