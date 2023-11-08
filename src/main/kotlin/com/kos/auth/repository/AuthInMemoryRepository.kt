package com.kos.auth.repository

import com.kos.auth.Authorization
import java.time.OffsetDateTime
import java.util.*

class AuthInMemoryRepository : AuthRepository {

    private val hoursBeforeExpiration: Long = 24
    private val authorizations = mutableListOf<Authorization>()

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
    override suspend fun getAuthorization(token: String): Authorization? {
        return  authorizations.find { it.token == token }
    }

    override suspend fun state(): List<Authorization> {
        return authorizations
    }

    override suspend fun withState(initialState: List<Authorization>): AuthInMemoryRepository {
        authorizations.addAll(initialState)
        return this
    }

}