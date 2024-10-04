package com.kos.views

import com.kos.common.DatabaseFactory
import com.kos.views.ViewsTestHelper.basicSimpleWowView
import com.kos.views.ViewsTestHelper.id
import com.kos.views.ViewsTestHelper.name
import com.kos.views.ViewsTestHelper.owner
import com.kos.views.ViewsTestHelper.published
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
            val repositoryWithState = repository.withState(listOf(basicSimpleWowView))
            assertEquals(listOf(basicSimpleWowView), repositoryWithState.getOwnViews(owner))
        }
    }

    @Test
    fun `given a repository with views i can retrieve a certain view`() {
        runBlocking {
            val repositoryWithState = repository.withState(listOf(basicSimpleWowView))
            assertEquals(basicSimpleWowView, repositoryWithState.get(id))
        }
    }

    @Test
    fun `given an empty repository, trying to retrieve a certain view returns null`() {
        runBlocking {
            assertEquals(null, repository.get(id))
        }
    }

    @Test
    fun `given an empty repository i can insert views`() {
        runBlocking {
            assert(repository.create(name, owner, listOf(), Game.WOW).isSuccess)
            assert(repository.state().size == 1)
            assert(repository.state().all { it.owner == owner })
        }
    }

    @Test
    fun `given a repository with a view i can edit it`() {
        runBlocking {
            val repo =
                repository.withState(listOf(basicSimpleWowView))
            val edit = repo.edit(id, "name2", published, listOf(1))
            val finalState = repo.state()
            assertEquals(ViewModified(id, listOf(1)), edit)
            assertEquals(finalState, listOf(basicSimpleWowView.copy(name = "name2", characterIds = listOf(1))))
        }
    }

    @Test
    fun `given a repository with a view i can edit more than one character`() {
        runBlocking {
            val repositoryWithState = repository.withState(listOf(basicSimpleWowView))
            val edit = repositoryWithState.edit(id, "name", published, listOf(1, 2, 3, 4))
            val finalState = repositoryWithState.state()
            assertEquals(ViewModified(id, listOf(1, 2, 3, 4)), edit)
            assertEquals(finalState, listOf(basicSimpleWowView.copy(characterIds = listOf(1, 2, 3, 4))))
        }
    }

    @Test
    fun `given a repository with a view i can delete it`() {
        runBlocking {
            val repo = repository.withState(listOf(basicSimpleWowView))
            val delete = repo.delete(id)
            val finalState = repo.state()
            assertEquals(ViewDeleted(id), delete)
            assertEquals(finalState, listOf())
        }
    }

    @Test
    fun `given a repository with a view i can patch it`() {
        runBlocking {
            val repo = repository.withState(listOf(basicSimpleWowView))
            val patchedName = "new-name"
            val patch = repo.patch(basicSimpleWowView.id, patchedName, null, null)
            val patchedView = repo.state().first()
            assertEquals(ViewModified(basicSimpleWowView.id, basicSimpleWowView.characterIds), patch)
            assertEquals(basicSimpleWowView.id, patchedView.id)
            assertEquals(basicSimpleWowView.published, patchedView.published)
            assertEquals(basicSimpleWowView.characterIds, patchedView.characterIds)
            assertEquals(patchedName, patchedView.name)
        }
    }

    @Test
    fun `given a repository with a view i can patch more than one field`() {
        runBlocking {
            val repo = repository.withState(listOf(basicSimpleWowView))
            val characters: List<Long> = listOf(1, 2, 3)
            val patchedName = "new-name"
            val patchedPublish = false
            val patch = repo.patch(basicSimpleWowView.id, patchedName, patchedPublish, characters)
            val patchedView = repo.state().first()
            assertEquals(ViewModified(basicSimpleWowView.id, characters), patch)
            assertEquals(basicSimpleWowView.id, patchedView.id)
            assertEquals(patchedPublish, patchedView.published)
            assertEquals(characters, patchedView.characterIds)
            assertEquals(patchedName, patchedView.name)
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