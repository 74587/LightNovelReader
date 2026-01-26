package io.nightfish.lightnovelreader.api.web.search

import kotlinx.coroutines.flow.Flow

interface SearchProvider {
    /**
     * 搜索类型
     */
    val searchTypes: List<SearchType>
    /**
     * 执行搜索任务
     *
     * 应当返回搜索结果的数据流
     *
     * @param searchType 搜索类别
     * @param keyword 搜索关键词
     * @return 搜索结果的数据流
     */
    fun search(searchType: SearchType, keyword: String): Flow<SearchResult>

    /**
     * 用于提供搜索建议
     * 会在输入的关键词发生变化时调用
     *
     * @param history 用户的搜索历史记录, 按照从早到晚的顺序排序
     * @param keyword 当前输入的搜索关键词
     * @return 所有搜索建议
     */
    fun getSearchSuggestions(history: List<String>, keyword: String): List<String> =
        history.filter { it.startsWith(keyword) }
}