package com.kos.clients.domain

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
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

object EffectiveExtractorSerializer : KSerializer<Int> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("NameExtractor", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Int {
        require(decoder is JsonDecoder)
        val jsonObject = decoder.decodeJsonElement().jsonObject
        return jsonObject["effective"]!!.jsonPrimitive.int
    }

    override fun serialize(encoder: Encoder, value: Int) {
        encoder.encodeInt(value)
    }
}

object ValueExtractorSerializer : KSerializer<Double> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("NameExtractor", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Double {
        require(decoder is JsonDecoder)
        val jsonObject = decoder.decodeJsonElement().jsonObject
        return jsonObject["value"]!!.jsonPrimitive.double
    }

    override fun serialize(encoder: Encoder, value: Double) {
        encoder.encodeDouble(value)
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
    @Serializable(with = NameExtractorSerializer::class)
    val gender: String,
    val realm: Realm,
    @Serializable(with = NameExtractorSerializer::class)
    val guild: String? = null,
    val experience: Int
)

@Serializable
data class GetWowCharacterStatsResponse(
    val health: Int,
    val power: Int,
    @SerialName("power_type")
    @Serializable(with = NameExtractorSerializer::class)
    val powerType: String,
    @Serializable(with = EffectiveExtractorSerializer::class)
    val strength: Int,
    @Serializable(with = EffectiveExtractorSerializer::class)
    val agility: Int,
    @Serializable(with = EffectiveExtractorSerializer::class)
    val intellect: Int,
    @Serializable(with = EffectiveExtractorSerializer::class)
    val stamina: Int,
    @Serializable(with = ValueExtractorSerializer::class)
    @SerialName("melee_crit")
    val meleeCrit: Double,
    @SerialName("attack_power")
    val attackPower: Int,
    @SerialName("main_hand_damage_min")
    val mainHandDamageMin: Double,
    @SerialName("main_hand_damage_max")
    val mainHandDamageMax: Double,
    @SerialName("main_hand_speed")
    val mainHandSpeed: Double,
    @SerialName("main_hand_dps")
    val mainHandDps: Double,
    @SerialName("off_hand_damage_min")
    val offHandDamageMin: Double,
    @SerialName("off_hand_damage_max")
    val offHandDamageMax: Double,
    @SerialName("off_hand_speed")
    val offHandSpeed: Double,
    @SerialName("off_hand_dps")
    val offHandDps: Double,
    @SerialName("spell_power")
    val spellPower: Double,
    @SerialName("spell_penetration")
    val spellPenetration: Double,
    @Serializable(with = ValueExtractorSerializer::class)
    @SerialName("spell_crit")
    val spellCrit: Double,
    @SerialName("mana_regen")
    val manaRegen: Double,
    @SerialName("mana_regen_combat")
    val manaRegenCombat: Double,
    @Serializable(with = EffectiveExtractorSerializer::class)
    val armor: Int,
    @Serializable(with = ValueExtractorSerializer::class)
    val dodge: Double,
    @Serializable(with = ValueExtractorSerializer::class)
    val parry: Double,
    @Serializable(with = ValueExtractorSerializer::class)
    val block: Double,
    @SerialName("ranged_crit")
    @Serializable(with = ValueExtractorSerializer::class)
    val rangedCrit: Double,
    @Serializable(with = EffectiveExtractorSerializer::class)
    val spirit: Int,
    @Serializable(with = EffectiveExtractorSerializer::class)
    val defense: Int,
    @SerialName("fire_resistance")
    @Serializable(with = EffectiveExtractorSerializer::class)
    val fireResistance: Int,
    @SerialName("holy_resistance")
    @Serializable(with = EffectiveExtractorSerializer::class)
    val holyResistance: Int,
    @SerialName("shadow_resistance")
    @Serializable(with = EffectiveExtractorSerializer::class)
    val shadowResistance: Int,
    @Serializable(with = EffectiveExtractorSerializer::class)
    @SerialName("nature_resistance")
    val natureResistance: Int,
    @Serializable(with = EffectiveExtractorSerializer::class)
    @SerialName("arcane_resistance")
    val arcaneResistance: Int
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
data class GetWowSpecializationsResponse(
    @SerialName("specialization_groups")
    val specializationGroups: List<SpecializationGroup>
)

@Serializable
data class SpecializationGroup(
    val specializations: List<Specialization>,
)

@Serializable
data class Specialization(
    @SerialName("specialization_name")
    val specializationName: String,
    @SerialName("spent_points")
    val spentPoints: Int,
    val talents: List<Talent>
)

@Serializable
data class Talent(
    val talent: TalentInfo,
    @SerialName("talent_rank")
    val talentRank: Int
)

@Serializable
data class TalentInfo(
    val id: Int
)

@Serializable
data class GetWowRealmResponse(val category: String)

@Serializable
data class WowItem(
    val id: Long,
    val slot: String,
    val quality: String,
    val name: String,
    val icon: String?
)

@Serializable
data class WowResource(val type: String, val value: Int)

@Serializable
data class WowWeaponStats(
    val minDamage: Double,
    val maxDamage: Double,
    val speed: Double,
    val dps: Double
)

@Serializable
data class WowResistances(
    val fire: Int,
    val holy: Int,
    val shadow: Int,
    val nature: Int,
    val arcane: Int
)

@Serializable
data class WowStats(
    val health: Int,
    val resource: WowResource,
    val strength: Int,
    val agility: Int,
    val intellect: Int,
    val stamina: Int,
    val meleeCrit: Double,
    val attackPower: Int,
    val mainHandStats: WowWeaponStats,
    val offHandStats: WowWeaponStats,
    val spellPower: Double,
    val spellPenetration: Double,
    val spellCrit: Double,
    val manaRegen: Double,
    val manaRegenCombat: Double,
    val armor: Int,
    val dodge: Double,
    val parry: Double,
    val block: Double,
    val rangedCrit: Double,
    val spirit: Int,
    val defense: Int,
    val resistances: WowResistances
) {
    companion object {
        fun apply(response: GetWowCharacterStatsResponse): WowStats =
            WowStats(
                response.health,
                WowResource(response.powerType, response.power),
                response.strength,
                response.agility,
                response.intellect,
                response.stamina,
                response.meleeCrit,
                response.attackPower,
                WowWeaponStats(
                    response.mainHandDamageMin,
                    response.mainHandDamageMax,
                    response.mainHandSpeed,
                    response.mainHandDps
                ),
                WowWeaponStats(
                    response.offHandDamageMin,
                    response.offHandDamageMax,
                    response.offHandSpeed,
                    response.offHandDps
                ),
                response.spellPower,
                response.spellPenetration,
                response.spellCrit,
                response.manaRegen,
                response.manaRegenCombat,
                response.armor,
                response.dodge,
                response.parry,
                response.block,
                response.rangedCrit,
                response.spirit,
                response.defense,
                WowResistances(
                    response.fireResistance,
                    response.holyResistance,
                    response.shadowResistance,
                    response.natureResistance,
                    response.arcaneResistance
                )
            )
    }
}

@Serializable
data class WowTalent(
    val id: Int,
    val rank: Int
)

@Serializable
data class WowSpecialization(
    val name: String,
    val points: Int,
    val talents: List<WowTalent>
) {
    companion object {
        fun apply(specialization: Specialization): WowSpecialization =
            WowSpecialization(
                specialization.specializationName,
                specialization.spentPoints,
                specialization.talents.map {
                    WowTalent(
                        it.talent.id,
                        it.talentRank
                    )
                }
            )
    }
}

@Serializable
data class WowTalents(
    val wowHeadEmbeddedTalents: String,
    val specializations: List<WowSpecialization>
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
    val gender: String,
    val realm: String,
    val region: String,
    val guild: String?,
    val experience: Int,
    val items: List<WowItem>,
    val faction: String,
    val avatar: String?,
    val stats: WowStats,
    val specializations: WowTalents
) : Data {
    companion object {
        fun apply(
            region: String,
            characterResponse: GetWowCharacterResponse,
            mediaResponse: GetWowMediaResponse,
            alreadyExistentItems: List<WowItem>,
            equipmentResponse: List<Pair<WowItemResponse, GetWowMediaResponse?>>,
            statsResponse: GetWowCharacterStatsResponse,
            specializationsResponse: GetWowSpecializationsResponse,
            wowHeadEmbeddedResponse: RaiderioWowHeadEmbeddedResponse
        ) = HardcoreData(
            characterResponse.id,
            characterResponse.name,
            characterResponse.level,
            characterResponse.isDead ?: false,
            characterResponse.isSelfFound ?: false,
            characterResponse.averageItemLevel,
            characterResponse.equippedItemLevel,
            characterResponse.characterClass,
            characterResponse.race,
            characterResponse.gender,
            characterResponse.realm.name,
            region,
            characterResponse.guild,
            characterResponse.experience,
            alreadyExistentItems + equipmentResponse.map { (item, icon) ->
                WowItem(
                    item.item.id,
                    item.slot.name,
                    item.quality.type,
                    item.name,
                    icon?.assets?.find { it.key == "icon" }?.value
                )
            },
            characterResponse.faction,
            mediaResponse.assets.find { it.key == "avatar" }?.value,
            WowStats.apply(statsResponse),
            WowTalents(
                wowHeadEmbeddedTalents = wowHeadEmbeddedResponse.talentLoadout.wowheadCalculator,
                specializations = specializationsResponse.specializationGroups.firstOrNull()?.specializations?.map { specialization ->
                    WowSpecialization.apply(specialization)
                }.orEmpty()
            )
        )
    }
}