package com.kos.credentials

import com.kos.roles.Role
import kotlinx.serialization.Serializable

@Serializable
data class Credentials(val userName: String, val password: String)

data class CredentialsRole(val userName: String, val role: Role)

