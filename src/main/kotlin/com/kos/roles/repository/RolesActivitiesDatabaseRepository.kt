package com.kos.roles.repository

import com.kos.activities.Activity
import com.kos.common.DatabaseFactory
import com.kos.roles.Role
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

class RolesActivitiesDatabaseRepository : RolesActivitiesRepository {

    object RoleActivities : Table("roles_activities") {
        val role = (varchar("role", 48))
        val activity = varchar("activity", 128)

        override val primaryKey = PrimaryKey(role, activity)
    }

    override suspend fun state(): Map<Role, List<Activity>> {
        return DatabaseFactory.dbQuery {
            RoleActivities.selectAll().groupBy(
                keySelector = { it[RoleActivities.role] },
                valueTransform = { it[RoleActivities.activity] }
            )
        }
    }

    override suspend fun withState(initialState: Map<Role, List<Activity>>): RolesActivitiesRepository {
        DatabaseFactory.dbQuery {
            RoleActivities.batchInsert(initialState.flatMap { (role, activities) ->
                activities.map {
                    Pair(role, it)
                }
            }) {
                this[RoleActivities.role] = it.first
                this[RoleActivities.activity] = it.second
            }
        }
        return this
    }

    override suspend fun insertActivityToRole(activity: Activity, role: Role) {
        transaction {
            RoleActivities.insert {
                it[this.role] = role
                it[this.activity] = activity
            }
        }
    }

    override suspend fun deleteActivityFromRole(activity: Activity, role: Role) {
        transaction {
            RoleActivities.deleteWhere {
                (RoleActivities.role eq role) and (RoleActivities.activity eq activity)
            }
        }
    }

    override suspend fun getActivitiesFromRole(role: Role): Set<Activity> {
        return transaction {
            RoleActivities.select { RoleActivities.role eq role }.map {
                it[RoleActivities.activity]
            }.toSet()
        }
    }
}