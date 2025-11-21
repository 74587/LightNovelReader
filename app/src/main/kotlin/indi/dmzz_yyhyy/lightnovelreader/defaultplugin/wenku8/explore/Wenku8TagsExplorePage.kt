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
import java.net.URLEncoder

object Wenku8TagsExplorePage: ExplorePageDataSource {
    private var lock = false
    private val exploreBooksRows: MutableStateFlow<List<ExploreBooksRow>> = MutableStateFlow(emptyList())

    override val title = "分类"

    override fun getExplorePage(): ExplorePage  {
        if (!lock) {
            lock = true
            CoroutineScope(Dispatchers.IO).launch {
                Jsoup
                    .connect("${host}/modules/article/tags.php")
                    .wenku8Cookie()
                    .autoReconnectionGet()
                    ?.select("a[href~=tags\\.php\\?t=.*]")
                    ?.slice(0..48)
                    ?.map { "${host}/modules/article/" + it.attr("href") }
                    ?.map {url ->
                        val soup = Jsoup
                            .connect(url.split("=")[0] + "=" +
                                    URLEncoder.encode(url.split("=")[1], "gb2312"))
                            .wenku8Cookie()
                            .autoReconnectionGet()
                        exploreBooksRows.update {
                            it + getExploreBookRow(
                                soup = soup,
                                title = url.split("=")[1]
                            )
                        }
                    }
            }
        }

        return ExplorePage("分类", exploreBooksRows)
    }

    private fun getExploreBookRow(title: String, soup: Document?): ExploreBooksRow {
        soup ?: return ExploreBooksRow(
            "",
            emptyList(),
            false,
            ""
        )
        val idlList = soup.select("#content > table > tbody > tr:nth-child(2) > td > div > div:nth-child(1) > a")
            .map { it.attr("href").replace("/book/", "").replace(".htm", "") }
        val titleList = soup.select("#content > table > tbody > tr:nth-child(2) > td > div > div:nth-child(2) > b > a")
            .map { it.text().split("(").getOrNull(0) ?: "" }
        val authorList = soup.select("#content > table > tbody > tr:nth-child(2) > td > div > div:nth-child(2) > p:nth-child(2)")
            .slice(0..5)
            .map { it.text().split("/").getOrNull(0)?.split(":")?.get(1) ?: ""}
        val coverUrlList = soup.select("#content > table > tbody > tr:nth-child(2) > td > div > div:nth-child(1) > a > img")
            .map { it.attr("src") }
        return ExploreBooksRow(
            title = title,
            bookList = (0..5).map {
                ExploreDisplayBook(
                    id = idlList[it],
                    title = titleList[it],
                    author = authorList[it],
                    coverUri = coverUrlList[it].toUri(),
                )
            },
            expandable = true,
            expandedPageDataSourceId = title
        )
    }
}