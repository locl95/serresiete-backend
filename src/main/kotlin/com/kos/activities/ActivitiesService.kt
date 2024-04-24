package com.kos.activities

import com.kos.activities.repository.ActivitiesRepository

class ActivitiesService(private val activitiesRepository: ActivitiesRepository) {
    suspend fun getActivities(): List<Activity> = activitiesRepository.getActivities()
    suspend fun createActivity(activityRequest: ActivityRequest) =
        activitiesRepository.insertActivity(activityRequest.activity)
    suspend fun deleteActivity(activityRequest: ActivityRequest) =
        activitiesRepository.deleteActivity(activityRequest.activity)
}