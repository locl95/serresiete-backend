package com.kos.common

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import org.slf4j.LoggerFactory
import kotlin.math.log

interface ControllerError
class NotAuthorized : ControllerError
data class NotEnoughPermissions(val user: String) : ControllerError
data class NotFound(val id: String) : ControllerError
class BadRequest(val problem: String) : ControllerError
interface HttpError : ControllerError {
    fun error(): String
}

data class JsonParseError(val json: String, val path: String, val error: String? = null) : HttpError {
    override fun error(): String = "ParsedJson: ${json}\nPath: $path"
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
class TooMuchViews : ViewsError

interface DatabaseError : ControllerError
data class InsertCharacterError(val message: String) : DatabaseError

suspend fun ApplicationCall.respondLogging(error: String) {
    val logger = LoggerFactory.getLogger("ktor.application")
    logger.error(error)
    respond(HttpStatusCode.InternalServerError, error)
}

suspend fun ApplicationCall.respondWithHandledError(error: ControllerError) {
    when (error) {
        is NotFound -> respond(HttpStatusCode.NotFound, error.id)
        is NotAuthorized -> respond(HttpStatusCode.Unauthorized)
        is NotEnoughPermissions -> respond(HttpStatusCode.Forbidden)
        is NotPublished -> respond(HttpStatusCode.BadRequest, "view not published")
        is TooMuchViews -> respond(HttpStatusCode.BadRequest, "too much views")
        is BadRequest -> respond(HttpStatusCode.BadRequest, error.problem)
        is JsonParseError -> respondLogging(error.error())
        is RaiderIoError -> respondLogging(error.error())
        is InsertCharacterError -> respondLogging(error.message)
    }
}