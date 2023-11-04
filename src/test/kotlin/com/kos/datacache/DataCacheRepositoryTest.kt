package com.kos.datacache

import kotlin.test.Test

interface DataCacheRepositoryTest {
    @Test
    fun ICanInsertData()
    @Test
    fun ICanUpdateData()
    @Test
    fun ICanUpdateDataWithMoreThan2Characters()
    @Test
    fun ICanGetData()
}