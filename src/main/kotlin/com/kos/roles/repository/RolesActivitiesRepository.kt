package com.kos.roles.repository

import com.kos.activities.Activity
import com.kos.common.WithState
import com.kos.roles.Role

interface RolesActivitiesRepository : WithState<Map<Role, Set<Activity>>, RolesActivitiesRepository> {
    suspend fun getActivitiesFromRole(role: Role): Set<Activity>
    suspend fun updateActivitiesFromRole(role: Role, activities: Set<Activity>)


}