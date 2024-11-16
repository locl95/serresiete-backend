package com.kos.views

object ViewsTestHelper {

    val id = "1"
    val name = "name"
    val owner = "owner"
    val published = true
    val basicSimpleWowView = SimpleView(id, name, owner, published, listOf(), Game.WOW)
    val basicSimpleLolView = SimpleView(id, name, owner, published, listOf(), Game.LOL)
    val basicSimpleLolViews = listOf(
        basicSimpleLolView,
        basicSimpleLolView.copy(id = "2")
    )
    val basicSimpleGameViews = listOf(
        basicSimpleLolView,
        basicSimpleLolView.copy(id = "2"),
        basicSimpleWowView.copy(id = "3")
    )
}

fun View.toSimple() =
    SimpleView(this.id, this.name, this.owner, this.published, this.characters.map { it.id }, this.game)