package io.nightfish.lightnovelreader.api.web.explore.filter

import io.nightfish.lightnovelreader.api.book.BookInformation
import io.nightfish.lightnovelreader.api.util.local

class IsCompletedSwitchFilter: SwitchFilter("已完结".local(), false), LocalFilter {
    override fun filter(bookInformation: BookInformation): Boolean =
        !this.value || bookInformation.isComplete
}