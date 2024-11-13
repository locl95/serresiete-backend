package com.kos.roles.repository

import com.kos.activities.Activity
import com.kos.common.InMemoryRepository
import com.kos.roles.Role

class RolesActivitiesInMemoryRepository : RolesActivitiesRepository, InMemoryRepository {
    private val rolesActivities = mutableMapOf<Role, Set<Activity>>()

    override suspend fun state(): Map<Role, Set<Activity>> {
        return rolesActivities
    }

    override suspend fun withState(initialState: Map<Role, Set<Activity>>): RolesActivitiesRepository {
        rolesActivities.putAll(initialState)
        return this
    }

    override suspend fun getActivitiesFromRole(role: Role): Set<Activity> {
        return rolesActivities[role].orEmpty().toSet()
    }

    override suspend fun updateActivitiesFromRole(role: Role, activities: Set<Activity>) {
        rolesActivities[role] = activities
    }

    override fun clear() {
        rolesActivities.clear()
    }
}