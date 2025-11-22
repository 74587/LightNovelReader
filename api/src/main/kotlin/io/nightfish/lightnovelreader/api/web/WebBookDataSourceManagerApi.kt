package io.nightfish.lightnovelreader.api.web

interface WebBookDataSourceManagerApi {
    fun registerWebDataSource(
        webBookDataSource: WebBookDataSource,
        webDataSourceItem: WebDataSourceItem
    )

    fun unregisterWebDataSource(webDataSourceId: Int)
    fun getWebDataSource(): WebBookDataSource
}