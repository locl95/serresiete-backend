package com.kos.views

import com.kos.characters.Character
import com.kos.characters.CharacterRequest
import kotlinx.serialization.Serializable

enum class Game {
    WOW {
        override fun toString(): String = "wow"
    },
    LOL {
        override fun toString(): String = "lol"
    };

    companion object {
        fun fromString(value: String): Game = when (value) {
            "wow" -> WOW
            "lol" -> LOL
            else -> throw IllegalArgumentException("Unknown game: $value")
        }
    }
}

@Serializable
data class SimpleView(
    val id: String,
    val name: String,
    val owner: String,
    val published: Boolean,
    val characterIds: List<Long>,
    val game: Game
)

@Serializable
data class View(
    val id: String,
    val name: String,
    val owner: String,
    val published: Boolean,
    val characters: List<Character>,
    val game: Game
)

@Serializable
data class ViewRequest(val name: String, val published: Boolean, val characters: List<CharacterRequest>, val game: Game)

@Serializable
data class ViewPatchRequest(val name: String?, val published: Boolean?, val characters: List<CharacterRequest>?)

@Serializable
sealed interface ViewResult {
    val isSuccess: Boolean
}

@Serializable
data class ViewDeleted(val viewId: String) : ViewResult {
    override val isSuccess: Boolean = true
}

@Serializable
data class ViewModified(val viewId: String, val characters: List<Long>) : ViewResult {
    override val isSuccess: Boolean = true
}