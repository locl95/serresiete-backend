package com.kos.credentials

import kotlin.test.Test
import kotlin.test.assertEquals

class CredentialsDomainTest {

    @Test
    fun `assert activity values`() {
        assertEquals("get any view", Activities.getAnyView)
        assertEquals("create a view", Activities.createViews)
        assertEquals("get any views", Activities.getAnyViews)
        assertEquals("get own views", Activities.getOwnViews)
        assertEquals("get own view", Activities.getOwnView)
        assertEquals("get view data", Activities.getViewData)
        assertEquals("get view cached data", Activities.getViewCachedData)
        assertEquals("edit own view", Activities.editOwnView)
        assertEquals("edit any view", Activities.editAnyView)
        assertEquals("delete own view", Activities.deleteOwnView)
        assertEquals("delete any view", Activities.deleteAnyView)
        assertEquals("create credentials", Activities.createCredentials)
        assertEquals("edit credentials", Activities.editCredentials)
        assertEquals("get any credentials roles", Activities.getAnyCredentialsRoles)
        assertEquals("login", Activities.login)
        assertEquals("login", Activities.logout)
    }
}