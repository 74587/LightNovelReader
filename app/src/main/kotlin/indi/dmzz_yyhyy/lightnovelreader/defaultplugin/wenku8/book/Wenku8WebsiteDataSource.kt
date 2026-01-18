package indi.dmzz_yyhyy.lightnovelreader.defaultplugin.wenku8.book

import androidx.core.net.toUri
import indi.dmzz_yyhyy.lightnovelreader.defaultplugin.wenku8.Wenku8Api
import indi.dmzz_yyhyy.lightnovelreader.defaultplugin.wenku8.autoReconnectionGetWithWenku8Cookie
import indi.dmzz_yyhyy.lightnovelreader.utils.CxHttpInit
import indi.dmzz_yyhyy.lightnovelreader.utils.selectFirstXpath
import io.nightfish.lightnovelreader.api.book.BookInformation
import io.nightfish.lightnovelreader.api.book.BookVolumes
import io.nightfish.lightnovelreader.api.book.CanBeEmpty
import io.nightfish.lightnovelreader.api.book.ChapterContent
import io.nightfish.lightnovelreader.api.book.ChapterInformation
import io.nightfish.lightnovelreader.api.book.MutableBookInformation
import io.nightfish.lightnovelreader.api.book.MutableChapterContent
import io.nightfish.lightnovelreader.api.book.Volume
import io.nightfish.lightnovelreader.api.book.WorldCount
import io.nightfish.lightnovelreader.api.content.builder.ContentBuilder
import io.nightfish.lightnovelreader.api.content.builder.image
import io.nightfish.lightnovelreader.api.content.builder.simpleText
import io.nightfish.lightnovelreader.api.util.Cache
import io.nightfish.lightnovelreader.api.web.SearchResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.jsoup.Jsoup
import java.net.URLEncoder
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.time.Duration.Companion.seconds

class Wenku8WebsiteDataSource: Wenku8BookDataSource {
    private val host get() = Wenku8Api.host
    private val titleRegex = Regex("(.*) ?[(（](.*)[)）] ?$")
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    private val cache = Cache(
        timeout = 2 * 60 * 60 * 1000
    )

    private inline fun <reified T: CanBeEmpty> ifCache(id: String, block: () -> T): T {
        val cacheData = cache.getCache<T>(id.hashCode())
        if (cacheData == null) {
            val data = block.invoke()
            if (data.isEmpty()) return data
            cache.cache(id.hashCode(), data)
            return data
        }
        return cacheData
    }

    private fun url(string: String) = "$host/$string"

    override suspend fun getBookInformation(id: String): BookInformation = ifCache(id) {
        val soup = autoReconnectionGetWithWenku8Cookie(url("book/$id.htm")) ?: return@ifCache BookInformation.empty(id)
        if (soup.text().contains("因版权问题")) return@ifCache BookInformation.empty()
        val titleGroup = soup
            .selectFirstXpath("//*[@id=\"content\"]/div[1]/table[1]/tbody/tr[1]/td/table/tbody/tr/td[1]/span/b")
            ?.text()
            ?.let { titleRegex.find(it)?.groups }
        return@ifCache MutableBookInformation(
            id = id,
            title = titleGroup
                ?.get(1)?.value
                ?: soup
                    .selectFirstXpath("//*[@id=\"content\"]/div[1]/table[1]/tbody/tr[1]/td/table/tbody/tr/td[1]/span/b")
                    ?.text()
                ?: return@ifCache BookInformation.empty(id),
            subtitle = titleGroup
                ?.get(2)
                ?.value
                ?: "",
            coverUrl = soup
                .selectFirstXpath("//*[@id=\"content\"]/div[1]/table[2]/tbody/tr/td[1]/img")
                ?.attr("src")
                ?.toUri()
                ?: return@ifCache BookInformation.empty(),
            author = soup
                .selectFirstXpath("//*[@id=\"content\"]/div[1]/table[1]/tbody/tr[2]/td[2]")
                ?.text()
                ?.replace("小说作者：", "")
                ?: return@ifCache BookInformation.empty(),
            description = soup
                .selectFirstXpath("//*[@id=\"content\"]/div[1]/table[2]/tbody/tr/td[2]/span[6]")
                ?.text()
                ?: return@ifCache BookInformation.empty(),
            tags = soup
                .selectFirstXpath("//*[@id=\"content\"]/div[1]/table[2]/tbody/tr/td[2]/span[1]/b")
                ?.text()
                ?.replace("作品Tags：", "")
                ?.split(" ")
                ?: return@ifCache BookInformation.empty(),
            publishingHouse = soup
                .selectFirstXpath("//*[@id=\"content\"]/div[1]/table[1]/tbody/tr[2]/td[1]")
                ?.text()
                ?.replace("文库分类：", "")
                ?: return@ifCache BookInformation.empty(),
            wordCount = soup
                .selectFirstXpath("//*[@id=\"content\"]/div[1]/table[1]/tbody/tr[2]/td[5]")
                ?.text()
                ?.replace("全文长度：", "")
                ?.replace("字", "")
                ?.toIntOrNull()
                ?.let { WorldCount(it) }
                ?: return@ifCache BookInformation.empty(),
            lastUpdated = soup
                .selectFirstXpath("//*[@id=\"content\"]/div[1]/table[1]/tbody/tr[2]/td[4]")
                ?.text()
                ?.replace("最后更新：", "")
                ?.let { LocalDate.parse(it, dateTimeFormatter) }
                ?.atStartOfDay()
                ?: return@ifCache BookInformation.empty(),
            isComplete = soup
                .selectFirstXpath("//*[@id=\"content\"]/div[1]/table[1]/tbody/tr[2]/td[3]")
                ?.text()
                ?.contains("已完结")
                ?: return@ifCache BookInformation.empty()
        )
    }

