package com.kos.datacache

import arrow.core.Either
import com.kos.characters.WowCharacter
import com.kos.characters.WowCharacterRequest
import com.kos.common.HttpError
import com.kos.clients.domain.*
import kotlin.random.Random

object BlizzardMockHelper {

    fun getToken(): Either<HttpError, TokenResponse> {
        return Either.Right(TokenResponse("token", "token", 10, "sub"))
    }

    fun getCharacterProfile(wowCharacter: WowCharacter): Either<HttpError, GetWowCharacterResponse> {
        return Either.Right(
            GetWowCharacterResponse(
                wowCharacter.id,
                wowCharacter.name,
                60,
                false,
                false,
                50,
                50,
                "Hunter",
                "Alliance",
                "Night Elf",
                Realm("Stitches", 5220),
                "PERO LA QUERIA TANTO",
                0,
            )
        )
    }

    fun getCharacterMedia(wowCharacter: WowCharacter): Either<HttpError, GetWowMediaResponse> {
        return Either.Right(
            GetWowMediaResponse(listOf(AssetKeyValue("avatar", "${wowCharacter.id}")))
        )
    }

    fun getCharacterEquipment(wowCharacter: WowCharacter): Either<HttpError, GetWowEquipmentResponse> {
        return Either.Right(getWowEquipmentResponse)
    }

    fun getCharacterProfile(wowCharacter: WowCharacterRequest): Either<HttpError, GetWowCharacterResponse> {
        return Either.Right(
            GetWowCharacterResponse(
                Random.nextLong(),
                wowCharacter.name,
                60,
                false,
                false,
                50,
                50,
                "Hunter",
                "Alliance",
                "Night Elf",
                Realm("Stitches", 5220),
                "PERO LA QUERIA TANTO",
                0
            )
        )
    }

    val hardcoreRealm = GetWowRealmResponse("Hardcore")
    val notHardcoreRealm = GetWowRealmResponse("Not Hardcore")

    val getWowCharacterResponse = GetWowCharacterResponse(
        id = 30758927,
        name = "Kumii",
        level = 60,
        isDead = false,
        averageItemLevel = 59,
        equippedItemLevel = 57,
        characterClass = "Hunter",
        faction = "Alliance",
        race = "Night Elf",
        realm = Realm("Stitches", 5220),
        guild = "I CANT RELEASE",
        experience = 0
    )

