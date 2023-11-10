package com.kos.views

import com.kos.views.ViewsTestHelper.basicSimpleView
import com.kos.views.ViewsTestHelper.id
import com.kos.views.ViewsTestHelper.name
import com.kos.views.ViewsTestHelper.owner
import com.kos.views.repository.ViewsInMemoryRepository
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class ViewsInMemoryRepositoryTest : ViewsRepositoryTest {
    @Test
    override fun ICanRetrieveViews() {

        runBlocking {
            val inMemoryRepository = ViewsInMemoryRepository().withState(listOf(basicSimpleView))
            assertEquals(
                listOf(basicSimpleView),
                inMemoryRepository.getOwnViews(owner)
            )
        }
    }

    @Test
    override fun ICanRetriveACertainView() {
        runBlocking {
            val inMemoryRepository = ViewsInMemoryRepository().withState(listOf(basicSimpleView))
            assertEquals(basicSimpleView, inMemoryRepository.get(id))
        }
    }

    @Test
    override fun IfNoViewsRetrievingReturnsNotFound() {
        val inMemoryRepository = ViewsInMemoryRepository()
        runBlocking { assertEquals(null, inMemoryRepository.get(id)) }
    }

    @Test
    override fun ICanCreateAView() {
        val inMemoryRepository = ViewsInMemoryRepository()
        runBlocking {
            assert(inMemoryRepository.create(name, owner, listOf()).isSuccess)
            assert(inMemoryRepository.state().size == 1)
            assert(inMemoryRepository.state().all { it.owner == owner })
        }
    }

    @Test
    override fun ICanEditAView() {
        runBlocking {
            val inMemoryRepository = ViewsInMemoryRepository().withState(listOf(basicSimpleView))
            val edit = inMemoryRepository.edit(id, "name2", listOf(1))
            val finalState =  inMemoryRepository.state()
            assertEquals(ViewModified(id, listOf(1)), edit)
            assertEquals(finalState, listOf(basicSimpleView.copy(name = "name2", characterIds = listOf(1))))
        }
    }

    @Test
    override fun ICanEditAViewModifyingMoreThanOneCharacter() {
        runBlocking {
            val repository =  ViewsInMemoryRepository().withState((listOf(basicSimpleView.copy(characterIds = listOf(1)))))
            val edit = repository.edit(id, name, listOf(1, 2, 3, 4))
            val finalState = repository.state()
            assertEquals(ViewModified(id, listOf(1,2,3,4)), edit)
            assertEquals(finalState, listOf(basicSimpleView.copy(characterIds = listOf(1,2,3,4))))
        }
    }

    @Test
    override fun ICanDeleteAView() {
        runBlocking {
            val repository = ViewsInMemoryRepository().withState(listOf(basicSimpleView))
            val delete = repository.delete(id)
            val finalState =  repository.state()
            assertEquals(ViewDeleted(id), delete)
            assertEquals(finalState, listOf())
        }
    }
}