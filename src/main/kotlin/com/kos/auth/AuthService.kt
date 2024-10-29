package com.kos.auth

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.raise.either
import com.auth0.jwt.JWT
import com.auth0.jwt.JWTCreator
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTCreationException
import com.kos.activities.Activity
import com.kos.auth.repository.AuthRepository
import com.kos.common.ControllerError
import com.kos.credentials.CredentialsService
import java.time.OffsetDateTime

private const val secretKey: String = "toalhitasWasHere" //TODO: Both in environment
private const val issuer: String = "http://localhost:8080"

class AuthService(private val authRepository: AuthRepository, private val credentialsService: CredentialsService) {

    suspend fun login(userName: String): Either<ControllerError, LoginResponse> {
        return either {
            //TODO: check if refresh token has to bring all the activities
            val jwtAccess = generateToken(userName, TokenMode.ACCESS).bind()
            val jwtRefresh = generateToken(userName, TokenMode.REFRESH).bind()
            val refreshToken = authRepository.insertToken(userName, jwtRefresh, isAccess = false).bind()
            val accessToken = authRepository.insertToken(userName, jwtAccess, isAccess = true).bind()
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


    //TODO: Implement the extension method over JWT class in com.kos.common.JWTExtension.kt.
    //TODO: We need to validate with the token itself. We should not need to work with a repo.
    suspend fun validateTokenAndReturnUsernameWithActivities(
        token: String,
        isAccessRequest: Boolean
    ): Either<TokenError, Pair<String, Set<Activity>>> =
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
                    } ?: Either.Right(Pair(maybeAuthorization.userName, setOf()))
                }
            }
        }

    //TODO: Fix this function also, we need to check refresh from claim
    suspend fun refresh(refreshToken: String): Either<ControllerError, Authorization?> {
        return when (val maybeAuthorization = authRepository.getAuthorization(refreshToken)) {
            null -> Either.Left(TokenNotFound(refreshToken))
            else -> {
                if (maybeAuthorization.isAccess) Either.Left(TokenWrongMode(maybeAuthorization.token, isAccess = true))
                else {
                    maybeAuthorization.validUntil?.takeIf { it.isBefore(OffsetDateTime.now()) }?.let {
                        Either.Left(TokenExpired(maybeAuthorization.token, it))
                    } ?: generateToken(maybeAuthorization.userName, TokenMode.ACCESS).flatMap {
                        authRepository.insertToken(
                            maybeAuthorization.userName,
                            it,
                            isAccess = true
                        )
                    }
                }
            }
        }
    }

    suspend fun deleteExpiredTokens(): Int {
        return authRepository.deleteExpiredTokens()
    }

    private suspend fun generateToken(userName: String, tokenMode: TokenMode): Either<JWTCreationError, String> {
        return try {
            val now = OffsetDateTime.now()
            val jwtBuilder: JWTCreator.Builder = JWT.create()
                .withClaim("username", userName)
                .withClaim("mode", tokenMode.toString())
                .withIssuer(issuer)
                .withIssuedAt(now.toInstant())

            val token = when (tokenMode) {
                TokenMode.ACCESS -> {
                    val userActivities: List<Activity> =
                        credentialsService.getUserRoles(userName).flatMap { credentialsService.getRoleActivities(it) }
                    jwtBuilder
                        .withClaim("activities", userActivities)
                        .withExpiresAt(now.plusMinutes(15).toInstant())
                }

                TokenMode.REFRESH -> jwtBuilder
                    .withExpiresAt(now.plusDays(30).toInstant())
            }.sign(Algorithm.HMAC256(secretKey)) //TODO: Think about using a better encryption algorithm (public + private key)

            Either.Right(token)

        } catch (e: IllegalArgumentException) {
            Either.Left(JWTCreationError(e.message ?: e.stackTraceToString()))
        } catch (e: JWTCreationException) {
            Either.Left(JWTCreationError(e.message ?: e.stackTraceToString()))
        }
    }
}