package com.kos.roles.repository

import com.kos.activities.Activity
import com.kos.roles.Role
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

class RolesActivitiesDatabaseRepository(private val db: Database) : RolesActivitiesRepository {

    object RoleActivities : Table("roles_activities") {
        val role = (varchar("role", 48))
        val activity = varchar("activity", 128)

        override val primaryKey = PrimaryKey(role, activity)
    }

    override suspend fun state(): Map<Role, Set<Activity>> {
        return newSuspendedTransaction(Dispatchers.IO, db) {
            RoleActivities.selectAll().groupBy(
                keySelector = { Role.fromString(it[RoleActivities.role]) },
                valueTransform = { it[RoleActivities.activity] }
            ).mapValues { (_, activities) ->
                activities.toSet()
            }
        }
    }

    override suspend fun withState(initialState: Map<Role, Set<Activity>>): RolesActivitiesRepository {
        newSuspendedTransaction(Dispatchers.IO, db) {
            RoleActivities.batchInsert(initialState.flatMap { (role, activities) ->
                activities.map {
                    Pair(role, it)
                }
            }) {
                this[RoleActivities.role] = it.first.toString()
                this[RoleActivities.activity] = it.second
            }
        }
        return this
    }

    override suspend fun insertActivityToRole(activity: Activity, role: Role) {
        newSuspendedTransaction(Dispatchers.IO, db) {
            RoleActivities.insert {
                it[this.role] = role.toString()
                it[this.activity] = activity
            }
        }
    }

    override suspend fun deleteActivityFromRole(activity: Activity, role: Role) {
        newSuspendedTransaction(Dispatchers.IO, db) {
            RoleActivities.deleteWhere {
                (RoleActivities.role eq role.toString()) and (RoleActivities.activity eq activity)
            }
        }
    }

    override suspend fun getActivitiesFromRole(role: Role): Set<Activity> {
        return transaction {
            RoleActivities.select { RoleActivities.role eq role.toString() }.map {
                it[RoleActivities.activity]
            }.toSet()
        }
    }

    override suspend fun insertActivitiesToRole(role: Role, activities: Set<Activity>) {
        newSuspendedTransaction(Dispatchers.IO, db) {
            RoleActivities.batchInsert(activities) {
                this[RoleActivities.role] = role.toString()
                this[RoleActivities.activity] = it
            }
        }
    }
}