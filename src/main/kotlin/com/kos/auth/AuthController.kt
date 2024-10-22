package com.kos.auth

import arrow.core.Either
import com.kos.activities.Activities
import com.kos.credentials.CredentialsService
import com.kos.common.BadRequest
import com.kos.common.ControllerError
import com.kos.common.NotAuthorized
import com.kos.common.NotEnoughPermissions

class AuthController(
    private val authService: AuthService,
    private val credentialsService: CredentialsService
) {
    suspend fun login(client: String?): Either<ControllerError, LoginResponse> {
        return when (client) {
            null -> Either.Left(NotAuthorized)
            else ->
                if (credentialsService.hasPermissions(client, Activities.login)) authService.login(client)
                else Either.Left(NotEnoughPermissions(client))
        }
    }

    suspend fun logout(client: String?): Either<ControllerError, Boolean> {
        return when (client) {
            null -> Either.Left(NotAuthorized)
            else ->
                if (credentialsService.hasPermissions(client, Activities.logout)) Either.Right(authService.logout(client))
                else Either.Left(NotEnoughPermissions(client))
        }
    }

    suspend fun refresh(refreshToken: String?): Either<ControllerError, Authorization?> {
        return when (refreshToken) {
            null -> Either.Left(NotAuthorized)
            else -> authService.refresh(refreshToken).fold(
                { Either.Left(BadRequest(it.toString())) }, //TODO: This must be improved
                { Either.Right(it) } //TODO: This must be non optional
            )
        }
    }
}