package com.kos.auth.repository

import arrow.core.Either
import com.kos.auth.Authorization
import com.kos.auth.TokenNotFound
import com.kos.auth.User
import java.time.OffsetDateTime
import java.util.UUID

class AuthInMemoryRepository(
    initialState: Pair<List<User>, List<Authorization>> = Pair(
        mutableListOf(),
        mutableListOf()
    )
) : AuthRepository {
    private val users = mutableListOf<User>()
    private val authorizations = mutableListOf<Authorization>()

    init {
        users.addAll(initialState.first)
        authorizations.addAll(initialState.second)
    }

    override suspend fun insertToken(userName: String): Authorization {
        val authorization = Authorization(userName, UUID.randomUUID().toString(), OffsetDateTime.now(), OffsetDateTime.now().plusHours(24))
        authorizations.add(authorization)
        return authorization
    }

    override fun deleteToken(user: String) = authorizations.removeIf {it.userName == user}

    override suspend fun validateCredentials(userName: String, password: String): Boolean {
        return users.contains(User(userName, password))
    }

    override suspend fun validateToken(token: String): Either<TokenNotFound, String> {
        return when(val authorization = authorizations.find {it.token == token}) {
            null -> Either.Left(TokenNotFound(token))
            else -> Either.Right(authorization.userName)
        }
    }

    override fun state(): Pair<List<User>, List<Authorization>> {
       return Pair(users, authorizations)
    }

}