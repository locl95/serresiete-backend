package com.kos.credentials

import kotlinx.serialization.Serializable

@Serializable
data class Credentials(val userName: String, val password: String)

typealias Activity = String
typealias Role = String

object Activities {
    val getAnyView = "get any view"
    val createViews = "create a view"
    val getAnyViews = "get any views"
    val getOwnViews = "get own views"
    val getOwnView = "get own view"
    val getViewData = "get view data"
    val getViewCachedData = "get view cached data"
    val editOwnView = "edit own view"
    val editAnyView = "edit any view"
    val deleteOwnView = "delete own view"
    val deleteAnyView = "delete any view"
    val createCredentials = "create credentials"
    val login = "login"
    val logout = "login"
}