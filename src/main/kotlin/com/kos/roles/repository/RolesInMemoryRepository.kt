package com.kos.roles.repository

import RolesRepository
import com.kos.activities.Activity
import com.kos.common.InMemoryRepository
import com.kos.roles.Role

class RolesInMemoryRepository : RolesRepository, InMemoryRepository {
    private val roles = mutableListOf<Role>()

    override suspend fun getRoles(): List<Role> {
        return roles
    }

    override suspend fun insertRole(role: Role) {
        roles.add(role)
    }

    override suspend fun deleteRole(role: Role) {
        val index = roles.indexOf(role)
        roles.removeAt(index)
    }

    override suspend fun state(): List<Role> {
        return roles
    }

    override suspend fun withState(initialState: List<Activity>): RolesRepository {
        roles.addAll(initialState)
        return this
    }

    override fun clear() {
        roles.clear()
    }
}