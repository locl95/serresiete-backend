package com.kos.auth

import com.kos.common.AuthError
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.OffsetDateTime


object OffsetDateTimeSerializer : KSerializer<OffsetDateTime> {
    override val descriptor = PrimitiveSerialDescriptor("OffsetDateTime", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): OffsetDateTime {
        return OffsetDateTime.parse(decoder.decodeString())
    }

    override fun serialize(encoder: Encoder, value: OffsetDateTime) {
        encoder.encodeString(value.toString())
    }
}

@Serializable
data class Authorization(
    val userName: String,
    val token: String,
    @Serializable(with = OffsetDateTimeSerializer::class)
    val lastUsed: OffsetDateTime,
    @Serializable(with = OffsetDateTimeSerializer::class)
    val validUntil: OffsetDateTime?,
    val isAccess: Boolean
)

fun Authorization.isRefresh() = !isAccess

@Serializable
data class LoginResponse(
    val accessToken: Authorization?,
    val refreshToken: Authorization?
)

interface TokenError : AuthError {
    val token: String
}

data class TokenNotFound(override val token: String) : TokenError {
    override val message: String = "$token not found"
}

data class TokenExpired(override val token: String, val validUntil: OffsetDateTime) : TokenError {
    override val message: String = "$token expired"
}
data class TokenWrongMode(override val token: String, val isAccess: Boolean) : TokenError {
    override val message: String = "$token wrong mode"
}

data class JWTCreationError(override val message: String) : AuthError