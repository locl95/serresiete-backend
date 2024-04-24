package com.kos.activities

import kotlinx.serialization.Serializable

object Activities {
    val deleteActivityFromRole = "delete activity from role"
    val addActivityToRole= "add activity to role"
    val deleteCredentials = "delete credentials"
    val getAnyCredentials = "get any credentials"
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
    val editCredentials = "edit credentials"
    val getAnyCredentialsRoles = "get any credentials roles"
    val getOwnCredentialsRoles = "get own credentials roles"
    val addRoleToUser = "add role to user"
    val getAnyActivities = "get any activities"
    val getAnyRoles = "get any roles"
    val createActivities = "create activities"
    val createRoles = "create roles"
    val deleteActivities = "delete activities"
    val deleteRoles = "delete roles"
    val login = "login"
    val logout = "logout"
}
typealias Activity = String

@Serializable
data class ActivityRequest(val activity: Activity)