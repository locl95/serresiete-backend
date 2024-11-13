package com.kos.roles

import RolesRepository
import com.kos.activities.Activity
import com.kos.activities.ActivityRequest
import com.kos.roles.repository.RolesActivitiesRepository

class RolesService(
    private val rolesRepository: RolesRepository,
    private val rolesActivitiesRepository: RolesActivitiesRepository
) {
    suspend fun getRoles(): List<Role> = rolesRepository.getRoles()
    suspend fun deleteRole(role: Role) =
        rolesRepository.deleteRole(role)
    suspend fun addActivityToRole(activityRequest: ActivityRequest, role: Role) =
        rolesActivitiesRepository.insertActivityToRole(activityRequest.activity, role)
    suspend fun removeActivityFromRole(activity: Activity, role: Role) =
        rolesActivitiesRepository.deleteActivityFromRole(activity, role)

    suspend fun getRole(role: Role): Pair<Role, Set<Activity>> =
        Pair(role, rolesActivitiesRepository.getActivitiesFromRole(role))

    suspend fun addActivitiesToRole(role: Role, activities: Set<Activity>) {
        rolesActivitiesRepository.insertActivitiesToRole(role, activities)
    }

}