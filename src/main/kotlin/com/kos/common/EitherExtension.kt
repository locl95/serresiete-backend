package com.kos.common

import arrow.core.Either

fun<A,B> List<Either<A, B>>.split(): Pair<List<A>, List<B>> {
    val (lefts, rights) = this.partition { it.isLeft() }
    val leftList = lefts.map { it as Either.Left<A> }.map { it.value }
    val rightList = rights.map { it as Either.Right<B> }.map { it.value }
    return Pair(leftList, rightList)
}

fun<A,B> Either<A,B>.getLeftOrNull(): A? = this.swap().getOrNull()