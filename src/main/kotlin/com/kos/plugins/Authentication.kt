package com.kos.plugins

import arrow.core.Either
import com.kos.activities.Activity
import com.kos.auth.AuthService
import com.kos.common.toCredentials
import com.kos.credentials.CredentialsService
import io.ktor.server.application.*
import io.ktor.server.auth.*

data class UserWithToken(val name: String, val token: String) : Principal
data class UserWithActivities(val name: String, val activities: Set<Activity>) : Principal

fun Application.configureAuthentication(authService: AuthService, credentialsService: CredentialsService) {
    install(Authentication) {
        basic("auth-basic") {
            validate { credentials ->
                if(credentialsService.validateCredentials(credentials.toCredentials())) UserIdPrincipal(credentials.name)
                else null
            }
        }
        bearer("auth-bearer") {
            authenticate {
                when(val eitherOwnerOrError = authService.validateTokenAndReturnUsername(it.token, isAccessRequest = true)) { //TODO: validateTokenAndReturnActivities
                    is Either.Right -> UserIdPrincipal(eitherOwnerOrError.value)
                    else -> null
                }
            }
        }

        bearer("auth-bearer-jwt") {
            authenticate {
                when(val eitherOwnerOrError = authService.validateTokenAndReturnUsernameWithActivities(it.token, isAccessRequest = true)) { //TODO: validateTokenAndReturnActivities
                    is Either.Right -> UserWithActivities(eitherOwnerOrError.value.first, eitherOwnerOrError.value.second)
                    else -> null
                }
            }
        }

        bearer("auth-bearer-refresh") {
            authenticate {
                when(val eitherOwnerOrError = authService.validateTokenAndReturnUsername(it.token, isAccessRequest = false)) {
                    is Either.Right -> UserWithToken(eitherOwnerOrError.value, it.token)
                    else -> null
                }
            }
        }
    }
}