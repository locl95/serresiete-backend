package com.kos.activities.repository

import com.kos.activities.Activity
import com.kos.common.DatabaseFactory
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class ActivitiesDatabaseRepository : ActivitiesRepository {

    object Activities : Table("activities") {
        val activity = varchar("activity", 256)

        override val primaryKey = PrimaryKey(activity)
    }

    private fun resultRowToActivity(row: ResultRow): Activity = row[Activities.activity]

    override suspend fun getActivities(): List<Activity> {
        return DatabaseFactory.dbQuery {
            Activities.selectAll().map { resultRowToActivity(it) }
        }
    }

    override suspend fun insertActivity(activity: Activity) {
        DatabaseFactory.dbQuery {
            Activities.insert { it[Activities.activity] = activity }
        }
    }

    override suspend fun deleteActivity(activity: Activity) {
        DatabaseFactory.dbQuery {
            Activities.deleteWhere { Activities.activity.eq(activity) }
        }
    }

    override suspend fun state(): List<Activity> {
        return getActivities()
    }

    override suspend fun withState(initialState: List<Activity>): ActivitiesRepository {
        DatabaseFactory.dbQuery {
            Activities.batchInsert(initialState) {
                this[Activities.activity] = it
            }
        }
        return this
    }

}