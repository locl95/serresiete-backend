package com.kos.activities.repository

import com.kos.activities.Activity
import com.kos.common.InMemoryRepository

class ActivitiesInMemoryRepository : ActivitiesRepository, InMemoryRepository {
    private val activities = mutableSetOf<Activity>()

    override suspend fun getActivities(): Set<Activity> {
        return activities
    }

    override suspend fun insertActivity(activity: Activity) {
        activities.add(activity)
    }

    override suspend fun deleteActivity(activity: Activity) {
        activities.remove(activity)
    }

    override suspend fun state(): Set<Activity> {
        return activities
    }

    override suspend fun withState(initialState: Set<Activity>): ActivitiesRepository {
        activities.addAll(initialState)
        return this
    }

    override fun clear() {
        activities.clear()
    }
}