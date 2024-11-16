package com.kos.clients.domain

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*


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

object NameExtractorSerializer : KSerializer<String> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("NameExtractor", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): String {
        require(decoder is JsonDecoder)
        val jsonObject = decoder.decodeJsonElement().jsonObject
        return jsonObject["name"]!!.jsonPrimitive.content
    }

    override fun serialize(encoder: Encoder, value: String) {
        encoder.encodeString(value)
    }
}

object IdExtractorSerializer : KSerializer<Long> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("IdExtractor", PrimitiveKind.LONG)

    override fun deserialize(decoder: Decoder): Long {
        require(decoder is JsonDecoder)
        val jsonObject = decoder.decodeJsonElement().jsonObject
        return jsonObject["id"]!!.jsonPrimitive.long
    }

    override fun serialize(encoder: Encoder, value: Long) {
        encoder.encodeLong(value)
    }
}

@Serializable
data class Realm(val name: String, val id: Long)

@Serializable
data class GetWowCharacterResponse(
    val id: Long,
    val name: String,
    val level: Int,
    @SerialName("is_ghost")
    val isDead: Boolean? = null,
    @SerialName("average_item_level")
    val averageItemLevel: Int,
    @SerialName("equipped_item_level")
    val equippedItemLevel: Int,
    @Serializable(with = NameExtractorSerializer::class)
    @SerialName("character_class")
    val characterClass: String,
    @Serializable(with = NameExtractorSerializer::class)
    val race: String,
    val realm: Realm,
    @Serializable(with = NameExtractorSerializer::class)
    val guild: String? = null,
    val experience: Int
)

@Serializable
data class GetWowRealmResponse(val category: String)

@Serializable
data class HardcoreData(
    val id: Long,
    val name: String,
    val level: Int,
    val isDead: Boolean,
    val averageItemLevel: Int,
    val equippedItemLevel: Int,
    val characterClass: String,
    val race: String,
    val realm: String,
    val guild: String?,
    val experience: Int
) : Data {
    companion object {
        fun apply(characterResponse: GetWowCharacterResponse) = HardcoreData(
            characterResponse.id,
            characterResponse.name,
            characterResponse.level,
            characterResponse.isDead ?: false,
            characterResponse.averageItemLevel,
            characterResponse.equippedItemLevel,
            characterResponse.characterClass,
            characterResponse.race,
            characterResponse.realm.name,
            characterResponse.guild,
            characterResponse.experience
        )
    }
}