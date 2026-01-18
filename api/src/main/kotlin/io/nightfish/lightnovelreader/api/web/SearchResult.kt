package io.nightfish.lightnovelreader.api.web

import io.nightfish.lightnovelreader.api.book.BookInformation

sealed class SearchResult {
    class SingleBook(
        val bookId: String
    ): SearchResult()

    class MultipleBook(
        val bookInformation: BookInformation
    ): SearchResult()

    class Error(
        val error: Throwable
    ): SearchResult() {
        constructor(message: String): this(kotlin.Error(message))
    }

    class End: SearchResult()
}