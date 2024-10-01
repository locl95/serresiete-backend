package com.kos.httpclients.domain

import com.kos.characters.LolCharacter
import com.kos.common.HttpError
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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
    val goldEarned: Int
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

@Serializable
data class LeagueEntryResponse(
    val queueType: String,
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
    val kills: Int,
    val deaths: Int,
    val assists: Int,
    val assistMePings: Int
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
    val leagues: Map<String, LeagueProfile>
) : Data {
    companion object {
        fun apply(
            lolCharacter: LolCharacter,
            leagues: List<LeagueEntryResponse>,
            matches: List<GetMatchResponse>
        ): RiotData =
            RiotData(
                lolCharacter.summonerIcon,
                lolCharacter.summonerLevel,
                lolCharacter.name,
                leagues.associate { leagueEntryResponse ->
                    val gamesPlayed = leagueEntryResponse.wins + leagueEntryResponse.losses
                    val playerMatches =
                        matches.flatMap { getMatchResponse -> getMatchResponse.info.participants.filter { it.puuid == lolCharacter.puuid } }
                    leagueEntryResponse.queueType to LeagueProfile(
                        playerMatches.groupBy { it.role }.mapValues { it.value.size }.maxBy { it.value }.key,
                        leagueEntryResponse.tier,
                        leagueEntryResponse.rank,
                        leagueEntryResponse.leaguePoints,
                        gamesPlayed,
                        leagueEntryResponse.wins.toDouble() / gamesPlayed,
                        playerMatches.map {
                            MatchProfile(
                                it.championId,
                                it.kills,
                                it.deaths,
                                it.assists,
                                it.assistMePings
                            )
                        }
                    )
                }
            )
    }
}