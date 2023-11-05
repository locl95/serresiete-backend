package com.kos.credentials.repository

import com.kos.auth.Authorization
import com.kos.credentials.User

class CredentialsInMemoryRepository(initialState: List<User> = mutableListOf()) : CredentialsRepository {
    private val users = mutableListOf<User>()

    init {
        users.addAll(initialState)
    }
    override suspend fun validateCredentials(userName: String, password: String): Boolean {
        return users.contains(User(userName, password))
    }

    override suspend fun state(): List<User> {
        return users
    }
}