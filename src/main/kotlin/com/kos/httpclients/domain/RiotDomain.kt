package com.kos.httpclients.domain

import com.kos.characters.LolCharacter
import com.kos.common.HttpError
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable
data class GetPUUIDResponse(val puuid: String, val gameName: String, val tagLine: String)

@Serializable
data class GetSummonerResponse(
    val id: String,
    val accountId: String,
    val puuid: String,
    val profileIconId: Int,
    val revisionDate: Long,
    val summonerLevel: Int
)

@Serializable
data class MatchParticipant(
    val assistMePings: Int,
    val puuid: String,
    val visionWardsBoughtInGame: Int,
    val wardsPlaced: Int,
    val visionScore: Int,
    val role: String,
    val individualPosition: String,
    val lane: String,
    val kills: Int,
    val enemyMissingPings: Int,
    val deaths: Int,
    val championId: Int,
    val assists: Int,
    val totalTimeSpentDead: Int,
    val totalMinionsKilled: Int,
    val goldEarned: Int,
    val win: Boolean
)

@Serializable
data class MatchInfo(
    val gameDuration: Int,
    val endOfGameResult: String,
    val mapId: Int,
    val participants: List<MatchParticipant>
)

@Serializable
data class GetMatchResponse(
    val info: MatchInfo
)

@Serializable(with = QueueTypeSerializer::class)
enum class QueueType {
    SOLO_Q {
        override fun toInt(): Int = 420
        override fun toString(): String = "RANKED_SOLO_5x5"
    },
    FLEX_Q {
        override fun toInt(): Int = 440
        override fun toString(): String = "RANKED_FLEX_SR"
    };

    abstract fun toInt(): Int
}

object QueueTypeSerializer : KSerializer<QueueType> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("QueueType", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: QueueType) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): QueueType {
        return when (val queueType = decoder.decodeString()) {
            "RANKED_SOLO_5x5" -> QueueType.SOLO_Q
            "RANKED_FLEX_SR" -> QueueType.FLEX_Q
            else -> throw IllegalArgumentException("Unknown queue type: $queueType")
        }
    }
}

@Serializable
data class LeagueEntryResponse(
    val queueType: QueueType,
    val tier: String,
    val rank: String,
    val leaguePoints: Int,
    val wins: Int,
    val losses: Int,
    val hotStreak: Boolean
)

@Serializable
data class RiotStatus(
    @SerialName("status_code")
    val statusCode: Int,
    val message: String
)

data class RiotError(val status: RiotStatus) : HttpError {
    override fun error(): String = "${status.statusCode} ${status.message}"
}

@Serializable
data class MatchProfile(
    val championId: Int,
    val role: String,
    val individualPosition: String,
    val lane: String,
    val kills: Int,
    val deaths: Int,
    val assists: Int,
    val assistMePings: Int,
    val visionWardsBoughtInGame: Int,
    val enemyMissingPings: Int,
    val wardsPlaced: Int,
    val gameDuration: Int,
    val totalTimeSpentDead: Int,
    val win: Boolean
)

@Serializable
data class LeagueProfile(
    val mainRole: String,
    val tier: String,
    val rank: String,
    val leaguePoints: Int,
    val gamesPlayed: Int,
    val winrate: Double,
    val matches: List<MatchProfile>
)

@Serializable
data class RiotData(
    val summonerIcon: Int,
    val summonerLevel: Int,
    val summonerName: String,
    val leagues: Map<QueueType, LeagueProfile>
) : Data {
    companion object {

        fun apply(
            lolCharacter: LolCharacter,
            leagues: List<Pair<LeagueEntryResponse, List<GetMatchResponse>>>
        ): RiotData =
            RiotData(
                lolCharacter.summonerIcon,
                lolCharacter.summonerLevel,
                lolCharacter.name,
                leagues.associate { leagueEntryResponseAndMatches ->
                    val leagueEntryResponse = leagueEntryResponseAndMatches.first
                    val matches = leagueEntryResponseAndMatches.second
                    val gamesPlayed = leagueEntryResponse.wins + leagueEntryResponse.losses
                    val playerMatches: List<MatchProfile> =
                        matches.flatMap { getMatchResponse ->
                            getMatchResponse.info.participants.filter { it.puuid == lolCharacter.puuid }.map {
                                MatchProfile(
                                    it.championId,
                                    it.role,
                                    it.individualPosition,
                                    it.lane,
                                    it.kills,
                                    it.deaths,
                                    it.assists,
                                    it.assistMePings,
                                    it.visionWardsBoughtInGame,
                                    it.enemyMissingPings,
                                    it.wardsPlaced,
                                    getMatchResponse.info.gameDuration,
                                    it.totalTimeSpentDead,
                                    it.win
                                )
                            }
                        }
                    leagueEntryResponse.queueType to LeagueProfile(
                        playerMatches.groupBy { it.role }.mapValues { it.value.size }.maxBy { it.value }.key,
                        leagueEntryResponse.tier,
                        leagueEntryResponse.rank,
                        leagueEntryResponse.leaguePoints,
                        gamesPlayed,
                        leagueEntryResponse.wins.toDouble() / gamesPlayed,
                        playerMatches
                    )
                }
            )

    }
}