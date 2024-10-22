package com.kos.auth

import arrow.core.Either
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTCreationException
import com.kos.activities.Activity
import com.kos.auth.repository.AuthRepository
import com.kos.common.AuthError
import com.kos.credentials.CredentialsService
import java.time.OffsetDateTime

private const val secretKey: String = "toalhitasWasHere"

class AuthService(private val authRepository: AuthRepository, private val credentialsService: CredentialsService) {

    suspend fun login(userName: String): Either<JWTCreationError, LoginResponse> {
        //TODO: need different tokens for refresh and access
        return generateToken(userName).map {
            val refreshToken = authRepository.insertToken(userName, it, isAccess = false)
            val accessToken = authRepository.insertToken(userName, it, isAccess = true)
            LoginResponse(accessToken, refreshToken)
        }
    }

    suspend fun logout(user: String) = authRepository.deleteTokensFromUser(user)

    suspend fun validateTokenAndReturnUsername(token: String, isAccessRequest: Boolean): Either<TokenError, String> =
        when (val maybeAuthorization = authRepository.getAuthorization(token)) {
            null -> Either.Left(TokenNotFound(token))
            else -> {
                if (isAccessRequest && maybeAuthorization.isRefresh()) Either.Left(
                    TokenWrongMode(
                        maybeAuthorization.token,
                        isAccess = false
                    )
                )
                else if (!isAccessRequest && maybeAuthorization.isAccess) Either.Left(
                    TokenWrongMode(
                        maybeAuthorization.token,
                        isAccess = true
                    )
                )
                else {
                    maybeAuthorization.validUntil?.takeIf { it.isBefore(OffsetDateTime.now()) }?.let {
                        Either.Left(TokenExpired(maybeAuthorization.token, it))
                    } ?: Either.Right(maybeAuthorization.userName)
                }
            }
        }

    suspend fun refresh(refreshToken: String): Either<AuthError, Authorization?> {
        return when (val maybeAuthorization = authRepository.getAuthorization(refreshToken)) {
            null -> Either.Left(TokenNotFound(refreshToken))
            else -> {
                if (maybeAuthorization.isAccess) Either.Left(TokenWrongMode(maybeAuthorization.token, isAccess = true))
                else {
                    maybeAuthorization.validUntil?.takeIf { it.isBefore(OffsetDateTime.now()) }?.let {
                        Either.Left(TokenExpired(maybeAuthorization.token, it))
                    } ?: generateToken(maybeAuthorization.userName).map { authRepository.insertToken(maybeAuthorization.userName, it, isAccess = true) }
                }
            }
        }
    }

    suspend fun deleteExpiredTokens(): Int {
        return authRepository.deleteExpiredTokens()
    }

    private suspend fun generateToken(userName: String): Either<JWTCreationError, String> {
        val userActivities: List<Activity> =
            credentialsService.getUserRoles(userName).flatMap { credentialsService.getRoleActivities(it) }
        return try {
            Either.Right(
                JWT.create()
                    .withClaim("username", userName)
                    .withClaim("activities", userActivities)
                    .sign(Algorithm.HMAC256(secretKey))
            )
        } catch (e: IllegalArgumentException) {
            Either.Left(JWTCreationError(e.message ?: e.stackTraceToString()))
        } catch (e: JWTCreationException) {
            Either.Left(JWTCreationError(e.message ?: e.stackTraceToString()))
        }
    }
}