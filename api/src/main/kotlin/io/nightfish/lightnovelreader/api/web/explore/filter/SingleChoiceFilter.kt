package io.nightfish.lightnovelreader.api.web.explore.filter

import io.nightfish.lightnovelreader.api.util.LocalString

open class SingleChoiceFilter(
    private val title: LocalString,
    val dialogTitle: LocalString,
    val description: LocalString,
    private val choices: List<String>,
    private val defaultChoice: String
): Filter<String>(defaultChoice) {
    override fun getTitle(): LocalString = title
    fun getAllChoices(): List<String> = choices
    fun getDefaultChoice(): String = defaultChoice
}