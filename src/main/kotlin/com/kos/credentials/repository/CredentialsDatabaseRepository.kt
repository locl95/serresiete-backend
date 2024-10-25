package com.kos.credentials.repository

import com.kos.common.DatabaseFactory
import com.kos.credentials.Credentials
import com.kos.credentials.CredentialsRole
import com.kos.roles.Role
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

class CredentialsDatabaseRepository(private val db: Database) : CredentialsRepository {
    override suspend fun withState(initialState: CredentialsRepositoryState): CredentialsDatabaseRepository {
        newSuspendedTransaction(Dispatchers.IO, db) {
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
        return newSuspendedTransaction(Dispatchers.IO, db) {
            Users.select { Users.userName.eq(userName) }.map { resultRowToUser(it) }.singleOrNull()
        }
    }

    override suspend fun insertCredentials(credentials: Credentials): Unit {
        newSuspendedTransaction(Dispatchers.IO, db) {
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

    override suspend fun editCredentials(userName: String, newPassword: String) {
        newSuspendedTransaction(Dispatchers.IO, db) {
            Users.update({ Users.userName.eq(userName) }) {
                it[password] = newPassword
            }
        }
    }

    override suspend fun getUserRoles(userName: String): List<Role> {
        return newSuspendedTransaction(Dispatchers.IO, db) {
            CredentialsRoles.select { CredentialsRoles.userName.eq(userName) }
                .map { resultRowToCredentialsRoles(it).role }
        }
    }

    override suspend fun insertRole(userName: String, role: Role) {
        newSuspendedTransaction(Dispatchers.IO, db) {
            CredentialsRoles.insert {
                it[CredentialsRoles.userName] = userName
                it[CredentialsRoles.role] = role
            }
        }
    }

    override suspend fun deleteRole(userName: String, role: String) {
        newSuspendedTransaction(Dispatchers.IO, db) {
            CredentialsRoles.deleteWhere { CredentialsRoles.role.eq(role) and CredentialsRoles.userName.eq(userName) }
        }
    }

    override suspend fun deleteCredentials(user: String) {
        DatabaseFactory.dbQuery {
            Users.deleteWhere { userName.eq(user) }
        }
    }

    override suspend fun state(): CredentialsRepositoryState {
        return newSuspendedTransaction(Dispatchers.IO, db) {
            CredentialsRepositoryState(
                Users.selectAll().map { resultRowToUser(it) },
                CredentialsRoles.selectAll().map { resultRowToCredentialsRoles(it) }.groupBy { it.userName }
                    .mapValues { it.value.map { cr -> cr.role } }
            )
        }
    }
}