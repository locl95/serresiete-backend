package com.kos.views

import com.kos.views.repository.ViewsDatabaseRepository
import com.kos.views.repository.ViewsInMemoryRepository
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class ViewsInMemoryRepositoryTest : ViewsRepositoryTest {
    @Test
    override fun ICanRetrieveViews() {
        val inMemoryRepository = ViewsInMemoryRepository(listOf(SimpleView("1", "name", "a", listOf())))
        runBlocking {
            assertEquals(
                listOf(SimpleView("1", "name", "a", listOf())),
                inMemoryRepository.getOwnViews("a")
            )
        }
    }

    @Test
    override fun ICanRetriveACertainView() {
        val inMemoryRepository = ViewsInMemoryRepository(listOf(SimpleView("1", "name", "a", listOf())))
        runBlocking { assertEquals((SimpleView("1", "name", "a", listOf())), inMemoryRepository.get("1")) }
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
            assert(inMemoryRepository.create("name", "a", listOf()).isSuccess)
            assert(inMemoryRepository.state().size == 1)
            assert(inMemoryRepository.state().all { it.owner == "a" })
        }
    }

    @Test
    override fun ICanEditAView() {
        val inMemoryRepository = ViewsInMemoryRepository(listOf(SimpleView("1", "name", "a", listOf())))
        val edit = runBlocking { inMemoryRepository.edit("1", "name2", listOf(1)) }
        val finalState = runBlocking { inMemoryRepository.state() }
        assertEquals(ViewSuccess("1"), edit)
        assertEquals(finalState, listOf(SimpleView("1", "name2", "a", listOf(1))))
    }

    @Test
    override fun ICanEditAViewModifyingMoreThanOneCharacter() {
        val repository = runBlocking { ViewsInMemoryRepository((listOf(SimpleView("1", "name", "a", listOf(1))))) }
        val edit = runBlocking { repository.edit("1", "name", listOf(1, 2, 3, 4)) }
        val finalState = runBlocking { repository.state() }
        assertEquals(ViewSuccess("1"), edit)
        assertEquals(finalState, listOf(SimpleView("1", "name", "a", listOf(1, 2, 3, 4))))
    }

    @Test
    override fun ICanDeleteAView() {
        val repository = runBlocking { ViewsInMemoryRepository((listOf(SimpleView("1", "name", "a", listOf(1))))) }
        val delete = runBlocking { repository.delete("1") }
        val finalState = runBlocking { repository.state() }
        assertEquals(ViewSuccess("1"), delete)
        assertEquals(finalState, listOf())
    }
}