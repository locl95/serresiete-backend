package com.kos.views

import arrow.core.Either
import arrow.core.flatten
import com.kos.activities.Activities
import com.kos.common.*
import com.kos.credentials.CredentialsService
import com.kos.httpclients.domain.Data

class ViewsController(
    private val viewsService: ViewsService,
    private val credentialsService: CredentialsService
) {

    suspend fun getViews(client: String?): Either<ControllerError, List<SimpleView>> {
        return when (client) {
            null -> Either.Left(NotAuthorized())
            else -> {
                if (credentialsService.hasPermissions(
                        client,
                        Activities.getAnyViews
                    )
                ) Either.Right(viewsService.getViews())
                else if (credentialsService.hasPermissions(
                        client,
                        Activities.getOwnViews
                    )
                ) Either.Right(viewsService.getOwnViews(client))
                else Either.Left(NotEnoughPermissions(client))
            }
        }
    }

    suspend fun getView(client: String?, id: String): Either<ControllerError, View> {
        return when (client) {
            null -> Either.Left(NotAuthorized())
            else -> {
                return when (val maybeView = viewsService.get(id)) {
                    null -> Either.Left(NotFound(id))
                    else -> {
                        if ((maybeView.owner == client && credentialsService.hasPermissions(
                                client,
                                Activities.getOwnView
                            ))
                            || credentialsService.hasPermissions(client, Activities.getAnyView)
                        ) Either.Right(maybeView)
                        else Either.Left(NotEnoughPermissions(client))
                    }
                }
            }
        }
    }

    suspend fun getViewData(client: String?, id: String): Either<ControllerError, List<Data>> {
        return when (client) {
            null -> Either.Left(NotAuthorized())
            else -> {
                return when (val maybeView = viewsService.get(id)) {
                    null -> Either.Left(NotFound(id))
                    else -> {
                        if (credentialsService.hasPermissions(client, Activities.getViewData) && maybeView.published)
                            Either.Right(viewsService.getData(maybeView)).flatten()
                        else if (!maybeView.published) Either.Left(NotPublished(id))
                        else Either.Left(NotEnoughPermissions(client))
                    }
                }
            }
        }
    }

    suspend fun getViewCachedData(client: String?, id: String): Either<ControllerError, List<Data>> {
        return when (client) {
            null -> Either.Left(NotAuthorized())
            else -> {
                return when (val maybeView = viewsService.getSimple(id)) {
                    null -> Either.Left(NotFound(id))
                    else -> {
                        if (credentialsService.hasPermissions(
                                client,
                                Activities.getViewCachedData
                            ) && maybeView.published
                        )
                            Either.Right(viewsService.getCachedData(maybeView)).flatten()
                        else if (!maybeView.published) Either.Left(NotPublished(id))
                        else Either.Left(NotEnoughPermissions(client))
                    }
                }
            }
        }
    }

    suspend fun createView(client: String?, request: ViewRequest): Either<ControllerError, SimpleView> {
        return when (client) {
            null -> Either.Left(NotAuthorized())
            else -> {
                if (credentialsService.hasPermissions(client, Activities.createViews)) {
                    when (val res = viewsService.create(client, request)) {
                        is Either.Right -> Either.Right(res.value)
                        is Either.Left -> res
                    }
                } else Either.Left(NotEnoughPermissions(client))
            }
        }
    }

    suspend fun editView(client: String?, request: ViewRequest, id: String): Either<ControllerError, ViewModified> {
        return when (client) {
            null -> Either.Left(NotAuthorized())
            else -> when (val maybeView = viewsService.get(id)) {
                null -> Either.Left(NotFound(id))
                else -> {
                    if ((maybeView.owner == client && credentialsService.hasPermissions(client, Activities.editOwnView))
                        || credentialsService.hasPermissions(client, Activities.editAnyView)
                    ) {
                        viewsService.edit(maybeView.id, request)
                    } else Either.Left(NotEnoughPermissions(client))
                }
            }
        }
    }

    suspend fun patchView(client: String?, request: ViewPatchRequest, id: String): Either<ControllerError, ViewPatched> {
        //TODO: We can propagate view fields to those who are optional from patch, or we can keep it like this to display which fields we modified
        return when (client) {
            null -> Either.Left(NotAuthorized())
            else -> when (val maybeView = viewsService.get(id)) {
                null -> Either.Left(NotFound(id))
                else -> {
                    if ((maybeView.owner == client && credentialsService.hasPermissions(client, Activities.editOwnView))
                        || credentialsService.hasPermissions(client, Activities.editAnyView)
                    ) {
                        viewsService.patch(maybeView.id, request)
                    } else Either.Left(NotEnoughPermissions(client))
                }
            }
        }
    }

    suspend fun deleteView(client: String?, id: String): Either<ControllerError, ViewDeleted> {
        return when (client) {
            null -> Either.Left(NotAuthorized())
            else -> when (val maybeView = viewsService.get(id)) {
                null -> Either.Left(NotFound(id))
                else -> {
                    if ((maybeView.owner == client && credentialsService.hasPermissions(
                            client,
                            Activities.deleteOwnView
                        ))
                        || credentialsService.hasPermissions(client, Activities.deleteAnyView)
                    ) {
                        Either.Right(viewsService.delete(maybeView.id))
                    } else Either.Left(NotEnoughPermissions(client))
                }
            }
        }
    }

}