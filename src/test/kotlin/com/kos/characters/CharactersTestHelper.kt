package com.kos.characters

import com.kos.characters.repository.CharactersState
import com.kos.riot.GetPUUIDResponse
import com.kos.riot.GetSummonerResponse

object CharactersTestHelper {
    val emptyCharactersState = CharactersState(listOf(), listOf())
    val basicRequest = WowCharacterRequest("kakarona", "eu", "zuljin")
    val basicLolCharacter = LolCharacter(999, "GTP ZeroMVPs", "WOW", "999", 1, "999", 1)
    val basicCharacter = basicRequest.toCharacter(1)
    val basicWowCharacter2 = WowCharacter(2, "layser", "eu", "zuljin")
    val basicGetSummonerResponse = GetSummonerResponse(
        "999",
        "999",
        "999",
        2,
        25,
        29
    )
    val basicGetPuuidResponse = GetPUUIDResponse(
        "999",
        "R7 Disney Girl",
        "EUW"
    )
}