package com.kos.credentials

import com.kos.roles.Role
import kotlinx.serialization.Serializable

@Serializable
data class Credentials(val userName: String, val password: String)

@Serializable
data class CredentialsWithRoles(val userName: String, val roles: List<Role>)

@Serializable
data class CreateCredentialsRequest(val userName: String, val password: String, val roles: Set<Role>)

data class CredentialsRole(val userName: String, val role: Role)

