package com.kos.views

import kotlin.test.Test


interface ViewsRepositoryTest {
    @Test
    fun ICanRetrieveViews(): Unit
    @Test
    fun ICanRetriveACertainView(): Unit
    @Test
    fun IfNoViewsRetrievingReturnsNotFound(): Unit
    @Test
    fun ICanCreateAView(): Unit
    @Test
    fun ICanEditAView(): Unit
}