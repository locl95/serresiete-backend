package com.kos.auth

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

data class User(val userName: String, val password: String)

@Serializable
data class Authorization(
    val userName: String,
    val token: String,
    @Serializable(with = OffsetDateTimeSerializer::class)
    val lastUsed: OffsetDateTime,
    @Serializable(with = OffsetDateTimeSerializer::class)
    val validUntil: OffsetDateTime
)

interface TokenError {
    val token: String
}

data class TokenNotFound(override val token: String) : TokenError
data class TokenExpired(override val token: String, val validUntil: OffsetDateTime) : TokenError
