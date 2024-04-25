package com.kos.credentials

import com.kos.activities.Activity
import kotlinx.serialization.Serializable

@Serializable
data class Credentials(val userName: String, val password: String)

data class CredentialsRole(val userName: String, val role: Role)
data class RoleActivity(val role: Role, val activity: Activity)

@Serializable
data class RoleRequest(val role: Role)

typealias Role = String

