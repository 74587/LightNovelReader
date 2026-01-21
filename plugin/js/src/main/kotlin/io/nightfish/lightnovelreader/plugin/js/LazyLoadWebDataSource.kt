package io.nightfish.lightnovelreader.plugin.js

import io.nightfish.lightnovelreader.api.web.WebBookDataSource

class LazyLoadWebDataSource(
    val dataSourceId: Int,
    val webBookDataSourceProvider: () -> WebBookDataSource
): WebBookDataSource {
    var bookDataSource: WebBookDataSource = EmptyWebDataSource
    override val id: Int get() = dataSourceId
    override suspend fun isOffLine() = bookDataSource.isOffLine()
    override val offLine get() = bookDataSource.offLine
    override val isOffLineFlow get() = bookDataSource.isOffLineFlow
    override val searchProvider get() = bookDataSource.searchProvider
    override val explorePageProvider get() = bookDataSource.explorePageProvider

    override suspend fun getBookInformation(id: String) = bookDataSource.getBookInformation(id)
    override suspend fun getBookVolumes(id: String) = bookDataSource.getBookVolumes(id)
    override suspend fun getChapterContent(chapterId: String, bookId: String) = bookDataSource.getChapterContent(chapterId, bookId)
    override fun onLoad() {
        bookDataSource = webBookDataSourceProvider.invoke()
    }
}