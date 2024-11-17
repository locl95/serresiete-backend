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

@Serializable
data class Realm(val name: String, val id: Long)

@Serializable
data class GetWowCharacterResponse(
    val id: Long,
    val name: String,
    val level: Int,
    @SerialName("is_ghost")
    val isDead: Boolean? = null,
    @SerialName("is_self_found")
    val isSelfFound: Boolean? = null,
    @SerialName("average_item_level")
    val averageItemLevel: Int,
    @SerialName("equipped_item_level")
    val equippedItemLevel: Int,
    @Serializable(with = NameExtractorSerializer::class)
    @SerialName("character_class")
    val characterClass: String,
    @Serializable(with = NameExtractorSerializer::class)
    val faction: String,
    @Serializable(with = NameExtractorSerializer::class)
    val race: String,
    val realm: Realm,
    @Serializable(with = NameExtractorSerializer::class)
    val guild: String? = null,
    val experience: Int
)

@Serializable
data class AssetKeyValue(val key: String, val value: String)

@Serializable
data class GetWowMediaResponse(
    val assets: List<AssetKeyValue>
)

@Serializable
data class WowItemId(val id: Long)

@Serializable
data class WowItemSlot(val name: String)

@Serializable
data class WowItemQuality(val type: String)

@Serializable
data class WowItemResponse(
    val item: WowItemId,
    val slot: WowItemSlot,
    val quality: WowItemQuality,
    val name: String,
)

@Serializable
data class GetWowEquipmentResponse(
    @SerialName("equipped_items")
    val equippedItems: List<WowItemResponse>
)

@Serializable
data class GetWowRealmResponse(val category: String)

@Serializable
data class WowItem(
    val id: Long,
    val slot: String,
    val quality: String,
    val name: String,
)

@Serializable
data class HardcoreData(
    val id: Long,
    val name: String,
    val level: Int,
    val isDead: Boolean,
    val isSelfFound: Boolean,
    val averageItemLevel: Int,
    val equippedItemLevel: Int,
    val characterClass: String,
    val race: String,
    val realm: String,
    val guild: String?,
    val experience: Int,
    val items: List<WowItem>,
    val faction: String,
    val avatar: String?
) : Data {
    companion object {
        fun apply(characterResponse: GetWowCharacterResponse, mediaResponse: GetWowMediaResponse, equipmentResponse: GetWowEquipmentResponse) = HardcoreData(
            characterResponse.id,
            characterResponse.name,
            characterResponse.level,
            characterResponse.isDead ?: false,
            characterResponse.isSelfFound ?: false,
            characterResponse.averageItemLevel,
            characterResponse.equippedItemLevel,
            characterResponse.characterClass,
            characterResponse.race,
            characterResponse.realm.name,
            characterResponse.guild,
            characterResponse.experience,
            equipmentResponse.equippedItems.map { WowItem(it.item.id, it.slot.name, it.quality.type, it.name) },
            characterResponse.faction,
            mediaResponse.assets.find { it.key == "avatar" }?.value
        )
    }
}