package com.kos.auth

import arrow.core.Either
import com.kos.auth.repository.AuthRepository
import java.time.OffsetDateTime

class AuthService(private val authRepository: AuthRepository) {
    suspend fun login(userName: String) = authRepository.insertToken(userName)
    suspend fun logout(user: String) = authRepository.deleteToken(user)
    suspend fun validateTokenAndReturnUsername(token: String): Either<TokenError, String> =
        when (val maybeAuthorization = authRepository.getAuthorization(token)) {
            null -> Either.Left(TokenNotFound(token))
            else -> {
                maybeAuthorization.validUntil?.takeIf { it.isBefore(OffsetDateTime.now()) }?.let {
                    Either.Left(TokenExpired(maybeAuthorization.token, it))
                } ?: Either.Right(maybeAuthorization.userName)
            }
        }
}