package com.kos.datacache

import arrow.core.Either
import com.kos.characters.CharactersTestHelper.basicLolCharacter
import com.kos.common.HttpError
import com.kos.httpclients.domain.*

object RiotMockHelper {
    val leagueEntries: Either<HttpError, List<LeagueEntryResponse>> =
        Either.Right(
            listOf(
                LeagueEntryResponse(
                    "RANKED_FLEX_SR",
                    "GOLD",
                    "I",
                    1,
                    13,
                    14,
                    false
                ),
                LeagueEntryResponse(
                    "RANKED_SOLO_5x5",
                    "GOLD",
                    "I",
                    1,
                    13,
                    14,
                    false
                )
            )
        )
    val matchId = "EUW1_232424252"
    val matches = Either.Right(listOf(matchId))
    val match: Either<HttpError, GetMatchResponse> = Either.Right(
        GetMatchResponse(
            MatchInfo(
                1,
                "COMPLETED",
                1,
                listOf(
                    MatchParticipant(
                        1,
                        basicLolCharacter.puuid,
                        1,
                        1,
                        1,
                        "SUPPORT",
                        "SUPPORT",
                        "BOT",
                        1,
                        1,
                        1,
                        18,
                        1,
                        25,
                        250,
                        10000
                    )
                )
            )
        )
    )

    val riotData: RiotData =
        RiotData(
            summonerIcon = 1389,
            summonerLevel = 499,
            summonerName = "GTP ZeroMVPs",
            leagues = mapOf(
                Pair(
                    "RANKED_SOLO_5x5",
                    LeagueProfile(
                        mainRole = "SUPPORT",
                        tier = "GOLD",
                        rank = "I",
                        leaguePoints = 1,
                        gamesPlayed = 27,
                        winrate = 0.5925925925925926,
                        matches = listOf(
                            MatchProfile(
                                championId = 497,
                                role = "SUPPORT",
                                individualPosition = "UTILITY",
                                lane = "BOTTOM",
                                kills = 2,
                                deaths = 7,
                                assists = 15,
                                assistMePings = 0,
                                visionWardsBoughtInGame = 8,
                                enemyMissingPings = 0,
                                wardsPlaced = 47,
                                gameDuration = 1883,
                                totalTimeSpentDead = 174
                            ), MatchProfile(
                                championId = 497,
                                role = "SUPPORT",
                                individualPosition = "UTILITY",
                                lane = "NONE",
                                kills = 0,
                                deaths = 2,
                                assists = 20,
                                assistMePings = 0,
                                visionWardsBoughtInGame = 5,
                                enemyMissingPings = 2,
                                wardsPlaced = 20,
                                gameDuration = 1146,
                                totalTimeSpentDead = 24
                            ), MatchProfile(
                                championId = 12,
                                role = "SUPPORT",
                                individualPosition = "UTILITY",
                                lane = "NONE",
                                kills = 2,
                                deaths = 2,
                                assists = 4,
                                assistMePings = 0,
                                visionWardsBoughtInGame = 6,
                                enemyMissingPings = 0,
                                wardsPlaced = 11,
                                gameDuration = 917,
                                totalTimeSpentDead = 36
                            ), MatchProfile(
                                championId = 497,
                                role = "SUPPORT",
                                individualPosition = "UTILITY",
                                lane = "BOTTOM",
                                kills = 2,
                                deaths = 3,
                                assists = 21,
                                assistMePings = 0,
                                visionWardsBoughtInGame = 16,
                                enemyMissingPings = 0,
                                wardsPlaced = 51,
                                gameDuration = 1712,
                                totalTimeSpentDead = 67
                            ), MatchProfile(
                                championId = 235,
                                role = "SUPPORT",
                                individualPosition = "UTILITY",
                                lane = "BOTTOM",
                                kills = 0,
                                deaths = 2,
                                assists = 13,
                                assistMePings = 0,
                                visionWardsBoughtInGame = 11,
                                enemyMissingPings = 0,
                                wardsPlaced = 32,
                                gameDuration = 1856,
                                totalTimeSpentDead = 73
                            )
                        )
                    )
                )
            )
        )


}