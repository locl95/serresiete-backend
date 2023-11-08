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
        runBlocking {
            val repository = ViewsDatabaseRepository().withState(listOf(SimpleView("1", "name", "a", listOf())))
            assertEquals(listOf(SimpleView("1", "name", "a", listOf())), repository.getOwnViews("a"))
        }
    }

    @Test
    override fun ICanRetriveACertainView() {
        runBlocking {
            val repository = ViewsDatabaseRepository().withState(listOf(SimpleView("1", "name", "a", listOf())))
            assertEquals((SimpleView("1", "name", "a", listOf())), repository.get("1"))
        }
    }

    @Test
    override fun IfNoViewsRetrievingReturnsNotFound() {
        runBlocking {
            val repository = ViewsDatabaseRepository()
            assertEquals(null, repository.get("1"))
        }
    }

    @Test
    override fun ICanCreateAView() {
        runBlocking {
            val repository = ViewsDatabaseRepository()
            assert(repository.create("name", "a", listOf()).isSuccess)
            assert(repository.state().size == 1)
            assert(repository.state().all { it.owner == "a" })
        }
    }

    @Test
    override fun ICanEditAView() {
        runBlocking {
            val repository =
                ViewsDatabaseRepository().withState(listOf(SimpleView("1", "name", "a", listOf())))
            val edit = repository.edit("1", "name2", listOf(1))
            val finalState = repository.state()
            assertEquals(ViewSuccess("1"), edit)
            assertEquals(finalState, listOf(SimpleView("1", "name2", "a", listOf(1))))
        }
    }

    @Test
    override fun ICanEditAViewModifyingMoreThanOneCharacter() {
        runBlocking {
            val repository = ViewsDatabaseRepository().withState(listOf(SimpleView("1", "name", "a", listOf(1))))
            val edit = repository.edit("1", "name", listOf(1, 2, 3, 4))
            val finalState = repository.state()
            assertEquals(ViewSuccess("1"), edit)
            assertEquals(finalState, listOf(SimpleView("1", "name", "a", listOf(1, 2, 3, 4))))
        }
    }

    @Test
    override fun ICanDeleteAView() {
        runBlocking {
            val repository = ViewsDatabaseRepository().withState(listOf(SimpleView("1", "name", "a", listOf(1))))
            val delete = repository.delete("1")
            val finalState = repository.state()
            assertEquals(ViewSuccess("1"), delete)
            assertEquals(finalState, listOf())
        }
    }

}