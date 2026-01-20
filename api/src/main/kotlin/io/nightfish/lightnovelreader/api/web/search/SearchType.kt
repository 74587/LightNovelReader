package io.nightfish.lightnovelreader.api.web.search

import io.nightfish.lightnovelreader.api.util.LocalString

data class SearchType(
    val type: String,
    val name: LocalString,
    val tip: LocalString
)
