package com.kos.auth.repository

import arrow.core.Either
import com.kos.auth.*
import java.time.OffsetDateTime
import java.util.UUID

class AuthInMemoryRepository(
    initialState: List<Authorization> = mutableListOf()) : AuthRepository {

    private val hoursBeforeExpiration: Long = 24
    private val authorizations = mutableListOf<Authorization>()

    init {
        authorizations.addAll(initialState)
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

    override suspend fun state(): List<Authorization> {
        return authorizations
    }

}