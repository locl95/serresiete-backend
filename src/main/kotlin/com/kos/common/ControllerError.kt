package com.kos.common

import io.ktor.http.*
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.Forbidden
import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.http.HttpStatusCode.Companion.Unauthorized
import io.ktor.server.application.*
import io.ktor.server.response.*
import org.slf4j.LoggerFactory

sealed interface ControllerError
data object NotAuthorized : ControllerError
data class NotEnoughPermissions(val user: String) : ControllerError
data class NotFound(val id: String) : ControllerError
class BadRequest(val problem: String) : ControllerError
class InvalidQueryParameter(param: String, value: String, allowed: List<String>?) : ControllerError {
    private val baseMessage = "invalid query param[$param]: $value"
    val message: String = allowed._fold({ baseMessage }, { "$baseMessage\nallowed values: $it" })
}

class InvalidTaskType(val type: String)
interface HttpError : ControllerError {
    fun error(): String
}

data class JsonParseError(val json: String, val path: String, val error: String? = null) : HttpError {
    override fun error(): String = "ParsedJson: ${json}\nPath: $path Error: $error"
}

data class RaiderIoError(
    val statusCode: Int,
    val error: String,
    val message: String
) : HttpError {
    override fun error(): String = "$message. $error: $statusCode"
}

interface ViewsError : ControllerError
class NotPublished(val id: String) : ViewsError
data object TooMuchViews : ViewsError
data object TooMuchCharacters : ViewsError
data object UserWithoutRoles : ViewsError

interface DatabaseError : ControllerError
data class InsertError(val message: String) : DatabaseError

interface AuthError: ControllerError {
    val message: String
}

suspend fun ApplicationCall.respondLogging(error: String) {
    val logger = LoggerFactory.getLogger("ktor.application")
    logger.error(error)
    respond(HttpStatusCode.InternalServerError, error)
}

suspend fun ApplicationCall.respondWithHandledError(error: ControllerError) {
    when (error) {
        is NotFound -> respond(NotFound, error.id)
        is NotAuthorized -> respond(Unauthorized)
        is NotEnoughPermissions -> respond(Forbidden)
        is NotPublished -> respond(BadRequest, "view not published")
        is TooMuchViews -> respond(BadRequest, "too much views")
        is TooMuchCharacters -> respond(BadRequest, "too many characters in a view")
        is BadRequest -> respond(BadRequest, error.problem)
        is JsonParseError -> respondLogging(error.error())
        is RaiderIoError -> respondLogging(error.error())
        is InvalidQueryParameter -> respond(HttpStatusCode.BadRequest, error.message)
        is InsertError -> respondLogging(error.message)
        is AuthError -> respond(HttpStatusCode.Unauthorized, error.message)
        is DatabaseError -> respondLogging(error.toString()) //TODO: improve
        is HttpError -> TODO()
        is ViewsError -> TODO()
    }
}