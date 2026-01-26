package io.nightfish.lightnovelreader.api.web.explore

abstract class AbstractDefaultExplorePageProvider: ExplorePageProvider.DefaultExplorePageProvider {
    override val explorePageIdList = mutableListOf<String>()
    override val exploreTapPageDataSourceMap = mutableMapOf<String, ExploreTapPageDataSource>()
    override val exploreExpandedPageDataSourceMap = mutableMapOf<String, ExploreExpandedPageDataSource>()

    private var index = 0

    protected fun registerTapPage(
        id: String,
        exploreTapPageDataSource: ExploreTapPageDataSource,
    ) {
        explorePageIdList.add(id)
        exploreTapPageDataSourceMap[id] = exploreTapPageDataSource
    }

    protected fun registerTapPage(
        exploreTapPageDataSource: ExploreTapPageDataSource,
    ) {
        index++
        registerTapPage(index.toString(), exploreTapPageDataSource)
    }


    protected fun registerExpandedPageDataSource(
        id: String,
        exploreExpandedPageDataSource: ExploreExpandedPageDataSource
    ) {
        exploreExpandedPageDataSourceMap[id] = exploreExpandedPageDataSource
    }
}