package com.kos.roles

import kotlinx.serialization.Serializable

enum class Role {
    ADMIN {
        override val maxNumberOfViews: Int = Int.MAX_VALUE
        override val maxNumberOfCharacters: Int = Int.MAX_VALUE
        override fun toString(): String = "admin"
    },
    USER {
        override val maxNumberOfViews: Int = 2
        override val maxNumberOfCharacters: Int = 10
        override fun toString(): String = "user"
    },
    SERVICE {
        override val maxNumberOfViews: Int = 0
        override val maxNumberOfCharacters: Int = 0
        override fun toString(): String = "service"
    };

    abstract val maxNumberOfViews: Int
    abstract val maxNumberOfCharacters: Int

    companion object {
        fun fromString(value: String): Role = when (value) {
            "admin" -> ADMIN
            "user" -> USER
            "service" -> SERVICE
            else -> throw IllegalArgumentException("Unknown role: $value")
        }
    }
}

@Serializable
data class RoleRequest(val role: Role)