package com.kos.httpclients.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


data class BlizzardCredentials(val client: String, val secret: String)
@Serializable
data class TokenResponse(
    @SerialName("access_token")
    val accessToken: String,
    @SerialName("token_type")
    val tokenType: String,
    @SerialName("expires_in")
    val expiresIn: Long,
    val sub: String
)

@Serializable
data class GetWowCharacterResponse(
    val id: Long,
    val name: String,
    val level: Int,
    @SerialName("is_ghost")
    val isDead: Boolean,
)