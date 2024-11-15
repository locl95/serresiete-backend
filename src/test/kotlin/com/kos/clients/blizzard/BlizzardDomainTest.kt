package com.kos.clients.blizzard

import com.kos.clients.domain.GetWowCharacterResponse
import kotlinx.serialization.json.Json
import kotlin.test.Test
class BlizzardDomainTest {
    private val json = Json {
        ignoreUnknownKeys = true
    }

    @Test
    fun `i can deserialize a character response`() {
        val response = """
            {
                "_links": {
                    "self": {
                        "href": "https://eu.api.blizzard.com/profile/wow/character/stitches/kumii?namespace=profile-classic1x-eu"
                    }
                },
                "id": 30758927,
                "name": "Kumii",
                "gender": {
                    "type": "FEMALE",
                    "name": "Female"
                },
                "faction": {
                    "type": "ALLIANCE",
                    "name": "Alliance"
                },
                "race": {
                    "key": {
                        "href": "https://eu.api.blizzard.com/data/wow/playable-race/4?namespace=static-1.15.0_51892-classic1x-eu"
                    },
                    "name": "Night Elf",
                    "id": 4
                },
                "character_class": {
                    "key": {
                        "href": "https://eu.api.blizzard.com/data/wow/playable-class/3?namespace=static-1.15.0_51892-classic1x-eu"
                    },
                    "name": "Hunter",
                    "id": 3
                },
                "active_spec": {
                    "key": {
                        "href": "https://eu.api.blizzard.com/data/wow/playable-specialization/0?namespace=static-1.15.0_51892-classic1x-eu"
                    },
                    "id": 0
                },
                "realm": {
                    "key": {
                        "href": "https://eu.api.blizzard.com/data/wow/realm/5220?namespace=dynamic-classic1x-eu"
                    },
                    "name": "Stitches",
                    "id": 5220,
                    "slug": "stitches"
                },
                "guild": {
                    "key": {
                        "href": "https://eu.api.blizzard.com/data/wow/guild/stitches/i-cant-release?namespace=profile-classic1x-eu"
                    },
                    "name": "I CANT RELEASE",
                    "id": 1193006,
                    "realm": {
                        "key": {
                            "href": "https://eu.api.blizzard.com/data/wow/realm/5220?namespace=dynamic-classic1x-eu"
                        },
                        "name": "Stitches",
                        "id": 5220,
                        "slug": "stitches"
                    },
                    "faction": {
                        "type": "ALLIANCE",
                        "name": "Alliance"
                    }
                },
                "level": 60,
                "experience": 0,
                "titles": {
                    "href": "https://eu.api.blizzard.com/profile/wow/character/stitches/kumii/titles?namespace=profile-classic1x-eu"
                },
                "pvp_summary": {
                    "href": "https://eu.api.blizzard.com/profile/wow/character/stitches/kumii/pvp-summary?namespace=profile-classic1x-eu"
                },
                "media": {
                    "href": "https://eu.api.blizzard.com/profile/wow/character/stitches/kumii/character-media?namespace=profile-classic1x-eu"
                },
                "hunter_pets": {
                    "href": "https://eu.api.blizzard.com/profile/wow/character/stitches/kumii/hunter-pets?namespace=profile-classic1x-eu"
                },
                "last_login_timestamp": 1703019625000,
                "average_item_level": 59,
                "equipped_item_level": 57,
                "specializations": {
                    "href": "https://eu.api.blizzard.com/profile/wow/character/stitches/kumii/specializations?namespace=profile-classic1x-eu"
                },
                "statistics": {
                    "href": "https://eu.api.blizzard.com/profile/wow/character/stitches/kumii/statistics?namespace=profile-classic1x-eu"
                },
                "equipment": {
                    "href": "https://eu.api.blizzard.com/profile/wow/character/stitches/kumii/equipment?namespace=profile-classic1x-eu"
                },
                "appearance": {
                    "href": "https://eu.api.blizzard.com/profile/wow/character/stitches/kumii/appearance?namespace=profile-classic1x-eu"
                },
                "is_ghost": false
            }
        """.trimIndent()

        val foo = json.decodeFromString<GetWowCharacterResponse>(response)
        println(foo)
    }
}