package com.kos.common

interface WithState<T> {
    fun state(): T
}