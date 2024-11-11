package com.kos.credentials

import arrow.core.Either
import com.kos.activities.Activities
import com.kos.activities.Activity
import com.kos.common.ControllerError
import com.kos.common.NotAuthorized
import com.kos.common.NotEnoughPermissions
import com.kos.common.NotFound
import com.kos.roles.Role

class CredentialsController(val credentialsService: CredentialsService) {
    suspend fun createCredential(
        client: String?,
        activities: Set<Activity>,
        request: CreateCredentialRequest,
    ): Either<ControllerError, Unit> {
        return when (client) {
            null -> Either.Left(NotAuthorized)
            else -> {
                if (activities.contains(Activities.createCredentials)) Either.Right(
                    credentialsService.createCredentials(request)
                )
                else Either.Left(NotEnoughPermissions(client))
            }
        }
    }

    suspend fun editCredential(
        client: String?,
        activities: Set<Activity>,
        userName: String,
        request: EditCredentialRequest
    ): Either<ControllerError, Unit> {
        return when (client) {
            null -> Either.Left(NotAuthorized)
            else -> {
                if (activities.contains(Activities.editCredentials)) {
                    Either.Right(credentialsService.editCredential(userName, request))
                } else Either.Left(NotEnoughPermissions(client))
            }
        }
    }

    suspend fun patchCredential(
        client: String?,
        activities: Set<Activity>,
        username: String,
        credentials: PatchCredentialRequest
    ): Either<ControllerError, Unit> {
        return when (client) {
            null -> Either.Left(NotAuthorized)
            else -> {
                if (activities.contains(Activities.patchCredentials)) {
                    Either.Right(credentialsService.patchCredential(username, credentials))
                } else Either.Left(NotEnoughPermissions(client))
            }
        }
    }

    suspend fun deleteCredential(
        client: String?,
        activities: Set<Activity>,
        userToDelete: String
    ): Either<ControllerError, Unit> {
        return when (client) {
            null -> Either.Left(NotAuthorized)
            else -> {
                if (activities.contains(Activities.deleteCredentials)) {
                    Either.Right(credentialsService.deleteCredentials(userToDelete))
                } else Either.Left(NotEnoughPermissions(client))
            }
        }
    }

    suspend fun getCredentials(client: String?, activities: Set<Activity>): Either<ControllerError, List<CredentialsWithRoles>> {
        return when (client) {
            null -> Either.Left(NotAuthorized)
            else -> {
                if (activities.contains(Activities.getAnyCredentials)) {
                    Either.Right(credentialsService.getCredentials())
                } else Either.Left(NotEnoughPermissions(client))
            }
        }
    }

    suspend fun getCredential(client: String?, user: String, activities: Set<Activity>): Either<ControllerError, CredentialsWithRoles> {
        return when (client) {
            null -> Either.Left(NotAuthorized)
            else -> {
                if (activities.contains(Activities.getAnyCredential)) {
                    when (val credential = credentialsService.getCredential(user)) {
                        null -> Either.Left(NotFound(user))
                        else -> Either.Right(credential)
                    }
                } else Either.Left(NotEnoughPermissions(client))
            }
        }
    }

    suspend fun getUserRoles(
        client: String?,
        activities: Set<Activity>,
        user: String
    ): Either<ControllerError, List<Role>> {
        return when (client) {
            null -> Either.Left(NotAuthorized)
            else -> {
                if ((user == client && activities.contains(
                        Activities.getOwnCredentialsRoles
                    )) || activities.contains(
                        Activities.getAnyCredentialsRoles
                    )
                ) {
                    Either.Right(credentialsService.getUserRoles(user))
                } else Either.Left(NotEnoughPermissions(client))
            }
        }
    }

    suspend fun addRoleToUser(
        client: String?,
        activities: Set<Activity>,
        user: String,
        role: Role
    ): Either<ControllerError, Unit> {
        return when (client) {
            null -> Either.Left(NotAuthorized)
            else -> {
                if (activities.contains(Activities.addRoleToUser)) {
                    Either.Right(credentialsService.addRoleToUser(user, role))
                } else Either.Left(NotEnoughPermissions(client))
            }
        }
    }

    suspend fun deleteRoleFromUser(
        client: String?,
        activities: Set<Activity>,
        user: String,
        role: Role
    ): Either<ControllerError, Unit> {
        return when (client) {
            null -> Either.Left(NotAuthorized)
            else -> {
                if (activities.contains(Activities.deleteRoleFromUser)) {
                    Either.Right(credentialsService.deleteRoleFromUser(user, role))
                } else Either.Left(NotEnoughPermissions(client))
            }
        }
    }
}