package com.kos.activities.repository

import com.kos.activities.Activity
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

class ActivitiesDatabaseRepository(private val db: Database) : ActivitiesRepository {

    object Activities : Table("activities") {
        val activity = varchar("activity", 256)

        override val primaryKey = PrimaryKey(activity)
    }

    private fun resultRowToActivity(row: ResultRow): Activity = row[Activities.activity]

    override suspend fun getActivities(): Set<Activity> {
        return newSuspendedTransaction(Dispatchers.IO, db) {
            Activities.selectAll().map { resultRowToActivity(it) }.toSet()
        }
    }

    override suspend fun insertActivity(activity: Activity) {
        return newSuspendedTransaction(Dispatchers.IO, db) {
            Activities.insert { it[Activities.activity] = activity }
        }
    }

    override suspend fun deleteActivity(activity: Activity) {
        return newSuspendedTransaction(Dispatchers.IO, db) {
            Activities.deleteWhere { Activities.activity.eq(activity) }
        }
    }

    override suspend fun state(): Set<Activity> {
        return getActivities()
    }

    override suspend fun withState(initialState: Set<Activity>): ActivitiesRepository {
        newSuspendedTransaction(Dispatchers.IO, db) {
            Activities.batchInsert(initialState) {
                this[Activities.activity] = it
            }
        }
        return this
    }

}