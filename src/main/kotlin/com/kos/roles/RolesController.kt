package com.kos.roles

import arrow.core.Either
import com.kos.activities.Activities
import com.kos.activities.Activity
import com.kos.activities.ActivityRequest
import com.kos.common.ControllerError
import com.kos.common.NotAuthorized
import com.kos.common.NotEnoughPermissions

class RolesController(private val rolesService: RolesService) {
    suspend fun getRoles(client: String?, activities: Set<Activity>): Either<ControllerError, List<Role>> {
        return when (client) {
            null -> Either.Left(NotAuthorized)
            else -> {
                if (activities.contains(Activities.getAnyRoles)) Either.Right(rolesService.getRoles())
                else Either.Left(NotEnoughPermissions(client))
            }
        }
    }

    suspend fun addActivityToRole(
        client: String?,
        activityRequest: ActivityRequest,
        role: Role,
        activities: Set<Activity>
    ): Either<ControllerError, Unit> {
        return when (client) {
            null -> Either.Left(NotAuthorized)
            else -> {
                if (activities.contains(Activities.addActivityToRole))
                    Either.Right(rolesService.addActivityToRole(activityRequest, role))
                else Either.Left(NotEnoughPermissions(client))
            }
        }
    }

    suspend fun deleteActivityFromRole(
        client: String?,
        role: Role,
        activity: Activity,
        activities: Set<Activity>
    ): Either<ControllerError, Unit> {
        return when (client) {
            null -> Either.Left(NotAuthorized)
            else -> {
                if (activities.contains(Activities.deleteActivityFromRole))
                    Either.Right(rolesService.removeActivityFromRole(activity, role))
                else Either.Left(NotEnoughPermissions(client))
            }
        }
    }
}