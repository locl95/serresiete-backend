package com.kos.activities

import arrow.core.Either
import com.kos.common.ControllerError
import com.kos.common.NotAuthorized
import com.kos.common.NotEnoughPermissions
import com.kos.credentials.CredentialsService
import com.kos.roles.Role

class ActivitiesController(
    private val activitiesService: ActivitiesService,
    private val credentialsService: CredentialsService
) {
    suspend fun getActivities(client: String?, activities: Set<Activity>): Either<ControllerError, Set<Activity>> {
        return when (client) {
            null -> Either.Left(NotAuthorized)
            else -> {
                if (activities.contains(Activities.getAnyActivities))
                    Either.Right(activitiesService.getActivities())
                else Either.Left(NotEnoughPermissions(client))
            }
        }
    }

    suspend fun getActivitiesFromRole(
        client: String?,
        role: Role,
        activities: Set<Activity>
    ): Either<ControllerError, Set<Activity>> {
        return when (client) {
            null -> Either.Left(NotAuthorized)
            else -> {
                if (activities.contains(Activities.getAnyActivities))
                    Either.Right(credentialsService.getRoleActivities(role))
                else Either.Left(NotEnoughPermissions(client))
            }
        }
    }
}