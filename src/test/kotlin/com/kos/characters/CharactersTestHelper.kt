package com.kos.characters

import com.kos.characters.repository.CharactersState
import com.kos.httpclients.domain.GetAccountResponse
import com.kos.httpclients.domain.GetPUUIDResponse
import com.kos.httpclients.domain.GetSummonerResponse

object CharactersTestHelper {
    val emptyCharactersState = CharactersState(listOf(), listOf())
    val basicWowRequest = WowCharacterRequest("kakarona", "eu", "zuljin")
    val basicWowRequest2 = WowCharacterRequest("layser", "eu", "zuljin")
    val basicLolCharacter = LolCharacter(1, "GTP ZeroMVPs", "WOW", "1", 1, "1", 1)
    val basicLolCharacterEnrichedRequest = LolCharacterEnrichedRequest(
        basicLolCharacter.name,
        basicLolCharacter.tag,
        basicLolCharacter.puuid,
        basicLolCharacter.summonerIcon,
        basicLolCharacter.summonerId,
        basicLolCharacter.summonerLevel
    )
    val basicWowCharacter = basicWowRequest.toCharacter(1)
    val basicWowCharacter2 = basicWowRequest2.toCharacter(2)
    val basicGetSummonerResponse = GetSummonerResponse(
        "1",
        "1",
        "1",
        2,
        25,
        29
    )
    val basicGetAccountResponse = GetAccountResponse(
        "Marcnute",
        "EUW"
    )
    val basicGetPuuidResponse = GetPUUIDResponse(
        "1",
        "R7 Disney Girl",
        "EUW"
    )
    val gigaLolCharacterRequestList = List(14) { index ->
        LolCharacterRequest("sanxei$index", "euw100$index")
    }
    val gigaLolCharacterList = List(7) { index ->
        LolCharacter(index.toLong(), "sanxei$index", "euw100$index", "puuid", index, "summonerId", 400)
    }
}