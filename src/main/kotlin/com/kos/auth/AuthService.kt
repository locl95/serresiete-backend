package com.kos.auth

import arrow.core.Either
import com.kos.auth.repository.AuthRepository
import java.time.OffsetDateTime

class AuthService(private val authRepository: AuthRepository) {
    suspend fun login(userName: String): LoginResponse {
        val refreshToken = authRepository.insertToken(userName, isAccess = false)
        val accessToken = authRepository.insertToken(userName, isAccess = true)
        return LoginResponse(accessToken, refreshToken)
    }

    suspend fun logout(user: String) = authRepository.deleteToken(user)
    suspend fun validateTokenAndReturnUsername(token: String, isAccessRequest: Boolean): Either<TokenError, String> =
        when (val maybeAuthorization = authRepository.getAuthorization(token)) {
            null -> Either.Left(TokenNotFound(token))
            else -> {
                if (isAccessRequest && maybeAuthorization.isRefresh()) Either.Left(TokenWrongMode(maybeAuthorization.token, isAccess = false))
                else if (!isAccessRequest && maybeAuthorization.isAccess) Either.Left(TokenWrongMode(maybeAuthorization.token, isAccess = true))
                else {
                    maybeAuthorization.validUntil?.takeIf { it.isBefore(OffsetDateTime.now()) }?.let {
                        Either.Left(TokenExpired(maybeAuthorization.token, it))
                    } ?: Either.Right(maybeAuthorization.userName)
                }
            }
        }



    suspend fun refresh(refreshToken: String): Either<TokenError, Authorization?> {
        return when (val maybeAuthorization = authRepository.getAuthorization(refreshToken)) {
            null -> Either.Left(TokenNotFound(refreshToken))
            else -> {
                if (maybeAuthorization.isAccess) Either.Left(TokenWrongMode(maybeAuthorization.token, isAccess = true))
                else {
                    maybeAuthorization.validUntil?.takeIf { it.isBefore(OffsetDateTime.now()) }?.let {
                        Either.Left(TokenExpired(maybeAuthorization.token, it))
                    } ?: Either.Right(authRepository.insertToken(maybeAuthorization.userName, isAccess = true))
                }
            }
        }
    }
}