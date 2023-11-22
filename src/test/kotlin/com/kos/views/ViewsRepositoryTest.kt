package com.kos.views

import com.kos.common.DatabaseFactory
import com.kos.views.ViewsTestHelper.basicSimpleView
import com.kos.views.ViewsTestHelper.id
import com.kos.views.ViewsTestHelper.name
import com.kos.views.ViewsTestHelper.owner
import com.kos.views.repository.ViewsDatabaseRepository
import com.kos.views.repository.ViewsInMemoryRepository
import com.kos.views.repository.ViewsRepository
import kotlinx.coroutines.runBlocking
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

abstract class ViewsRepositoryTest {
    abstract val repository: ViewsRepository

    @BeforeTest
    abstract fun beforeEach()

    @Test
    fun `given a repository with views i can retrieve them`() {
        runBlocking {
            val repositoryWithState = repository.withState(listOf(basicSimpleView))
            assertEquals(listOf(basicSimpleView), repositoryWithState.getOwnViews(owner))
        }
    }

    @Test
    fun `given a repository with views i can retrieve a certain view`() {
        runBlocking {
            val repositoryWithState = repository.withState(listOf(basicSimpleView))
            assertEquals((SimpleView(id, name, owner, listOf())), repositoryWithState.get(id))
        }
    }

    @Test
    fun `given an empty repository, trying to retrieve a certain guild returns null`() {
        runBlocking {
            assertEquals(null, repository.get(id))
        }
    }

    @Test
    fun `given an empty repository i can insert views`() {
        runBlocking {
            assert(repository.create(name, owner, listOf()).isSuccess)
            assert(repository.state().size == 1)
            assert(repository.state().all { it.owner == owner })
        }
    }

    @Test
    fun `given a repository with a view i can edit it`() {
        runBlocking {
            val repo =
                repository.withState(listOf(basicSimpleView))
            val edit = repo.edit(id, "name2", listOf(1))
            val finalState = repo.state()
            assertEquals(ViewModified(id, listOf(1)), edit)
            assertEquals(finalState, listOf(basicSimpleView.copy(name = "name2", characterIds = listOf(1))))
        }
    }

    @Test
    fun `given a repository with a view i can edit more than one character`() {
        runBlocking {
            val repositoryWithState = repository.withState(listOf(basicSimpleView))
            val edit = repositoryWithState.edit(id, "name", listOf(1, 2, 3, 4))
            val finalState = repositoryWithState.state()
            assertEquals(ViewModified(id, listOf(1, 2, 3, 4)), edit)
            assertEquals(finalState, listOf(basicSimpleView.copy(characterIds = listOf(1, 2, 3, 4))))
        }
    }

    @Test
    fun `given a repository with a view i can delete it`() {
        runBlocking {
            val repo = repository.withState(listOf(basicSimpleView))
            val delete = repo.delete(id)
            val finalState = repo.state()
            assertEquals(ViewDeleted(id), delete)
            assertEquals(finalState, listOf())
        }
    }
}

class ViewsInMemoryRepositoryTest : ViewsRepositoryTest() {
    override val repository = ViewsInMemoryRepository()
    override fun beforeEach() {
        repository.clear()
    }
}

class ViewsDatabaseRepositoryTest : ViewsRepositoryTest() {
    override val repository = ViewsDatabaseRepository()
    override fun beforeEach() {
        DatabaseFactory.init(mustClean = true)
    }
}