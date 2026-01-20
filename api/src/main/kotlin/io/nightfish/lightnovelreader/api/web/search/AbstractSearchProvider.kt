package io.nightfish.lightnovelreader.api.web.search

import io.nightfish.lightnovelreader.api.util.LocalString

abstract class AbstractSearchProvider: SearchProvider {
    private  val _searchTypes = mutableListOf<SearchType>()
    override val searchTypes: List<SearchType> = _searchTypes

    protected fun registerSearchType(
        typeId: String,
        name: LocalString,
        tip: LocalString
    ) {
        _searchTypes.add(SearchType(typeId, name, tip))
    }
}