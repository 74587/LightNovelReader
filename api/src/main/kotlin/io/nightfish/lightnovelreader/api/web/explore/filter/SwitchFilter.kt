package io.nightfish.lightnovelreader.api.web.explore.filter

import io.nightfish.lightnovelreader.api.util.LocalString

abstract class SwitchFilter(
    private var title: LocalString,
    default: Boolean
): Filter<Boolean>(default) {
    override fun getTitle(): LocalString = title
}