package io.nightfish.lightnovelreader.api.web.explore.filter

import io.nightfish.lightnovelreader.api.book.BookInformation
import io.nightfish.lightnovelreader.api.util.LocalString
import io.nightfish.lightnovelreader.api.util.local


class WordCountFilter : SliderFilter(
    title = "字数限制".local(),
    description = "仅显示字数大于该值的书本，若为0则显示全部书本。",
    defaultValue = 0f,
    valueRange = 0f..200_0000f,
    steps = 9
), LocalFilter {
    override var enabled: Boolean
        get() = value != 0f
        set(value) { if (!value) this.value = 0f }
    override val displayValue: String
        get() = if (value == 0f) "无限制" else "${(value / 1000).toInt()}K"

    override val displayTitle: LocalString = "字数".local()
    override fun filter(bookInformation: BookInformation): Boolean =
        !enabled || bookInformation.wordCount.count >= value
}