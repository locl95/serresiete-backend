package com.kos.common

import com.kos.views.ViewResult
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import kotlinx.serialization.Serializable

interface ControllerError
class NotAuthorized : ControllerError
data class NotEnoughPermissions(val user: String) : ControllerError
data class NotFound(val id: String) : ControllerError
class BadRequest(val problem: String) : ControllerError
interface HttpError : ControllerError {
    fun error(): String
}

data class JsonParseError(val json: String, val path: String) : HttpError {
    override fun error(): String = "ParsedJson: ${json}\nPath: $path"
}

data class RaiderIoError(
    val statusCode: Int,
    val error: String,
    val message: String
): HttpError {
    override fun error(): String = "$message. $error: $statusCode"
}

interface ViewsError : ControllerError
class NotPublished(val id: String) : ViewsError
class TooMuchViews : ViewsError
data class InsertCharacterError(val message: String) : ViewsError

suspend fun ApplicationCall.respondWithHandledError(error: ControllerError) {
    when (error) {
        is NotFound -> respond(HttpStatusCode.NotFound, error.id)
        is NotAuthorized -> respond(HttpStatusCode.Unauthorized)
        is NotEnoughPermissions -> respond(HttpStatusCode.Forbidden)
        is NotPublished -> respond(HttpStatusCode.BadRequest, "view not published")
        is TooMuchViews -> respond(HttpStatusCode.BadRequest, "too much views")
        is BadRequest -> respond(HttpStatusCode.BadRequest, error.problem)
        is JsonParseError -> respond(HttpStatusCode.InternalServerError, error.error())
        is RaiderIoError -> respond(HttpStatusCode.InternalServerError, error.error())
        is InsertCharacterError -> respond(HttpStatusCode.InternalServerError, error.message)
    }
}