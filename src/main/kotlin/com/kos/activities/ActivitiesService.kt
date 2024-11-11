package com.kos.activities

import com.kos.activities.repository.ActivitiesRepository

class ActivitiesService(private val activitiesRepository: ActivitiesRepository) {
    suspend fun getActivities(): Set<Activity> = activitiesRepository.getActivities()
}