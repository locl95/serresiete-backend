package com.kos.characters

object CharactersTestHelper {
    val basicRequest = CharacterRequest("kakarona", "eu", "zuljin")
    val basicCharacter = basicRequest.toCharacter(1)
    val basicCharacter2 = Character(2, "layser", "eu", "zuljin")
}