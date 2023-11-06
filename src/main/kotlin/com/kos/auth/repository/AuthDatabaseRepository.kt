package com.kos.auth.repository

import com.kos.auth.Authorization
import com.kos.common.DatabaseFactory.dbQuery
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.time.OffsetDateTime
import java.util.*

class AuthDatabaseRepository : AuthRepository {

    private val hoursBeforeExpiration = 24L

    suspend fun withState(initialState: List<Authorization>): AuthDatabaseRepository {
        dbQuery {
            Authorizations.batchInsert(initialState) {
                this[Authorizations.userName] = it.userName
                this[Authorizations.token] = it.token
                this[Authorizations.lastUsed] = it.lastUsed.toString()
                this[Authorizations.validUntil] = it.validUntil?.toString()
            }
        }
        return this
    }

    object Authorizations : Table() {
        val userName = varchar("user_name", 48)
        val token = varchar("token", 128)
        val lastUsed = text("last_used")
        val validUntil = text("valid_until").nullable()

        override val primaryKey = PrimaryKey(token)
    }

    private fun resultRowToAuthorization(row: ResultRow) = Authorization(
        row[Authorizations.userName],
        row[Authorizations.token],
        OffsetDateTime.parse(row[Authorizations.lastUsed]),
        row[Authorizations.validUntil]?.let {
            OffsetDateTime.parse(it)
        },
    )

    override suspend fun insertToken(userName: String): Authorization? =
        dbQuery {
            val insertStatement = Authorizations.insert {
                it[Authorizations.userName] = userName
                it[token] = UUID.randomUUID().toString()
                it[lastUsed] = OffsetDateTime.now().toString()
                it[validUntil] = OffsetDateTime.now().plusHours(hoursBeforeExpiration).toString()
            }

            insertStatement.resultedValues?.singleOrNull()?.let { resultRowToAuthorization(it) }
        }

    override suspend fun deleteToken(token: String): Boolean {
        dbQuery {
            Authorizations.deleteWhere { Authorizations.token.eq(token) }
        }
        return true
    }

    override suspend fun getAuthorization(token: String): Authorization? {
        return dbQuery {
            Authorizations.select { Authorizations.token eq token }.map { resultRowToAuthorization(it) }.singleOrNull()
        }
    }

    override suspend fun state(): List<Authorization> {
        return dbQuery {
            Authorizations.selectAll().map { resultRowToAuthorization(it) }
        }
    }
}