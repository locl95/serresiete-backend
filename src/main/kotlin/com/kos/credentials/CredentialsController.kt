package com.kos.credentials

import arrow.core.Either
import com.kos.activities.Activities
import com.kos.common.ControllerError
import com.kos.common.NotAuthorized
import com.kos.common.NotEnoughPermissions
import com.kos.roles.Role

class CredentialsController(val credentialsService: CredentialsService) {
    suspend fun createCredential(client: String?, credentials: Credentials): Either<ControllerError, Unit> {
        return when (client) {
            null -> Either.Left(NotAuthorized())
            else -> {
                if (credentialsService.hasPermissions(client, Activities.createCredentials)) {
                    Either.Right(credentialsService.createCredentials(credentials))
                } else Either.Left(NotEnoughPermissions(client))
            }
        }
    }

    suspend fun editCredential(client: String?, credentials: Credentials): Either<ControllerError, Unit> {
        return when (client) {
            null -> Either.Left(NotAuthorized())
            else -> {
                if (credentialsService.hasPermissions(client, Activities.createCredentials)) {
                    Either.Right(credentialsService.editCredentials(credentials))
                } else Either.Left(NotEnoughPermissions(client))
            }
        }
    }

    suspend fun deleteCredential(client: String?, userToDelete: String): Either<ControllerError, Unit> {
        return when (client) {
            null -> Either.Left(NotAuthorized())
            else -> {
                if (credentialsService.hasPermissions(client, Activities.deleteCredentials)) {
                    Either.Right(credentialsService.deleteCredentials(userToDelete))
                } else Either.Left(NotEnoughPermissions(client))
            }
        }
    }

    suspend fun getCredentials(client: String?): Either<ControllerError, List<Credentials>> {
        return when (client) {
            null -> Either.Left(NotAuthorized())
            else -> {
                if (credentialsService.hasPermissions(client, Activities.getAnyCredentials)) {
                    Either.Right(credentialsService.getCredentials())
                } else Either.Left(NotEnoughPermissions(client))
            }
        }
    }

    suspend fun getUserRoles(client: String?, user: String): Either<ControllerError, List<Role>> {
        return when (client) {
            null -> Either.Left(NotAuthorized())
            else -> {
                if ((user == client && credentialsService.hasPermissions(
                        client,
                        Activities.getOwnCredentialsRoles
                    )) || credentialsService.hasPermissions(
                        client,
                        Activities.getAnyCredentialsRoles
                    )
                ) {
                    Either.Right(credentialsService.getUserRoles(user))
                } else Either.Left(NotEnoughPermissions(client))
            }
        }
    }
    suspend fun addRoleToUser(client: String?, user: String, role: Role): Either<ControllerError, Unit> {
        return when (client) {
            null -> Either.Left(NotAuthorized())
            else -> {
                if (credentialsService.hasPermissions(client, Activities.addRoleToUser)) {
                    Either.Right(credentialsService.addRoleToUser(user, role))
                } else Either.Left(NotEnoughPermissions(client))
            }
        }
    }
    suspend fun deleteRoleFromUser(client: String?, user: String, role: Role): Either<ControllerError, Unit> {
        return when (client) {
            null -> Either.Left(NotAuthorized())
            else -> {
                if (credentialsService.hasPermissions(client, Activities.addRoleToUser)) {
                    Either.Right(credentialsService.deleteRoleFromUser(user, role))
                } else Either.Left(NotEnoughPermissions(client))
            }
        }
    }
}