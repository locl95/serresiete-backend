package com.kos.activities.repository

import com.kos.activities.Activity
import com.kos.common.WithState

interface ActivitiesRepository : WithState<List<Activity>, ActivitiesRepository> {
    suspend fun getActivities(): List<Activity>
    suspend fun insertActivity(activity: Activity): Unit
    suspend fun deleteActivity(activity: Activity): Unit
}
