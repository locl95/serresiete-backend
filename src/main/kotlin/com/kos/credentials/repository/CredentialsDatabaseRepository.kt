package com.kos.credentials.repository

import com.kos.common.DatabaseFactory
import com.kos.credentials.User
import org.jetbrains.exposed.sql.*


class CredentialsDatabaseRepository: CredentialsRepository {
    suspend fun withState(initialState: List<User>): CredentialsDatabaseRepository {
        DatabaseFactory.dbQuery {
            Users.batchInsert(initialState) {
                this[Users.userName] = it.userName
                this[Users.password] = it.password
            }
        }
        return this
    }

    object Users : Table() {
        val userName = varchar("user_name", 48)
        val password = varchar("password", 48)

        override val primaryKey = PrimaryKey(userName)
    }

    private fun resultRowToUser(row: ResultRow) = User(
        row[Users.userName],
        row[Users.password]
    )
    override suspend fun validateCredentials(userName: String, password: String): Boolean {
        return when (DatabaseFactory.dbQuery {
            Users.select { Users.userName.eq(userName) and Users.password.eq(password) }.singleOrNull()
        }) {
            null -> false
            else -> true
        }
    }

    override suspend fun state(): List<User> {
        return Users.selectAll().map { resultRowToUser(it) }
    }
}