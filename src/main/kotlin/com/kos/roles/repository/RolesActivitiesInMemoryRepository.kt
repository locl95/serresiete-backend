package com.kos.roles.repository

import com.kos.activities.Activity
import com.kos.common.InMemoryRepository
import com.kos.roles.Role

class RolesActivitiesInMemoryRepository : RolesActivitiesRepository, InMemoryRepository {
    override suspend fun state(): Map<Role, List<Activity>> {
        TODO("Not yet implemented")
    }

    override suspend fun withState(initialState: Map<Role, List<Activity>>): RolesActivitiesRepository {
        TODO("Not yet implemented")
    }

    override suspend fun insertActivityToRole(activity: Activity, role: Role): Unit {
        TODO("Not yet implemented")
    }

    override suspend fun deleteActivityFromRole(activity: Activity, role: Role): Unit {
        TODO("Not yet implemented")
    }

    override suspend fun getActivitiesFromRole(role: Role): Set<Activity> {
        TODO("Not yet implemented")
    }

    override fun clear() {
        TODO("Not yet implemented")
    }
}