package com.kos.views

import arrow.core.Either
import com.kos.characters.Character
import com.kos.characters.CharacterCreateRequest
import com.kos.common.InvalidGameType
import com.kos.httpclients.domain.Data
import kotlinx.serialization.Serializable

@Serializable
enum class Game {
    WOW {
        override fun toString(): String = "wow"
    },
    LOL {
        override fun toString(): String = "lol"
    };

    companion object {
        fun fromString(value: String): Either<InvalidGameType, Game> = when (value) {
            "wow" -> Either.Right(WOW)
            "lol" -> Either.Right(LOL)
            else -> Either.Left(InvalidGameType(value))
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

//TODO: We need to decode/encode the character request based on Game.
//TODO: Right now we are using the type discriminator and that's cringe as fuck.
@Serializable
data class ViewRequest(
    val name: String,
    val published: Boolean,
    val characters: List<CharacterCreateRequest>,
    val game: Game,
)

@Serializable
data class ViewPatchRequest(
    val name: String? = null,
    val published: Boolean? = null,
    val characters: List<CharacterCreateRequest>? = null,
    val game: Game
)

@Serializable
sealed interface ViewResult {
    val isSuccess: Boolean
}

@Serializable
data class ViewDeleted(val viewId: String) : ViewResult {
    override val isSuccess: Boolean = true
}

@Serializable
data class ViewModified(val viewId: String, val name: String, val published: Boolean, val characters: List<Long>) :
    ViewResult {
    override val isSuccess: Boolean = true
}

@Serializable
data class ViewPatched(val viewId: String, val name: String?, val published: Boolean?, val characters: List<Long>?) :
    ViewResult {
    override val isSuccess: Boolean = true
}

@Serializable
data class ViewData(val viewName: String, val data: List<Data>)