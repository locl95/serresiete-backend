package com.kos.credentials.repository

import com.kos.common.DatabaseFactory
import com.kos.credentials.*
import org.jetbrains.exposed.sql.*

class CredentialsDatabaseRepository : CredentialsRepository {
    override suspend fun withState(initialState: CredentialsRepositoryState): CredentialsDatabaseRepository {
        DatabaseFactory.dbQuery {
            Users.batchInsert(initialState.users) {
                this[Users.userName] = it.userName
                this[Users.password] = it.password
            }
            CredentialsRoles.batchInsert(initialState.credentialsRoles.flatMap { (userName, roles) ->
                roles.map { Pair(userName, it) }
            }) {
                this[CredentialsRoles.userName] = it.first
                this[CredentialsRoles.role] = it.second
            }
            RolesActivities.batchInsert(initialState.rolesActivities.flatMap { (role, activities) ->
                activities.map { Pair(role, it) }
            }) {
                this[RolesActivities.role] = it.first
                this[RolesActivities.activity] = it.second
            }
        }
        return this
    }

    object Users : Table() {
        val userName = varchar("user_name", 48)
        val password = varchar("password", 128)

        override val primaryKey = PrimaryKey(userName)
    }

    private fun resultRowToUser(row: ResultRow) = Credentials(
        row[Users.userName],
        row[Users.password]
    )

    override suspend fun getCredentials(userName: String): Credentials? {
        return DatabaseFactory.dbQuery {
            Users.select { Users.userName.eq(userName) }.map { resultRowToUser(it) }.singleOrNull()
        }
    }

    override suspend fun insertCredentials(credentials: Credentials): Unit {
        DatabaseFactory.dbQuery {
            Users.insert {
                it[userName] = credentials.userName
                it[password] = credentials.password
            }
        }
    }

    object CredentialsRoles : Table("credentials_roles") {
        val userName = varchar("user_name", 48)
        val role = varchar("role", 48)

        override val primaryKey = PrimaryKey(userName, role)
    }

    private fun resultRowToCredentialsRoles(row: ResultRow) = CredentialsRole(
        row[CredentialsRoles.userName],
        row[CredentialsRoles.role]
    )

    object RolesActivities : Table("roles_activities") {
        val role = varchar("role", 48)
        val activity = varchar("activity", 128)

        override val primaryKey = PrimaryKey(role, activity)
    }

    private fun resultRowToRolesActivities(row: ResultRow) = RoleActivity(
        row[RolesActivities.role],
        row[RolesActivities.activity]
    )

    override suspend fun getActivities(user: String): List<Activity> {
        return DatabaseFactory.dbQuery {
            CredentialsRoles.join(RolesActivities, JoinType.INNER, null, null) {
                CredentialsRoles.role eq RolesActivities.role
            }.select { CredentialsRoles.userName.eq(user) }.map { it[RolesActivities.activity] }
        }
    }

    override suspend fun editCredentials(userName: String, newPassword: String) {
        DatabaseFactory.dbQuery {
            Users.update({ Users.userName.eq(userName) }) {
                it[password] = newPassword
            }
        }
    }

    override suspend fun getRoles(userName: String): List<Role> {
        return DatabaseFactory.dbQuery {
            CredentialsRoles.select { CredentialsRoles.userName.eq(userName) }
                .map { resultRowToCredentialsRoles(it).role }
        }
    }

    override suspend fun state(): CredentialsRepositoryState {
        return DatabaseFactory.dbQuery {
            CredentialsRepositoryState(
                Users.selectAll().map { resultRowToUser(it) },
                CredentialsRoles.selectAll().map { resultRowToCredentialsRoles(it) }.groupBy { it.userName }
                    .mapValues { it.value.map { cr -> cr.role } },
                RolesActivities.selectAll().map { resultRowToRolesActivities(it) }.groupBy { it.role }
                    .mapValues { it.value.map { cr -> cr.activity } }
            )
        }
    }
}