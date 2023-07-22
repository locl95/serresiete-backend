package com.kos.views

import com.kos.characters.Character
import com.kos.characters.CharacterRequest
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class SimpleView(val id: String, val owner: String, val characterIds: List<Long>)

@Serializable
data class View(val id: String, val owner: String, val characters: List<Character>)

@Serializable
data class ViewRequest(val characters: List<CharacterRequest>)

@Serializable
sealed interface ViewResult {
    val viewId: String
    val isSuccess: Boolean
}

@Serializable
data class ViewSuccess(override val viewId: String) : ViewResult {
    override val isSuccess: Boolean = true
}

@Serializable
data class ViewNotFound(override val viewId: String) : ViewResult {
    override val isSuccess: Boolean = false
}