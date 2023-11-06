package com.kos.credentials.repository

import com.kos.common.DatabaseFactory
import com.kos.credentials.Credentials
import org.jetbrains.exposed.sql.*


class CredentialsDatabaseRepository: CredentialsRepository {
    suspend fun withState(initialState: List<Credentials>): CredentialsDatabaseRepository {
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

    private fun resultRowToUser(row: ResultRow) = Credentials(
        row[Users.userName],
        row[Users.password]
    )

    override suspend fun getCredentials(userName: String): Credentials? {
        return DatabaseFactory.dbQuery {
            Users.select { Users.userName.eq(userName) }.map {resultRowToUser(it)}.singleOrNull()
        }
    }

    override suspend fun state(): List<Credentials> {
        return Users.selectAll().map { resultRowToUser(it) }
    }
}