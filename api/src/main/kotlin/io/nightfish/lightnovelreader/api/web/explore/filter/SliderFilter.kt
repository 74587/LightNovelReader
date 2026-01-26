package io.nightfish.lightnovelreader.api.web.explore.filter

import androidx.annotation.IntRange
import io.nightfish.lightnovelreader.api.util.LocalString

abstract class SliderFilter(
    private val title: LocalString,
    val description: String,
    defaultValue: Float,
    val valueRange: ClosedFloatingPointRange<Float>,
    @field:IntRange(from = 0) val steps: Int = 0,
) : Filter<Float>(defaultValue) {
    abstract var enabled: Boolean
    abstract val displayValue: String
    open val displayTitle = title
    override fun getTitle(): LocalString = title
}