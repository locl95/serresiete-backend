package com.kos.activities

import arrow.core.Either
import com.kos.common.ControllerError
import com.kos.common.NotAuthorized
import com.kos.common.NotEnoughPermissions
import com.kos.credentials.CredentialsService
import com.kos.roles.Role

class ActivitiesController(private val activitiesService: ActivitiesService, private val credentialsService: CredentialsService) {
    suspend fun getActivities(client: String?): Either<ControllerError, List<Activity>> {
        return when (client) {
            null -> Either.Left(NotAuthorized)
            else -> {
                if (credentialsService.hasPermissions(client, Activities.getAnyActivities)) {
                    Either.Right(activitiesService.getActivities())
                } else Either.Left(NotEnoughPermissions(client))
            }
        }
    }

    suspend fun createActivity(client: String?, activityRequest: ActivityRequest): Either<ControllerError, Unit> {
        return when (client) {
            null -> Either.Left(NotAuthorized)
            else -> {
                if (credentialsService.hasPermissions(client, Activities.createActivities)) {
                    Either.Right(activitiesService.createActivity(activityRequest))
                } else Either.Left(NotEnoughPermissions(client))
            }
        }
    }

    suspend fun deleteActivity(client: String?, activity: Activity): Either<ControllerError, Unit> {
        return when (client) {
            null -> Either.Left(NotAuthorized)
            else -> {
                if (credentialsService.hasPermissions(client, Activities.deleteActivities)) {
                    Either.Right(activitiesService.deleteActivity(activity))
                } else Either.Left(NotEnoughPermissions(client))
            }
        }
    }

    suspend fun getActivitiesFromRole(client: String?, role: Role): Either<ControllerError, Set<Activity>> {
        return when (client) {
            null -> Either.Left(NotAuthorized)
            else -> {
                if (credentialsService.hasPermissions(client, Activities.getAnyActivities)) {
                    Either.Right(credentialsService.getRoleActivities(role))
                } else Either.Left(NotEnoughPermissions(client))
            }
        }
    }
}