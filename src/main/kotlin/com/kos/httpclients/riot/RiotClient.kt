package com.kos.httpclients.riot

import arrow.core.Either
import com.kos.common.HttpError
import com.kos.httpclients.domain.GetMatchResponse
import com.kos.httpclients.domain.GetPUUIDResponse
import com.kos.httpclients.domain.GetSummonerResponse
import com.kos.httpclients.domain.LeagueEntryResponse

interface RiotClient {
    suspend fun getPUUIDByRiotId(riotName: String, riotTag: String): Either<HttpError, GetPUUIDResponse>
    suspend fun getSummonerByPuuid(puuid: String): Either<HttpError, GetSummonerResponse>
    suspend fun getMatchesByPuuid(puuid: String, queue: Int): Either<HttpError, List<String>>
    suspend fun getMatchById(matchId: String): Either<HttpError, GetMatchResponse>
    suspend fun getLeagueEntriesBySummonerId(summonerId: String): Either<HttpError, List<LeagueEntryResponse>>
}