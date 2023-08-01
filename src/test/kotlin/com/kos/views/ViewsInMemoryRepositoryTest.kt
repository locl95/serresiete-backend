package com.kos.views

import com.kos.views.repository.ViewsInMemoryRepository
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class ViewsInMemoryRepositoryTest : ViewsRepositoryTest {
    @Test
    override fun ICanRetrieveViews() {
        val inMemoryRepository = ViewsInMemoryRepository(listOf(SimpleView("1", "a", listOf())))
        runBlocking {assertEquals(listOf(SimpleView("1", "a", listOf())), inMemoryRepository.getOwnViews("a")) }
    }

    @Test
    override fun ICanRetriveACertainView() {
        val inMemoryRepository = ViewsInMemoryRepository(listOf(SimpleView("1", "a", listOf())))
        runBlocking { assertEquals((SimpleView("1", "a", listOf())), inMemoryRepository.get("1")) }
    }

    @Test
    override fun IfNoViewsRetrievingReturnsNotFound() {
        val inMemoryRepository = ViewsInMemoryRepository()
        runBlocking { assertEquals(null, inMemoryRepository.get("1")) }
    }

    @Test
    override fun ICanCreateAView() {
        val inMemoryRepository = ViewsInMemoryRepository()
        runBlocking {
            assert(inMemoryRepository.create("a", listOf()).isSuccess)
            assert(inMemoryRepository.state().size == 1)
            assert(inMemoryRepository.state().all { it.owner == "a" })
        }
    }

    @Test
    override fun ICanEditAView() {
        val inMemoryRepository = ViewsInMemoryRepository(listOf(SimpleView("1", "a", listOf())))
        val edit = runBlocking { inMemoryRepository.edit("1", listOf(1)) }
        val finalState = runBlocking { inMemoryRepository.state() }
        assertEquals(ViewSuccess("1"), edit)
        assertEquals(finalState, listOf(SimpleView("1", "a", listOf(1))))
    }
}