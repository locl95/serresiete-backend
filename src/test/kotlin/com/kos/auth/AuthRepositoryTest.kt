package com.kos.auth

import kotlin.test.Test


interface AuthRepositoryTest {
    @Test
    fun ICanGetAuthorizations()
    @Test
    fun ICanInsertAuthorizations(): Unit
    @Test
    fun ICanDeleteAuthorizations(): Unit
}