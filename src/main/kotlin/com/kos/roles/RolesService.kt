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
    suspend fun createRole(roleRequest: RoleRequest) =
        rolesRepository.insertRole(roleRequest.role)
    suspend fun deleteRole(roleRequest: RoleRequest) =
        rolesRepository.deleteRole(roleRequest.role)
    suspend fun addActivityToRole(activityRequest: ActivityRequest, role: Role) =
        rolesActivitiesRepository.insertActivityToRole(activityRequest.activity, role)
    suspend fun removeActivityFromRole(activity: Activity, role: Role) =
        rolesActivitiesRepository.insertActivityToRole(activity, role)
}