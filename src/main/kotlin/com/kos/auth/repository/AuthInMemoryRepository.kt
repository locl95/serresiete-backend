package com.kos.auth.repository

import arrow.core.Either
import com.kos.auth.*
import java.time.OffsetDateTime
import java.util.UUID

class AuthInMemoryRepository(
    initialState: Pair<List<User>, List<Authorization>> = Pair(
        mutableListOf(),
        mutableListOf()
    )
) : AuthRepository {

    private val hoursBeforeExpiration: Long = 24
    private val users = mutableListOf<User>()
    private val authorizations = mutableListOf<Authorization>()

    init {
        users.addAll(initialState.first)
        authorizations.addAll(initialState.second)
    }

    override suspend fun insertToken(userName: String): Authorization {
        val authorization = Authorization(
            userName, UUID.randomUUID().toString(), OffsetDateTime.now(), OffsetDateTime.now().plusHours(
                hoursBeforeExpiration
            )
        )
        authorizations.add(authorization)
        return authorization
    }

    override suspend fun deleteToken(token: String) = authorizations.removeIf { it.token == token }

    override suspend fun validateCredentials(userName: String, password: String): Boolean {
        return users.contains(User(userName, password))
    }

    override suspend fun validateToken(token: String): Either<TokenError, String> {
        return when (val authorization = authorizations.find { it.token == token }) {
            null -> Either.Left(TokenNotFound(token))
            else -> {
                authorization.validUntil?.takeIf { it.isBefore(OffsetDateTime.now()) }?.let {
                    Either.Left(TokenExpired(authorization.token, it))
                } ?: Either.Right(authorization.userName)
            }
        }
    }

    override suspend fun state(): Pair<List<User>, List<Authorization>> {
        return Pair(users, authorizations)
    }

}