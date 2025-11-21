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

object Wenku8HomeExplorePage: ExplorePageDataSource {
    private var lock = false
    private val exploreBooksRows: MutableStateFlow<List<ExploreBooksRow>> = MutableStateFlow(emptyList())

    override val title = "首页"

    override fun getExplorePage(): ExplorePage  {
        if (!lock) {
            lock = true
            CoroutineScope(Dispatchers.IO).launch {
                val soup = Jsoup
                    .connect(host)
                    .wenku8Cookie()
                    .autoReconnectionGet()
                (0..2).map { index->
                    exploreBooksRows.update {
                        it + getBooksRow(index, soup)
                    }
                }
            }
        }
        return ExplorePage("首页", exploreBooksRows)
    }

    private fun getBooksRow(index: Int, soup: Document?): ExploreBooksRow {
        val title = soup?.selectFirst("#centers > div:nth-child(${index+2}) > div.blocktitle")?.text()
            ?.split("(")?.getOrNull(0) ?: ""
        val idlList = soup?.select("#centers > div:nth-child(${index+2}) > div.blockcontent > div > div > a:nth-child(1)")
            ?.map { it.attr("href").replace("/book/", "").replace(".htm", "") }
        val titleList = soup?.select("#centers > div:nth-child(${index+2}) > div.blockcontent > div > div > a:nth-child(3)")
            ?.map { it.text().split("(").getOrNull(0) ?: "" } ?: emptyList()
        val coverUrlList = soup?.select("#centers > div:nth-child(${index+2}) > div.blockcontent > div > div > a:nth-child(1) > img")
            ?.map { it.attr("src") } ?: emptyList()
        return ExploreBooksRow(
            title = title,
            bookList = idlList?.indices?.map {
                ExploreDisplayBook(
                    id = idlList[it],
                    title = titleList[it],
                    author = "",
                    coverUri = coverUrlList[it].toUri(),
                )
            } ?: emptyList(),
            expandable = false
        )
    }
}