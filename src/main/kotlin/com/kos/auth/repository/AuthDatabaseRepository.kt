package com.kos.auth.repository

import arrow.core.Either
import com.kos.auth.Authorization
import com.kos.auth.TokenNotFound
import com.kos.auth.User
import com.kos.common.DatabaseFactory.dbQuery
import com.kos.views.SimpleView
import com.kos.views.repository.ViewsDatabaseRepository
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.time.OffsetDateTime
import java.util.*

class AuthDatabaseRepository : AuthRepository {

    suspend fun withState(initialState: Pair<List<User>, List<Authorization>>): AuthDatabaseRepository {
        dbQuery {
            Users.batchInsert(initialState.first) {
                this[Users.userName] = it.userName
                this[Users.password] = it.password
            }
            Authorizations.batchInsert(initialState.second) {
                this[Authorizations.userName] = it.userName
                this[Authorizations.token] = it.token
                this[Authorizations.lastUsed] = it.lastUsed.toString()
                this[Authorizations.validUntil] = it.validUntil.toString()
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

    object Authorizations : Table() {
        val userName = varchar("user_name", 48)
        val token = varchar("token", 128)
        val lastUsed = text("last_used")
        val validUntil = text("valid_until")

        override val primaryKey = PrimaryKey(token)
    }

    private fun resultRowToAuthorization(row: ResultRow) = Authorization(
        row[Authorizations.userName],
        row[Authorizations.token],
        OffsetDateTime.parse(row[Authorizations.lastUsed]),
        OffsetDateTime.parse(row[Authorizations.validUntil])
    )

    override suspend fun insertToken(userName: String): Authorization? =
        dbQuery {
            val insertStatement = Authorizations.insert {
                it[Authorizations.userName] = userName
                it[token] = UUID.randomUUID().toString()
                it[lastUsed] = OffsetDateTime.now().toString()
                it[validUntil] = OffsetDateTime.now().plusHours(24).toString()
            }

            insertStatement.resultedValues?.singleOrNull()?.let { resultRowToAuthorization(it) }
        }

    override suspend fun deleteToken(token: String): Boolean {
        dbQuery {
            Authorizations.deleteWhere { Authorizations.token.eq(token) }
        }
        return true
    }

    override suspend fun validateCredentials(userName: String, password: String): Boolean =
        when (dbQuery {
            Users.select { Users.userName.eq(userName) and Users.password.eq(password) }.singleOrNull()
        }) {
            null -> false
            else -> true
        }

    override suspend fun validateToken(token: String): Either<TokenNotFound, String> =
        when (val maybeAuthorization = dbQuery {
            Authorizations.select { Authorizations.token eq token }.singleOrNull()
        }?.let { resultRowToAuthorization(it) }) {
            null -> Either.Left(TokenNotFound(token))
            else -> Either.Right(maybeAuthorization.userName)
        }

    override suspend fun state(): Pair<List<User>, List<Authorization>> {
        return dbQuery {
            Pair(
                Users.selectAll().map { resultRowToUser(it) },
                Authorizations.selectAll().map { resultRowToAuthorization(it) }
            )
        }
    }
}