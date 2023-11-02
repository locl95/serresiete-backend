package com.kos.views

import com.kos.common.DatabaseFactory
import com.kos.views.repository.ViewsDatabaseRepository
import kotlinx.coroutines.runBlocking
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals

class ViewsDatabaseRepositoryTest : ViewsRepositoryTest {

   @Before fun beforeEach() {

       DatabaseFactory.init(mustClean = true)
   }

    @Test
    override fun ICanRetrieveViews() {
        val repository = runBlocking { ViewsDatabaseRepository().withState(listOf(SimpleView("1", "a", listOf()))) }
        runBlocking {assertEquals(listOf(SimpleView("1", "a", listOf())), repository.getOwnViews("a")) }
    }

    @Test
    override fun ICanRetriveACertainView() {
        val repository = runBlocking { ViewsDatabaseRepository().withState(listOf(SimpleView("1", "a", listOf()))) }
        runBlocking { assertEquals((SimpleView("1", "a", listOf())), repository.get("1")) }
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
            assert(repository.create("a", listOf()).isSuccess)
            assert(repository.state().size == 1)
            assert(repository.state().all { it.owner == "a" })
        }
    }

    @Test
    override fun ICanEditAView() {
        val repository = runBlocking {ViewsDatabaseRepository().withState(listOf(SimpleView("1", "a", listOf()))) }
        val edit = runBlocking { repository.edit("1", listOf(1)) }
        val finalState = runBlocking { repository.state() }
        assertEquals(ViewSuccess("1"), edit)
        assertEquals(finalState, listOf(SimpleView("1", "a", listOf(1))))
    }

    @Test
    override fun ICanEditAViewModifyingMoreThanOneCharacter() {
        val repository = runBlocking {ViewsDatabaseRepository().withState(listOf(SimpleView("1", "a", listOf(1)))) }
        val edit = runBlocking { repository.edit("1", listOf(1,2,3,4)) }
        val finalState = runBlocking { repository.state() }
        assertEquals(ViewSuccess("1"), edit)
        assertEquals(finalState, listOf(SimpleView("1", "a", listOf(1,2,3,4))))
    }
}