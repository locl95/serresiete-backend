package com.kos.views

import arrow.core.Either
import arrow.core.flatten
import com.kos.activities.Activities
import com.kos.credentials.CredentialsService
import com.kos.raiderio.RaiderIoData
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*

class ViewsController(
    private val viewsService: ViewsService,
    private val credentialsService: CredentialsService
) {

    suspend fun getViews(user: String?): Either<ControllerError, List<SimpleView>> {
        return when (user) {
            null -> Either.Left(NotAuthorized())
            else -> {
                if (credentialsService.hasPermissions(user, Activities.getAnyViews)) Either.Right(viewsService.getViews())
                else if (credentialsService.hasPermissions(user, Activities.getOwnViews)) Either.Right(viewsService.getOwnViews(user))
                else Either.Left(NotEnoughPermissions(user))
            }
        }
    }

    suspend fun getView(user: String?, id: String): Either<ControllerError, View> {
        return when (user) {
            null -> Either.Left(NotAuthorized())
            else -> {
                return when (val maybeView = viewsService.get(id)) {
                    null -> Either.Left(NotFound(id))
                    else -> {
                        if ((maybeView.owner == user && credentialsService.hasPermissions(user, Activities.getOwnView))
                                    || credentialsService.hasPermissions(user, Activities.getAnyView)) Either.Right(maybeView)
                        else Either.Left(NotEnoughPermissions(user))
                    }
                }
            }
        }
    }

    suspend fun getViewData(user: String?, id: String): Either<ControllerError, List<RaiderIoData>> {
        return when (user) {
            null -> Either.Left(NotAuthorized())
            else -> {
                return when (val maybeView = viewsService.get(id)) {
                    null -> Either.Left(NotFound(id))
                    else -> {
                        if (credentialsService.hasPermissions(user, Activities.getViewData) && maybeView.published)
                            Either.Right(viewsService.getData(maybeView)).flatten()
                        else if (!maybeView.published) Either.Left(NotPublished(id))
                        else Either.Left(NotEnoughPermissions(user))
                    }
                }
            }
        }
    }

    suspend fun getViewCachedData(user: String?, id: String): Either<ControllerError, List<RaiderIoData>> {
        return when (user) {
            null -> Either.Left(NotAuthorized())
            else -> {
                return when (val maybeView = viewsService.get(id)) {
                    null -> Either.Left(NotFound(id))
                    else -> {
                        if (credentialsService.hasPermissions(user, Activities.getViewCachedData) && maybeView.published)
                            Either.Right(viewsService.getData(maybeView)).flatten()
                        else if (!maybeView.published) Either.Left(NotPublished(id))
                        else Either.Left(NotEnoughPermissions(user))
                    }
                }
            }
        }
    }

    suspend fun createView(user: String?, request: ViewRequest): Either<ControllerError, ViewModified> {
        return when (user) {
            null -> Either.Left(NotAuthorized())
            else -> {
                if (credentialsService.hasPermissions(user, Activities.createViews)) {
                    when (val res = viewsService.create(user, request)) {
                        is Either.Right -> Either.Right(res.value)
                        is Either.Left -> Either.Left(TooMuchViews())
                    }
                } else Either.Left(NotEnoughPermissions(user))
            }
        }
    }

}



interface ControllerError
class NotAuthorized : ControllerError
class NotEnoughPermissions(val user: String) : ControllerError
class NotFound(val id: String) : ControllerError
// class HttpError : ControllerError -> JsonParseError.kt

interface ViewsError : ControllerError
class NotPublished(val id: String) : ViewsError
// class TooMuchViews : ViewsError -> ViewsDomain.kt