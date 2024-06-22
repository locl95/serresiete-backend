package com.kos.activities.repository

import com.kos.activities.Activity
import com.kos.common.InMemoryRepository

class ActivitiesInMemoryRepository : ActivitiesRepository, InMemoryRepository {
    private val activities = mutableListOf<Activity>()

    override suspend fun getActivities(): List<Activity> {
        return activities
    }

    override suspend fun insertActivity(activity: Activity) {
        activities.add(activity)
    }

    override suspend fun deleteActivity(activity: Activity) {
        val index = activities.indexOf(activity)
        activities.removeAt(index)
    }

    override suspend fun state(): List<Activity> {
        return activities
    }

    override suspend fun withState(initialState: List<Activity>): ActivitiesRepository {
        activities.addAll(initialState)
        return this
    }

    override fun clear() {
        activities.clear()
    }
}