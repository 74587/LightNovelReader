package io.nightfish.lightnovelreader.api.web.explore.filter

import io.nightfish.lightnovelreader.api.book.BookInformation

class IsCompletedSwitchFilter(
    onChange: () -> Unit
): SwitchFilter("已完结", onChange), LocalFilter {
    override fun filter(bookInformation: BookInformation): Boolean =
        !this.enabled || bookInformation.isComplete
}