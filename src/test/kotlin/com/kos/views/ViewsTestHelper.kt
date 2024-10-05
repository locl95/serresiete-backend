package com.kos.views

object ViewsTestHelper {

    val id = "1"
    val name = "name"
    val owner = "owner"
    val published = true
    val basicSimpleWowView = SimpleView(id, name, owner, published, listOf(), Game.WOW)
    val basicSimpleLolView = SimpleView(id, name, owner, published, listOf(), Game.LOL)
}

fun View.toSimple() = SimpleView(this.id, this.name, this.owner, this.published, this.characters.map { it.id }, this.game)