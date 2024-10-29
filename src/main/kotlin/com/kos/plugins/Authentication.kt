package com.kos.plugins

import arrow.core.Either
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.kos.activities.Activity
import com.kos.auth.AuthService
import com.kos.common.toCredentials
import com.kos.credentials.CredentialsService
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import java.time.OffsetDateTime

data class UserWithToken(val name: String, val token: String) : Principal
data class UserWithActivities(val name: String, val activities: Set<Activity>) : Principal

private const val secretKey: String = "toalhitasWasHere" //TODO: Both in environment
private const val issuer: String = "http://localhost:8080"

fun Application.configureAuthentication(authService: AuthService, credentialsService: CredentialsService) {
    install(Authentication) {
        basic("auth-basic") {
            validate { credentials ->
                if (credentialsService.validateCredentials(credentials.toCredentials())) UserIdPrincipal(credentials.name)
                else null
            }
        }

        bearer("auth-bearer") {
            authenticate {
                when (val eitherOwnerOrError = authService.validateTokenAndReturnUsername(
                    it.token,
                    isAccessRequest = true
                )) { //TODO: validateTokenAndReturnActivities
                    is Either.Right -> UserIdPrincipal(eitherOwnerOrError.value)
                    else -> null
                }
            }
        }

        bearer("auth-bearer-jwt") {
            authenticate {
                when (val eitherOwnerOrError = authService.validateTokenAndReturnUsernameWithActivities(
                    it.token,
                    isAccessRequest = true
                )) { //TODO: validateTokenAndReturnActivities
                    is Either.Right -> UserWithActivities(
                        eitherOwnerOrError.value.first,
                        eitherOwnerOrError.value.second
                    )

                    else -> null
                }
            }
        }

        bearer("auth-bearer-refresh") {
            authenticate {
                when (val eitherOwnerOrError =
                    authService.validateTokenAndReturnUsername(it.token, isAccessRequest = false)) {
                    is Either.Right -> UserWithToken(eitherOwnerOrError.value, it.token)
                    else -> null
                }
            }
        }

        jwt("auth-jwt") {
            verifier(
                JWT.require(Algorithm.HMAC256(secretKey))
                    .withIssuer(issuer)
                    .withClaimPresence("username")
                    .withClaimPresence("activities")
                    .build()
            )

            validate { token ->
                if (token.payload.expiresAtAsInstant.isBefore(OffsetDateTime.now().toInstant())) null
                else {
                    val username = token.payload.getClaim("username").asString()
                    val activities = token.payload.getClaim("activities").asList(String::class.java).toSet()
                    UserWithActivities(username, activities)
                }
            }

        }
    }
}