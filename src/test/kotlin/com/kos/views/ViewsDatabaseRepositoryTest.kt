package com.kos.views

import com.kos.common.DatabaseFactory
import com.kos.views.repository.ViewsDatabaseRepository
import kotlinx.coroutines.runBlocking
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals

class ViewsDatabaseRepositoryTest : ViewsRepositoryTest {

    @Before
    fun beforeEach() {

        DatabaseFactory.init(mustClean = true)
    }

    @Test
    override fun ICanRetrieveViews() {
        val repository =
            runBlocking { ViewsDatabaseRepository().withState(listOf(SimpleView("1", "name", "a", listOf()))) }
        runBlocking { assertEquals(listOf(SimpleView("1", "name", "a", listOf())), repository.getOwnViews("a")) }
    }

    @Test
    override fun ICanRetriveACertainView() {
        val repository =
            runBlocking { ViewsDatabaseRepository().withState(listOf(SimpleView("1", "name", "a", listOf()))) }
        runBlocking { assertEquals((SimpleView("1", "name", "a", listOf())), repository.get("1")) }
    }

    @Test
    override fun IfNoViewsRetrievingReturnsNotFound() {
        val repository = ViewsDatabaseRepository()
        runBlocking { assertEquals(null, repository.get("1")) }
    }

    @Test
    override fun ICanCreateAView() {
        val repository = ViewsDatabaseRepository()
        runBlocking {
            assert(repository.create("name", "a", listOf()).isSuccess)
            assert(repository.state().size == 1)
            assert(repository.state().all { it.owner == "a" })
        }
    }

    @Test
    override fun ICanEditAView() {
        val repository =
            runBlocking { ViewsDatabaseRepository().withState(listOf(SimpleView("1", "name", "a", listOf()))) }
        val edit = runBlocking { repository.edit("1", "name2", listOf(1)) }
        val finalState = runBlocking { repository.state() }
        assertEquals(ViewSuccess("1"), edit)
        assertEquals(finalState, listOf(SimpleView("1", "name2", "a", listOf(1))))
    }

    @Test
    override fun ICanEditAViewModifyingMoreThanOneCharacter() {
        val repository =
            runBlocking { ViewsDatabaseRepository().withState(listOf(SimpleView("1", "name", "a", listOf(1)))) }
        val edit = runBlocking { repository.edit("1", "name", listOf(1, 2, 3, 4)) }
        val finalState = runBlocking { repository.state() }
        assertEquals(ViewSuccess("1"), edit)
        assertEquals(finalState, listOf(SimpleView("1", "name", "a", listOf(1, 2, 3, 4))))
    }

    @Test
    override fun ICanDeleteAView() {
        val repository =
            runBlocking { ViewsDatabaseRepository().withState(listOf(SimpleView("1", "name", "a", listOf(1)))) }
        val delete = runBlocking { repository.delete("1") }
        val finalState = runBlocking { repository.state() }
        assertEquals(ViewSuccess("1"), delete)
        assertEquals(finalState, listOf())
    }
}