    val getWowCharacterResponseString = """
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

    val getWowEquipmentResponseString = """
        {
          "_links": {
            "self": {
              "href": "https://eu.api.blizzard.com/profile/wow/character/stitches/kumii/equipment?namespace=profile-classic1x-eu"
            }
          },
          "character": {
            "key": {
              "href": "https://eu.api.blizzard.com/profile/wow/character/stitches/kumii?namespace=profile-classic1x-eu"
            },
            "name": "Kumii",
            "id": 30758927,
            "realm": {
              "key": {
                "href": "https://eu.api.blizzard.com/data/wow/realm/5220?namespace=dynamic-classic1x-eu"
              },
              "name": "Stitches",
              "id": 5220,
              "slug": "stitches"
            }
          },
          "equipped_items": [
            {
              "item": {
                "key": {
                  "href": "https://eu.api.blizzard.com/data/wow/item/18421?namespace=static-1.15.0_51892-classic1x-eu"
                },
                "id": 18421
              },
              "slot": {
                "type": "HEAD",
                "name": "Head"
              },
              "quantity": 1,
              "quality": {
                "type": "RARE",
                "name": "Rare"
              },
              "name": "Backwood Helm",
              "media": {
                "key": {
                  "href": "https://eu.api.blizzard.com/data/wow/media/item/18421?namespace=static-1.15.0_51892-classic1x-eu"
                },
                "id": 18421
              },
              "item_class": {
                "key": {
                  "href": "https://eu.api.blizzard.com/data/wow/item-class/4?namespace=static-1.15.0_51892-classic1x-eu"
                },
                "name": "Armor",
                "id": 4
              },
              "item_subclass": {
                "key": {
                  "href": "https://eu.api.blizzard.com/data/wow/item-class/4/item-subclass/3?namespace=static-1.15.0_51892-classic1x-eu"
                },
                "name": "Mail",
                "id": 3
              },
              "inventory_type": {
                "type": "HEAD",
                "name": "Head"
              },
              "binding": {
                "type": "ON_ACQUIRE",
                "name": "Binds when picked up"
              },
              "armor": {
                "value": 301,
                "display": {
                  "display_string": "301 Armor",
                  "color": {
                    "r": 255,
                    "g": 255,
                    "b": 255,
                    "a": 1.0
                  }
                }
              },
              "stats": [
                {
                  "type": {
                    "type": "AGILITY",
                    "name": "Agility"
                  },
                  "value": 21,
                  "display": {
                    "display_string": "+21 Agility",
                    "color": {
                      "r": 255,
                      "g": 255,
                      "b": 255,
                      "a": 1.0
                    }
                  }
                },
                {
                  "type": {
                    "type": "STAMINA",
                    "name": "Stamina"
                  },
                  "value": 13,
                  "display": {
                    "display_string": "+13 Stamina",
                    "color": {
                      "r": 255,
                      "g": 255,
                      "b": 255,
                      "a": 1.0
                    }
                  }
                },
                {
                  "type": {
                    "type": "INTELLECT",
                    "name": "Intellect"
                  },
                  "value": 9,
                  "display": {
                    "display_string": "+9 Intellect",
                    "color": {
                      "r": 255,
                      "g": 255,
                      "b": 255,
                      "a": 1.0
                    }
                  }
                },
                {
                  "type": {
                    "type": "SPIRIT",
                    "name": "Spirit"
                  },
                  "value": 9,
                  "display": {
                    "display_string": "+9 Spirit",
                    "color": {
                      "r": 255,
                      "g": 255,
                      "b": 255,
                      "a": 1.0
                    }
                  }
                }
              ],
              "spells": [
                {
                  "spell": {
                    "key": {
                      "href": "https://eu.api.blizzard.com/data/wow/spell/7597?namespace=static-1.15.0_51892-classic1x-eu"
                    },
                    "name": "Increased Critical 1",
                    "id": 7597
                  },
                  "description": "Equip: Improves your chance to get a critical strike by 1%."
                }
              ],
              "sell_price": {
                "value": 25741,
                "display_strings": {
                  "header": "Sell Price:",
                  "gold": "2",
                  "silver": "57",
                  "copper": "41"
                }
              },
              "durability": {
                "value": 53,
                "display_string": "Durability 53 / 70"
              }
            },
            {
              "item": {
                "key": {
                  "href": "https://eu.api.blizzard.com/data/wow/item/15411?namespace=static-1.15.0_51892-classic1x-eu"
                },
                "id": 15411
              },
              "slot": {
                "type": "NECK",
                "name": "Neck"
              },
              "quantity": 1,
              "quality": {
                "type": "RARE",
                "name": "Rare"
              },
              "name": "Mark of Fordring",
              "media": {
                "key": {
                  "href": "https://eu.api.blizzard.com/data/wow/media/item/15411?namespace=static-1.15.0_51892-classic1x-eu"
                },
                "id": 15411
              },
              "item_class": {
                "key": {
                  "href": "https://eu.api.blizzard.com/data/wow/item-class/4?namespace=static-1.15.0_51892-classic1x-eu"
                },
                "name": "Armor",
                "id": 4
              },
              "item_subclass": {
                "key": {
                  "href": "https://eu.api.blizzard.com/data/wow/item-class/4/item-subclass/0?namespace=static-1.15.0_51892-classic1x-eu"
                },
                "name": "Miscellaneous",
                "id": 0
              },
              "inventory_type": {
                "type": "NECK",
                "name": "Neck"
              },
              "binding": {
                "type": "ON_ACQUIRE",
                "name": "Binds when picked up"
              },
              "spells": [
                {
                  "spell": {
                    "key": {
                      "href": "https://eu.api.blizzard.com/data/wow/spell/7597?namespace=static-1.15.0_51892-classic1x-eu"
                    },
                    "name": "Increased Critical 1",
                    "id": 7597
                  },
                  "description": "Equip: Improves your chance to get a critical strike by 1%."
                },
                {
                  "spell": {
                    "key": {
                      "href": "https://eu.api.blizzard.com/data/wow/spell/9334?namespace=static-1.15.0_51892-classic1x-eu"
                    },
                    "name": "Attack Power 26",
                    "id": 9334
                  },
                  "description": "Equip: +26 Attack Power."
                }
              ],
              "sell_price": {
                "value": 10283,
                "display_strings": {
                  "header": "Sell Price:",
                  "gold": "1",
                  "silver": "2",
                  "copper": "83"
                }
              },
              "is_subclass_hidden": true
            },
            {
              "item": {
                "key": {
                  "href": "https://eu.api.blizzard.com/data/wow/item/13358?namespace=static-1.15.0_51892-classic1x-eu"
                },
                "id": 13358
              },
              "slot": {
                "type": "SHOULDER",
                "name": "Shoulders"
              },
              "quantity": 1,
              "quality": {
                "type": "RARE",
                "name": "Rare"
              },
              "name": "Wyrmtongue Shoulders",
              "media": {
                "key": {
                  "href": "https://eu.api.blizzard.com/data/wow/media/item/13358?namespace=static-1.15.0_51892-classic1x-eu"
                },
                "id": 13358
              },
              "item_class": {
                "key": {
                  "href": "https://eu.api.blizzard.com/data/wow/item-class/4?namespace=static-1.15.0_51892-classic1x-eu"
                },
                "name": "Armor",
                "id": 4
              },
              "item_subclass": {
                "key": {
                  "href": "https://eu.api.blizzard.com/data/wow/item-class/4/item-subclass/2?namespace=static-1.15.0_51892-classic1x-eu"
                },
                "name": "Leather",
                "id": 2
              },
              "inventory_type": {
                "type": "SHOULDER",
                "name": "Shoulder"
              },
              "binding": {
                "type": "ON_ACQUIRE",
                "name": "Binds when picked up"
              },
              "armor": {
                "value": 132,
                "display": {
                  "display_string": "132 Armor",
                  "color": {
                    "r": 255,
                    "g": 255,
                    "b": 255,
                    "a": 1.0
                  }
                }
              },
              "stats": [
                {
                  "type": {
                    "type": "AGILITY",
                    "name": "Agility"
                  },
                  "value": 23,
                  "display": {
                    "display_string": "+23 Agility",
                    "color": {
                      "r": 255,
                      "g": 255,
                      "b": 255,
                      "a": 1.0
                    }
                  }
                },
                {
                  "type": {
                    "type": "STAMINA",
                    "name": "Stamina"
                  },
                  "value": 10,
                  "display": {
                    "display_string": "+10 Stamina",
                    "color": {
                      "r": 255,
                      "g": 255,
                      "b": 255,
                      "a": 1.0
                    }
                  }
                }
              ],
              "sell_price": {
                "value": 21036,
                "display_strings": {
                  "header": "Sell Price:",
                  "gold": "2",
                  "silver": "10",
                  "copper": "36"
                }
              },
              "requirements": {
                "level": {
                  "value": 58,
                  "display_string": "Requires Level 58"
                }
              },
              "durability": {
                "value": 56,
                "display_string": "Durability 56 / 60"
              }
            },
            {
              "item": {
                "key": {
                  "href": "https://eu.api.blizzard.com/data/wow/item/859?namespace=static-1.15.0_51892-classic1x-eu"
                },
                "id": 859
              },
              "slot": {
                "type": "SHIRT",
                "name": "Shirt"
              },
              "quantity": 1,
              "quality": {
                "type": "COMMON",
                "name": "Common"
              },
              "name": "Fine Cloth Shirt",
              "media": {
                "key": {
                  "href": "https://eu.api.blizzard.com/data/wow/media/item/859?namespace=static-1.15.0_51892-classic1x-eu"
                },
                "id": 859
              },
              "item_class": {
                "key": {
                  "href": "https://eu.api.blizzard.com/data/wow/item-class/4?namespace=static-1.15.0_51892-classic1x-eu"
                },
                "name": "Armor",
                "id": 4
              },
              "item_subclass": {
                "key": {
                  "href": "https://eu.api.blizzard.com/data/wow/item-class/4/item-subclass/0?namespace=static-1.15.0_51892-classic1x-eu"
                },
                "name": "Miscellaneous",
                "id": 0
              },
              "inventory_type": {
                "type": "BODY",
                "name": "Shirt"
              },
              "binding": {
                "type": "ON_ACQUIRE",
                "name": "Binds when picked up"
              },
              "sell_price": {
                "value": 87,
                "display_strings": {
                  "header": "Sell Price:",
                  "gold": "0",
                  "silver": "0",
                  "copper": "87"
                }
              },
              "is_subclass_hidden": true
            }
          ]
        }
    """.trimIndent()

    val getWowEquipmentResponse = GetWowEquipmentResponse(
        equippedItems = listOf(
            WowItemResponse(
                item = WowItemId(id = 18421),
                slot = WowItemSlot(name = "Head"),
                quality = WowItemQuality(type = "RARE"),
                name = "Backwood Helm"
            ),
            WowItemResponse(
                item = WowItemId(id = 15411),
                slot = WowItemSlot(name = "Neck"),
                quality = WowItemQuality(type = "RARE"),
                name = "Mark of Fordring"
            ),
            WowItemResponse(
                item = WowItemId(id = 13358),
                slot = WowItemSlot(name = "Shoulders"),
                quality = WowItemQuality(type = "RARE"),
                name = "Wyrmtongue Shoulders"
            ),
            WowItemResponse(
                item = WowItemId(id = 859),
                slot = WowItemSlot(name = "Shirt"),
                quality = WowItemQuality(type = "COMMON"),
                name = "Fine Cloth Shirt"
            )
        )
    )



}