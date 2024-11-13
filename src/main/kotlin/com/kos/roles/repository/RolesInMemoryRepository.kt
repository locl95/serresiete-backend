package com.kos.roles.repository

import com.kos.common.InMemoryRepository
import com.kos.roles.Role

class RolesInMemoryRepository : RolesRepository, InMemoryRepository {
    private val roles = mutableListOf<Role>()

    override suspend fun getRoles(): List<Role> {
        return roles
    }

    override suspend fun state(): List<Role> {
        return roles
    }

    override suspend fun withState(initialState: List<Role>): RolesRepository {
        roles.addAll(initialState)
        return this
    }

    override fun clear() {
        roles.clear()
    }
}