    override suspend fun getBookVolumes(id: String): BookVolumes = ifCache(id) {
        val soup = autoReconnectionGetWithWenku8Cookie(url("novel/${id.toInt() / 1000}/$id/index.htm")) ?: return@ifCache BookVolumes.empty(id)
        val trs = soup.selectXpath("/html/body/table/tbody/tr")
        val volumes = mutableListOf<Volume>()
        var volume: Volume? = null
        var chapters = mutableListOf<ChapterInformation>()
        trs.forEach { tr ->
            if (tr.selectFirst("td")?.attr("class") == "vcss") {
                volume?.let(volumes::add)
                val td = tr.selectFirst("td")
                val vId = td?.attr("vid") ?: return@ifCache BookVolumes.empty()
                val title = td.text().ifEmpty { return@ifCache BookVolumes.empty() }
                chapters = mutableListOf()
                volume = Volume(vId, title, chapters)
                return@forEach
            }
            for (td in tr.select("td > a")) {
                val id = td
                    .attr("href")
                    .split(".")
                    .firstOrNull()
                    ?: return@ifCache BookVolumes.empty()
                val title = td.text().ifEmpty { return@ifCache BookVolumes.empty() }
                chapters.add(ChapterInformation(id, title))
            }
        }
        volume?.let(volumes::add)
        return@ifCache BookVolumes(id, volumes)
    }

    override suspend fun getChapterContent(
        chapterId: String,
        bookId: String
    ): ChapterContent = ifCache(chapterId + bookId) {
        val soup = autoReconnectionGetWithWenku8Cookie(url("novel/${bookId.toInt() / 1000}/$bookId/$chapterId.htm")) ?: return@ifCache ChapterContent.empty(chapterId)
        if (soup.text().contains("因版权问题")) return@ifCache ChapterContent.empty(chapterId)
        val content = soup.selectFirstXpath("//*[@id=\"content\"]") ?: return@ifCache ChapterContent.empty(chapterId)
        val jsonObject = ContentBuilder().apply {
            var text = ""
            content.toString().split("\n").forEach {
                val doc = Jsoup.parse(it)
                if (doc.body().children().isEmpty()) return@forEach
                val element = doc.body().child(0)
                if (element.id() == "content" || element.id() == "contentdp") return@forEach
                val line = doc.body().text()
                if (line.isNotEmpty()) {
                    text = "$text    $line"
                }
                when {
                    element.`is`("br") -> text += "\n"
                    element.`is`("div.divimage") -> {
                        simpleText(text)
                        text = ""
                        element
                            .selectFirst("img")
                            ?.attr("src")
                            ?.toUri()
                            ?.let(::image)
                    }
                }
            }
            if (text.isNotEmpty()) {
                simpleText(text)
            }
        }.build()
        return@ifCache MutableChapterContent(
            id = chapterId,
            title = soup.selectFirstXpath("//*[@id=\"title\"]")?.text() ?: return@ifCache ChapterContent.empty(chapterId),
            content = jsonObject,
            lastChapter = soup.selectFirstXpath("//*[@id=\"foottext\"]/a[3]").let {
                it ?: return@let ""
                if (it.attr("href") == "index.htm") ""
                else it.attr("href").split(".").firstOrNull() ?: ""
            },
            nextChapter = soup.selectFirstXpath("//*[@id=\"foottext\"]/a[4]").let {
                it ?: return@let ""
                if (it.attr("href") == "index.htm") ""
                else it.attr("href").split(".").firstOrNull() ?: ""
            }
        )
    }

    override fun search(searchType: String, keyword: String): Flow<SearchResult> = flow {
        val encodedKeyword = URLEncoder.encode(keyword, "gb2312")

        var targetPage = 1
        var presentPage = 1
        while(presentPage <= targetPage) {
            val soup = autoReconnectionGetWithWenku8Cookie(url("modules/article/search.php?searchtype=$searchType&searchkey=$encodedKeyword&page=$presentPage"))
            if (soup == null) {
                emit(SearchResult.Error("Failed to request the web page"))
                return@flow
            }
            if (soup.text().contains("错误原因：对不起，两次搜索的间隔时间不得少于 5 秒")) {
                delay(5.seconds)
                continue
            }
            val menu = soup.selectFirstXpath("//*[@id=\"content\"]/div[1]/div[4]/div/span[1]/fieldset/div/a")
            if (menu != null && menu.text().contains("小说目录")) {
                val id = menu.attr("href").split("/").getOrNull(3)
                if (id == null) {
                    emit(SearchResult.Error("Failed to prase single book id"))
                    return@flow
                }
                emit(SearchResult.SingleBook(id))
                return@flow
            }
            soup.baseUri()
            if (targetPage == 1) {
                val page = soup.selectFirstXpath("//*[@id=\"pagelink\"]/em")?.text()?.split("/")?.getOrNull(1)?.toIntOrNull()
                if (page == null) {
                    emit(SearchResult.Error("Failed to request the web page"))
                    return@flow
                }
                targetPage = page
            }

            val books = Wenku8Api.getBookInformationListFromBookCards(soup.selectXpath("//*[@id=\"content\"]/table/tbody/tr/td/div"))
            for (information in books) {
                emit(SearchResult.MultipleBook(information))
            }

            presentPage++
            delay(5.seconds)
        }
    }

    init {
        CxHttpInit.init()
    }
}