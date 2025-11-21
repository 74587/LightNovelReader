package indi.dmzz_yyhyy.lightnovelreader.defaultplugin.wenku8.explore

import androidx.core.net.toUri
import indi.dmzz_yyhyy.lightnovelreader.defaultplugin.wenku8.Wenku8Api.host
import indi.dmzz_yyhyy.lightnovelreader.defaultplugin.wenku8.wenku8Cookie
import indi.dmzz_yyhyy.lightnovelreader.utils.autoReconnectionGet
import io.nightfish.lightnovelreader.api.explore.ExploreBooksRow
import io.nightfish.lightnovelreader.api.explore.ExploreDisplayBook
import io.nightfish.lightnovelreader.api.explore.ExplorePage
import io.nightfish.lightnovelreader.api.web.explore.ExplorePageDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

object Wenku8AllExplorePage: ExplorePageDataSource {
    private var lock = false
    private val exploreBooksRows: MutableStateFlow<List<ExploreBooksRow>> = MutableStateFlow(emptyList())

    override val title = "全部"

    override fun getExplorePage(): ExplorePage  {
        if (!lock) {
            lock = true
            CoroutineScope(Dispatchers.IO).launch {
                exploreBooksRows.update {
                    it + getAllBookBooksRow().copy(expandable = true, expandedPageDataSourceId = "allBook")
                }
                exploreBooksRows.update {
                    it + getTopListBookBooksRow("热门轻小说", "allvisit")
                }
                exploreBooksRows.update {
                    it + getTopListBookBooksRow("动画化作品", "anime")
                }
                exploreBooksRows.update {
                    it + getTopListBookBooksRow("今日更新", "lastupdate")
                }
                exploreBooksRows.update {
                    it + getTopListBookBooksRow("新书一览", "postdate")
                }
                exploreBooksRows.update {
                    it + getCompletedBooksRow().copy(expandable = true, expandedPageDataSourceId = "allCompletedBook")
                }
            }
        }
        return ExplorePage("首页", exploreBooksRows)
    }

    private suspend fun getCompletedBooksRow(): ExploreBooksRow {
        val soup = Jsoup
            .connect("${host}/modules/article/articlelist.php?fullflag=1")
            .wenku8Cookie()
            .autoReconnectionGet()
        return getBooksRow(soup, "完结全本").copy(
            expandable = true,
            expandedPageDataSourceId = "allBook"
        )
    }

    private suspend fun getTopListBookBooksRow(title: String, sort: String): ExploreBooksRow {
        val soup = Jsoup
            .connect("${host}/modules/article/toplist.php?sort=$sort")
            .wenku8Cookie()
            .autoReconnectionGet()
        return getBooksRow(soup, title).copy(
            expandable = true,
            expandedPageDataSourceId = "${sort}Book"
        )
    }

    private suspend fun getAllBookBooksRow(): ExploreBooksRow {
        val soup = Jsoup
            .connect("${host}/modules/article/articlelist.php")
            .wenku8Cookie()
            .autoReconnectionGet()
        return getBooksRow(soup, "轻小说列表")
    }

    private fun getBooksRow(soup: Document?, title: String): ExploreBooksRow {
        val idlList = soup?.select("#content > table.grid > tbody > tr > td > div > div:nth-child(1) > a")
            ?.slice(0..5)
            ?.map { it.attr("href").replace("/book/", "").replace(".htm", "") }
        val titleList = soup?.select("#content > table.grid > tbody > tr > td > div > div:nth-child(2) > b > a")
            ?.slice(0..5)
            ?.map { it.text().split("(").getOrNull(0) ?: "" } ?: emptyList()
        val authorList = soup?.select("#content > table.grid > tbody > tr > td > div > div:nth-child(2) > p:nth-child(2)")
            ?.slice(0..5)
            ?.map { it.text().split("/").getOrNull(0)?.split(":")?.get(1) ?: ""} ?: emptyList()
        val coverUrlList = soup?.select("#content > table.grid > tbody > tr > td > div > div:nth-child(1) > a > img")
            ?.slice(0..5)
            ?.map { it.attr("src") } ?: emptyList()
        return ExploreBooksRow(
            title = title,
            bookList = idlList?.indices?.map {
                ExploreDisplayBook(
                    id = idlList[it],
                    title = titleList[it],
                    author = authorList[it],
                    coverUri = coverUrlList[it].toUri(),
                )
            } ?: emptyList(),
            expandable = false
        )
    }
}