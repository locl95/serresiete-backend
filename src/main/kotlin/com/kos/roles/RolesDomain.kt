package com.kos.roles

import kotlinx.serialization.Serializable

object Roles {
    val admin = "admin"
    val user = "user"
}
typealias Role = String

@Serializable
data class RoleRequest(val role: Role)