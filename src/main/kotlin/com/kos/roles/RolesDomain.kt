package com.kos.roles

import kotlinx.serialization.Serializable

typealias Role = String

@Serializable
data class RoleRequest(val role: Role)