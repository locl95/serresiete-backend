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

    override suspend fun insertActivityToRole(activity: Activity, role: Role): Unit {
        rolesActivities.compute(role) { _, currentActivities -> (currentActivities ?: mutableSetOf()) + activity }
    }

    override suspend fun deleteActivityFromRole(activity: Activity, role: Role): Unit {
        rolesActivities.computeIfPresent(role) { _, currentActivities ->
            currentActivities.toMutableSet().apply { remove(activity) }
        }
    }

    override suspend fun getActivitiesFromRole(role: Role): Set<Activity> {
        return rolesActivities[role].orEmpty().toSet()
    }

    override fun clear() {
        rolesActivities.clear()
    }
}