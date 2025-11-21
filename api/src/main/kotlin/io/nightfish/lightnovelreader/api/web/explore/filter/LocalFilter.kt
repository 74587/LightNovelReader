package io.nightfish.lightnovelreader.api.web.explore.filter

import io.nightfish.lightnovelreader.api.book.BookInformation

interface LocalFilter {
    fun filter(bookInformation: BookInformation): Boolean
}