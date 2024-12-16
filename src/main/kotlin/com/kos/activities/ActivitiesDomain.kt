package com.kos.activities

import kotlinx.serialization.Serializable

object Activities {
    const val deleteActivityFromRole = "delete activity from role"
    const val addActivityToRole= "add activity to role"
    const val deleteCredentials = "delete credentials"
    const val getAnyCredentials = "get any credentials"
    const val getAnyCredential = "get any credential"
    const val getAnyView = "get any view"
    const val createViews = "create a view"
    const val getAnyViews = "get any views"
    const val getOwnViews = "get own views"
    const val getOwnView = "get own view"
    const val getViewData = "get view data"
    const val getViewCachedData = "get view cached data"
    const val editOwnView = "edit own view"
    const val editAnyView = "edit any view"
    const val deleteOwnView = "delete own view"
    const val deleteAnyView = "delete any view"
    const val createCredentials = "create credentials"
    const val editCredentials = "edit credentials"
    const val getAnyCredentialsRoles = "get any credentials roles"
    const val getOwnCredentialsRoles = "get own credentials roles"
    const val addRoleToUser = "add role to user"
    const val deleteRoleFromUser = "delete role from user"
    const val getAnyActivities = "get any activities"
    const val getAnyRoles = "get any roles"
    const val createActivities = "create activities"
    const val createRoles = "create roles"
    const val deleteActivities = "delete activities"
    const val deleteRoles = "delete roles"
    const val login = "login"
    const val logout = "logout"
    const val runTask = "run task"
    const val getTask = "get task"
    const val getTasks = "get tasks"
    const val patchCredentials = "patch credentials"
    const val getQueueStatus = "get queue status"
}
typealias Activity = String

@Serializable
data class ActivityRequest(val activity: Activity)