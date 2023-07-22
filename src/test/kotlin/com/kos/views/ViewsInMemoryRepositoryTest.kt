package com.kos.views

import arrow.core.Either
import kotlin.test.Test
import kotlin.test.assertEquals
import com.kos.characters.Character
import com.kos.characters.CharacterRequest
import com.kos.views.repository.ViewsInMemoryRepository

class ViewsInMemoryRepositoryTest : ViewsRepositoryTest {
    @Test
    override fun ICanRetrieveViews() {
        val inMemoryRepository = ViewsInMemoryRepository(listOf(SimpleView("1", "a", listOf())))
        assertEquals(listOf(SimpleView("1", "a", listOf())), inMemoryRepository.getOwnViews("a"))
    }

    @Test
    override fun ICanRetriveACertainView() {
        val inMemoryRepository = ViewsInMemoryRepository(listOf(SimpleView("1", "a", listOf())))
        assertEquals((SimpleView("1", "a", listOf())), inMemoryRepository.get("1"))
    }

    @Test
    override fun IfNoViewsRetrievingReturnsNotFound() {
        val inMemoryRepository = ViewsInMemoryRepository()
        assertEquals(null, inMemoryRepository.get("1"))
    }

    @Test
    override fun ICanCreateAView() {
        val inMemoryRepository = ViewsInMemoryRepository()
        assert(inMemoryRepository.create("a", listOf()).isSuccess)
        assert(inMemoryRepository.state().size == 1)
        assert(inMemoryRepository.state().any { it.owner == "a" })
    }

    @Test
    override fun ICanEditAView() {
        val inMemoryRepository = ViewsInMemoryRepository(listOf(SimpleView("1", "a", listOf())))
        val edit = inMemoryRepository.edit("1", listOf(1))
        val finalState = inMemoryRepository.state()
        assertEquals(Either.Right(ViewSuccess("1")), edit)
        assertEquals(finalState, listOf(SimpleView("1", "a", listOf(1))))
    }

    @Test
    override fun IfNoViewsEditingReturnsNotFound() {
        val edit = ViewsInMemoryRepository().edit("1", listOf())
        assertEquals(Either.Left(ViewNotFound("1")), edit)
    }
}