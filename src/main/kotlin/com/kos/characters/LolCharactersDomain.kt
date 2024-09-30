package com.kos.characters

data class LolCharacter(
    override val id: Long,
    override val name: String,
    val tag: String,
    val puuid: String,
    val summonerIcon: Int,
    val summonerId: String,
    val summonerLevel: Int
) : Character

data class LolCharacterRequest(
    override val name: String,
    val tag: String
) : CharacterCreateRequest {

    override fun same(other: Character): Boolean {
        return when (other) {
            is LolCharacter -> this.name == other.name && this.tag == other.tag
            else -> false
        }
    }
}

data class LolCharacterEnrichedRequest(
    override val name: String,
    val tag: String,
    val puuid: String,
    val summonerIconId: Int,
    val summonerId: String,
    val summonerLevel: Int

) : CharacterInsertRequest {
    override fun toCharacter(id: Long): LolCharacter {
        return LolCharacter(id, this.name, this.tag, this.puuid, this.summonerIconId, this.summonerId, this.summonerLevel)
    }

    override fun same(other: Character): Boolean {
        return when (other) {
            is LolCharacter -> this.puuid === other.puuid && this.summonerId === other.summonerId
            else -> false
        }
    }
}