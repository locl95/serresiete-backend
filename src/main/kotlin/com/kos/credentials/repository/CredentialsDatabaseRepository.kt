package com.kos.credentials.repository

import com.kos.activities.Activity
import com.kos.common.DatabaseFactory
import com.kos.credentials.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

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

    override suspend fun getCredentials(): List<Credentials> {
        return Users.selectAll().map { resultRowToUser(it) }
    }

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

    override suspend fun editCredentials(userName: String, newPassword: String) {
        DatabaseFactory.dbQuery {
            Users.update({ Users.userName.eq(userName) }) {
                it[password] = newPassword
            }
        }
    }

    override suspend fun getUserRoles(userName: String): List<Role> {
        return DatabaseFactory.dbQuery {
            CredentialsRoles.select { CredentialsRoles.userName.eq(userName) }
                .map { resultRowToCredentialsRoles(it).role }
        }
    }

    override suspend fun getRoles(): Set<Role> {
        return DatabaseFactory.dbQuery {
            CredentialsRoles.selectAll()
                .map { resultRowToCredentialsRoles(it).role }.toSet()
        }
    }

    override suspend fun insertRole(userName: String, role: Role) {
        DatabaseFactory.dbQuery {
            CredentialsRoles.insert {
                it[CredentialsRoles.userName] = userName
                it[CredentialsRoles.role] = role
            }
        }
    }

    override suspend fun deleteRole(userName: String, role: String) {
        DatabaseFactory.dbQuery {
            CredentialsRoles.deleteWhere { CredentialsRoles.role.eq(role) and CredentialsRoles.userName.eq(userName) }
        }
    }

    override suspend fun deleteCredentials(user: String) {
        DatabaseFactory.dbQuery {
            Users.deleteWhere { userName.eq(user) }
        }
    }

    override suspend fun state(): CredentialsRepositoryState {
        return DatabaseFactory.dbQuery {
            CredentialsRepositoryState(
                Users.selectAll().map { resultRowToUser(it) },
                CredentialsRoles.selectAll().map { resultRowToCredentialsRoles(it) }.groupBy { it.userName }
                    .mapValues { it.value.map { cr -> cr.role } }
            )
        }
    }
}