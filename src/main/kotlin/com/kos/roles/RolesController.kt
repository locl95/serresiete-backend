package com.kos.roles

import arrow.core.Either
import com.kos.activities.Activities
import com.kos.activities.Activity
import com.kos.activities.ActivityRequest
import com.kos.common.ControllerError
import com.kos.common.NotAuthorized
import com.kos.common.NotEnoughPermissions
import com.kos.credentials.CredentialsService

class RolesController(private val rolesService: RolesService, private val credentialsService: CredentialsService) {
    suspend fun getRoles(client: String?): Either<ControllerError, List<Role>> {
        return when (client) {
            null -> Either.Left(NotAuthorized())
            else -> {
                if (credentialsService.hasPermissions(client, Activities.getAnyRoles)) {
                    Either.Right(rolesService.getRoles())
                } else Either.Left(NotEnoughPermissions(client))
            }
        }
    }

    suspend fun createRole(client: String?, roleRequest: RoleRequest): Either<ControllerError, Unit> {
        return when (client) {
            null -> Either.Left(NotAuthorized())
            else -> {
                if (credentialsService.hasPermissions(client, Activities.createRoles)) {
                    Either.Right(rolesService.createRole(roleRequest))
                } else Either.Left(NotEnoughPermissions(client))
            }
        }
    }

    suspend fun deleteRole(client: String?, role: Role): Either<ControllerError, Unit> {
        return when (client) {
            null -> Either.Left(NotAuthorized())
            else -> {
                if (credentialsService.hasPermissions(client, Activities.deleteRoles)) {
                    Either.Right(rolesService.deleteRole(role))
                } else Either.Left(NotEnoughPermissions(client))
            }
        }
    }

    suspend fun addActivityToRole(
        client: String?,
        activityRequest: ActivityRequest,
        role: Role
    ): Either<ControllerError, Unit> {
        return when (client) {
            null -> Either.Left(NotAuthorized())
            else -> {
                if (credentialsService.hasPermissions(client, Activities.addActivityToRole)) {
                    Either.Right(rolesService.addActivityToRole(activityRequest, role))
                } else Either.Left(NotEnoughPermissions(client))
            }
        }
    }

    suspend fun deleteActivityFromRole(client: String?, role: Role, activity: Activity): Either<ControllerError, Unit> {
        return when (client) {
            null -> Either.Left(NotAuthorized())
            else -> {
                if (credentialsService.hasPermissions(client, Activities.deleteActivityFromRole)) {
                    Either.Right(rolesService.removeActivityFromRole(activity, role))
                } else Either.Left(NotEnoughPermissions(client))
            }
        }
    }
}