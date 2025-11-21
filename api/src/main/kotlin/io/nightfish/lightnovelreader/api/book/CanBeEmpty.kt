package io.nightfish.lightnovelreader.api.book

interface CanBeEmpty {
    fun isEmpty(): Boolean
    fun isNotEmpty(): Boolean = !isEmpty()
}