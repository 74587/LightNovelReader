package io.nightfish.lightnovelreader.api.web.explore

import io.nightfish.lightnovelreader.api.explore.ExplorePage

interface ExploreTapPageDataSource {
    val title: String
    fun getExplorePage(): ExplorePage
}