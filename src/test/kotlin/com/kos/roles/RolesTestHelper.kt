package com.kos.roles

import com.kos.activities.ActivitiesTestHelper.basicActivity

object RolesTestHelper {
    val role = "role"
    val basicRolesActivities = mapOf(Pair(role, setOf(basicActivity)))
}