package indi.dmzz_yyhyy.lightnovelreader.defaultplugin.wenku8

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import androidx.navigation.NavController
import indi.dmzz_yyhyy.lightnovelreader.defaultplugin.wenku8.explore.Wenku8AllExplorePage
import indi.dmzz_yyhyy.lightnovelreader.defaultplugin.wenku8.explore.Wenku8HomeExplorePage
import indi.dmzz_yyhyy.lightnovelreader.defaultplugin.wenku8.explore.Wenku8TagsExplorePage
import indi.dmzz_yyhyy.lightnovelreader.defaultplugin.wenku8.explore.expanedpage.HomeBookExpandPageDataSource
import indi.dmzz_yyhyy.lightnovelreader.defaultplugin.wenku8.explore.expanedpage.filter.FirstLetterSingleChoiceFilter
import indi.dmzz_yyhyy.lightnovelreader.defaultplugin.wenku8.explore.expanedpage.filter.PublishingHouseSingleChoiceFilter
import indi.dmzz_yyhyy.lightnovelreader.ui.home.explore.expanded.navigateToExploreExpandDestination
import indi.dmzz_yyhyy.lightnovelreader.utils.update
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
import io.nightfish.lightnovelreader.api.content.component.ImageComponentData
import io.nightfish.lightnovelreader.api.util.Cache
import io.nightfish.lightnovelreader.api.web.WebBookDataSource
import io.nightfish.lightnovelreader.api.web.WebDataSource
import io.nightfish.lightnovelreader.api.web.explore.ExploreExpandedPageDataSource
import io.nightfish.lightnovelreader.api.web.explore.ExplorePageDataSource
import io.nightfish.lightnovelreader.api.web.explore.filter.IsCompletedSwitchFilter
import io.nightfish.lightnovelreader.api.web.explore.filter.SingleChoiceFilter
import io.nightfish.lightnovelreader.api.web.explore.filter.WordCountFilter
import io.nightfish.potatoautoproxy.ProxyPool
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.jsoup.Jsoup
import org.jsoup.select.Elements
import java.net.URLEncoder
import java.net.UnknownHostException
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


@WebDataSource(
    "Wenku8",
    "LightNovelReader from wenku8.net"
)
object Wenku8Api: WebBookDataSource {
    private val tagList = listOf(
        "校园", "青春", "恋爱", "治愈", "群像",
        "竞技", "音乐", "美食", "旅行", "欢乐向",
        "经营", "职场", "斗智", "脑洞", "宅文化",
        "穿越", "奇幻", "魔法", "异能", "战斗",
        "科幻", "机战", "战争", "冒险", "龙傲天",
        "悬疑", "犯罪", "复仇", "黑暗", "猎奇",
        "惊悚", "间谍", "末日", "游戏", "大逃杀",
        "青梅竹马", "妹妹", "女儿", "JK", "JC",
        "大小姐", "性转", "伪娘", "人外",
        "后宫", "百合", "耽美", "NTR", "女性视角"
    )
    private var allBookChapterListCacheId: String = ""
    private var allBookChapterListCache: List<ChapterInformation> = emptyList()
    private val DATA_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    override val exploreExpandedPageDataSourceMap = mutableMapOf<String, ExploreExpandedPageDataSource>()
    private var coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
    private val titleRegex = Regex("(.*) ?[(（](.*)[)）] ?$")
    private val hosts = listOf("https://www.wenku8.cc", "https://www.wenku8.net", "https://www.wenku8.com")
    private var isLocalIpUnableUse = true
    override val cache = Cache(
        timeout = 5 * 60 * 1000
    )
    private val _cache = Cache(
        timeout = 5 * 60 * 1000
    )
    var host  =  hosts[0]

    init {
        coroutineScope.launch {
            offLine = isOffLine()
        }
    }

    private inline fun <reified T: CanBeEmpty> ifCache(id: String, block: () -> T): T {
        val cacheData = _cache.getCache<T>(id.hashCode())
        if (cacheData == null) {
            val data = block.invoke()
            if (data.isEmpty()) return data
            _cache.cache(id.hashCode(), data)
            return data
        }
        return cacheData
    }

    override var offLine: Boolean = true

    override val isOffLineFlow = flow {
        emit(offLine)
        while (currentCoroutineContext().isActive) {
            offLine = isOffLine()
            if (offLine)
                offLine = isOffLine()
            emit(offLine)
            delay(if (offLine) 3000 else 100000)
        }
    }

    private suspend fun tryOrFalse(block: () -> Unit): Boolean = withContext(Dispatchers.IO) {
        try {
            block.invoke()
            return@withContext false
        } catch (e: UnknownHostException) {
            Log.e("Wenku8Api", "DNS probe failed. ${e.message}")
            true
        } catch (e: Exception) {
            Log.e("Wenku8Api", "${e.message}")
            Log.d("Wenku8Api", "An error occurred", e)
            return@withContext true
        }
    }

    override suspend fun isOffLine(): Boolean = withContext(Dispatchers.IO) {
        val isApiOffLine =
            async {
                tryOrFalse {
                    Jsoup
                        .connect(update("eNpb85aBtYRBMaOkpMBKXz-xoECvPDUvu9RCLzk_Vz8xL6UoPzNFryCjAAAfiA5Q").toString())
                        .userAgent("wenku8")
                        .let {
                            if (ProxyPool.enable && !isLocalIpUnableUse)
                                ProxyPool.apply {
                                    it.proxyGet()
                                }
                            else it.get()
                        }
                }
            }
        val isWebOffline1 =
            async {
                tryOrFalse {
                    Jsoup
                        .connect(hosts[0])
                        .wenku8Cookie()
                        .let {
                            if (ProxyPool.enable && !isLocalIpUnableUse)
                                ProxyPool.apply {
                                    it.proxyGet()
                                }
                            else it.get()
                        }
                }.also {
                    if (!it)
                        host = hosts[0]
                }
            }
        val isWebOffline2 =
            async {
                tryOrFalse {
                    Jsoup
                        .connect(hosts[1])
                        .wenku8Cookie()
                        .let {
                            if (ProxyPool.enable && !isLocalIpUnableUse)
                                ProxyPool.apply {
                                    it.proxyGet()
                                }
                            else it.get()
                        }
                }.also {
                    if (!it)
                        host = hosts[1]
                }
            }

        val isWebOffline3 =
            async {
                tryOrFalse {
                    Jsoup
                        .connect(hosts[2])
                        .wenku8Cookie()
                        .let {
                            if (ProxyPool.enable && !isLocalIpUnableUse)
                                ProxyPool.apply {
                                    it.proxyGet()
                                }
                            else it.get()
                        }
                }.also {
                    if (!it)
                        host = hosts[2]
                }
            }

        return@withContext isApiOffLine.await() || (isWebOffline1.await() && isWebOffline2.await() && isWebOffline3.await())
    }

    override val id: Int = "wenku8".hashCode()

    override suspend fun getBookInformation(id: String): BookInformation = ifCache(id) {
        return@ifCache wenku8Api("action=book&do=meta&aid=$id&t=0")?.let {
            if (id.toIntOrNull() == null) return@let BookInformation.empty(id)
            val titleGroup = it
                .selectFirst("[name=Title]")?.text()
                ?.let { it1 -> titleRegex.find(it1)?.groups }
            try {
                MutableBookInformation(
                    id = id,
                    title = titleGroup?.get(1)?.value ?: it.selectFirst("[name=Title]")?.text()
                    ?: "",
                    subtitle = titleGroup?.get(2)?.value ?: "",
                    coverUrl = "https://img.wenku8.com/image/${id.toInt() / 1000}/$id/${id}s.jpg".toUri(),
                    author = it.selectFirst("[name=Author]")?.attr("value") ?: "",
                    description = wenku8Api("action=book&do=intro&aid=$id&t=0")?.text() ?: "",
                    tags = it.selectFirst("[name=Tags]")?.attr("value")?.split(" ") ?: emptyList(),
                    publishingHouse = it.selectFirst("[name=PressId]")?.attr("value") ?: "",
                    wordCount = WorldCount(it.selectFirst("[name=BookLength]")?.attr("value")?.toInt() ?: -1),
                    lastUpdated = LocalDate.parse(
                        it.selectFirst("[name=LastUpdate]")?.attr("value"), DATA_TIME_FORMATTER
                    ).atStartOfDay(),
                    isComplete = it.selectFirst("[name=BookStatus]")?.attr("value") == "已完成"
                )
            } catch (e: NullPointerException) {
                e.printStackTrace()
                BookInformation.empty()
            }
        } ?: BookInformation.empty()
    }

    override suspend fun getBookVolumes(id: String): BookVolumes = ifCache(id) {
        if (id.toIntOrNull() == null) return@ifCache BookVolumes.empty(id)
        return@ifCache BookVolumes(
            id,
            wenku8Api("action=book&do=list&aid=$id&t=0")
            ?.select("volume")
            ?.map { element ->
                Volume(
                    volumeId = element.attr("vid"),
                    volumeTitle = element.ownText(),
                    chapters = element.select("volume > chapter")
                        .map {
                            ChapterInformation(
                                id = it.attr("cid"),
                                title = it.text(),
                            )
                        }
                )
            } ?: emptyList()
        )
    }

    override suspend fun getChapterContent(chapterId: String, bookId: String): ChapterContent = ifCache(chapterId + bookId)  {
        if (allBookChapterListCacheId != bookId) {
            allBookChapterListCacheId = bookId
            allBookChapterListCache = getBookVolumes(bookId).let { bookVolumes ->
                var list = emptyList<ChapterInformation>()
                bookVolumes.volumes.forEach {
                    list = list + it.chapters
                }
                return@let list
            }
        }
        return@ifCache wenku8Api("action=book&do=text&aid=$bookId&cid=$chapterId&t=0")
            .let { document ->
                document
                    ?.body()
                    .toString()
                    .replaceFirst("<body>", "")
                    .replaceFirst("</body>", "")
                    .let { s ->
                        var title = ""
                        var content = ""
                        s.split("\n").forEachIndexed { index, line ->
                            if (content != "") return@forEachIndexed
                            if (title == "" && line.any { !it.isWhitespace() }) {
                                title = line.trim()
                                return@forEachIndexed
                            }
                            if (title != "" && line.any { !it.isWhitespace() }) {
                                content = s.split("\n").drop(index).joinToString("\n")
                                return@forEachIndexed
                            }
                        }
                        val jsonObject = ContentBuilder().apply {
                            content.split("<!--image-->").forEach {
                                if (it.trim().startsWith("http")) {
                                    image(it.trim().toUri())
                                } else if(it.isNotBlank()) {
                                    simpleText(it)
                                }
                            }
                        }.build()
                        MutableChapterContent(
                            id = chapterId,
                            title = title,
                            content = jsonObject,
                            lastChapter = allBookChapterListCache
                                .indexOfFirst { it.id == chapterId }
                                .let {
                                    if (it == -1) "" else allBookChapterListCache.getOrNull(it - 1)?.id
                                        ?: ""
                                },
                            nextChapter = allBookChapterListCache
                                .indexOfFirst { it.id == chapterId }
                                .let {
                                    if (it == -1) "" else allBookChapterListCache.getOrNull(it + 1)?.id
                                        ?: ""
                                }
                        )
                    }
            }
    }

    override val explorePageDataSourceMap: Map<String, ExplorePageDataSource> =
        mapOf(
            Pair("首页", Wenku8HomeExplorePage),
            Pair("全部", Wenku8AllExplorePage),
            Pair("分类", Wenku8TagsExplorePage)
        )

    override val explorePageIdList: List<String> = listOf("首页", "全部", "分类")

    override fun search(searchType: String, keyword: String): Flow<List<BookInformation>> {
        val searchResult = MutableStateFlow(emptyList<BookInformation>())
        val encodedKeyword = URLEncoder.encode(keyword, "gb2312")
        coroutineScope.launch {
            delay(1)
            wenku8Api(
                "action=search&searchtype=$searchType&searchkey=${
                    URLEncoder.encode(
                        encodedKeyword,
                        "utf-8" 
                    )
                }"
            )
                ?.select("item")
                ?.forEach { element ->
                    searchResult.update {
                        it + listOf(getBookInformation(element.attr("aid")))
                    }
                }
                ?.let {
                    searchResult.update {
                        it + listOf(BookInformation.empty())
                    }
                }
        }
        return searchResult
    }

    override fun stopAllSearch() {
        coroutineScope.cancel()
        coroutineScope = CoroutineScope(Dispatchers.IO)
    }

    override val searchTypeIdList =
        listOf("articlename", "author")

    override val searchTypeMap: Map<String, String> =
        mapOf(
            Pair("articlename", "按书名搜索"),
            Pair("author", "按作者名搜索"),
        )

    override val searchTipMap: Map<String, String> =
        mapOf(
            Pair("articlename", "请输入书本名称"),
            Pair("author", "请输入作者名称"),
        )

    suspend fun getBookInformationListFromBookCards(elements: Elements): List<BookInformation> =
        elements
            .map { element ->
                if (element.text().contains("因版权问题"))
                    getBookInformation(element
                        .selectFirst("div > div:nth-child(1) > a")
                        ?.attr("href")
                        ?.replace("/book/", "")
                        ?.replace(".htm", "") ?: ""
                    )
                else {
                    val titleGroup = element.selectFirst("div > div:nth-child(1) > a")
                        ?.attr("title")
                        ?.let { it1 -> titleRegex.find(it1)?.groups }
                    MutableBookInformation(
                        id = element.selectFirst("div > div:nth-child(1) > a")
                            ?.attr("href")
                            ?.replace("/book/", "")
                            ?.replace(".htm", "") ?: "",
                        title = titleGroup?.get(1)?.value ?: element.selectFirst("div > div:nth-child(1) > a")
                            ?.attr("title") ?: "",
                        subtitle = titleGroup?.get(2)?.value ?: "",
                        coverUrl = element.selectFirst("div > div:nth-child(1) > a > img")
                            ?.attr("src")?.toUri() ?: Uri.EMPTY,
                        author = element.selectFirst("div > div:nth-child(2) > p:nth-child(2)")
                            ?.text()?.split("/")?.getOrNull(0)
                            ?.split(":")?.getOrNull(1) ?: "",
                        description = element.selectFirst("div > div:nth-child(2) > p:nth-child(5)")
                            ?.text()?.replace("简介:", "") ?: "",
                        tags = element.selectFirst("div > div:nth-child(2) > p:nth-child(4) > span")
                            ?.text()?.split(" ") ?: emptyList(),
                        publishingHouse = element.selectFirst("div > div:nth-child(2) > p:nth-child(2)")
                            ?.text()?.split("/")?.getOrNull(1)
                            ?.split(":")?.getOrNull(1) ?: "",
                        wordCount = WorldCount(element.selectFirst("div > div:nth-child(2) > p:nth-child(3)")
                            ?.text()?.split("/")?.getOrNull(1)
                            ?.split(":")?.getOrNull(1)
                            ?.replace("K", "")?.toInt()?.times(1000) ?: -1),
                        lastUpdated = element.selectFirst("div > div:nth-child(2) > p:nth-child(3)")
                            ?.text()?.split("/")?.getOrNull(0)
                            ?.split(":")?.getOrNull(1)
                            ?.let {
                                LocalDate.parse(it, DATA_TIME_FORMATTER)
                            }
                            ?.atStartOfDay() ?: LocalDateTime.MIN,
                        isComplete = element.selectFirst("div > div:nth-child(2) > p:nth-child(3)")
                            ?.text()?.split("/")?.getOrNull(2) == "已完结"
                    )
                }
            }

    private fun registerExploreExpandedPageDataSource(id: String, expandedPageDataSource: ExploreExpandedPageDataSource) =
        exploreExpandedPageDataSourceMap.put(id, expandedPageDataSource)

    init {
        registerExploreExpandedPageDataSource(
            id = "allBook",
            expandedPageDataSource = HomeBookExpandPageDataSource(
                title = "轻小说列表",
                filtersBuilder = {
                    listOf(
                        IsCompletedSwitchFilter { this.refresh() },
                        FirstLetterSingleChoiceFilter { choice ->
                            val arg = when (choice) {
                                "任意" -> ""
                                "0~9" -> "&initial=1"
                                else -> "&initial=${choice}"
                            }
                            this.arg = arg
                            this.refresh()
                        },
                        PublishingHouseSingleChoiceFilter { this.refresh() },
                        WordCountFilter { this.refresh() }
                    )
                }
            )
        )
        registerExploreExpandedPageDataSource(
            id = "allCompletedBook",
            expandedPageDataSource = HomeBookExpandPageDataSource(
                title = "完结全本",
                filtersBuilder = {
                    listOf(
                        IsCompletedSwitchFilter { this.refresh() },
                        FirstLetterSingleChoiceFilter { choice ->
                            val arg = when (choice) {
                                "任意" -> ""
                                "0~9" -> "&initial=1"
                                else -> "&initial=${choice}"
                            }
                            this.arg = arg
                            this.refresh()
                        },
                        PublishingHouseSingleChoiceFilter { this.refresh() },
                        WordCountFilter { this.refresh() }
                    )
                },
                extendedParameters = "&fullflag=1"
            )
        )
        listOf("allvisit", "anime", "lastupdate", "postdate").forEach { id ->
            val nameMap = mapOf(
                Pair("allvisit", "热门轻小说"),
                Pair("anime", "动画化作品"),
                Pair("lastupdate", "今日更新"),
                Pair("postdate", "新书一览"),
            )
            registerExploreExpandedPageDataSource(
                id = "${id}Book",
                expandedPageDataSource = HomeBookExpandPageDataSource(
                    baseUrl = "$host/modules/article/toplist.php",
                    title = nameMap[id] ?: "",
                    filtersBuilder = {
                        listOf(
                            IsCompletedSwitchFilter { this.refresh() },
                            FirstLetterSingleChoiceFilter { choice ->
                                val arg = when (choice) {
                                    "任意" -> ""
                                    "0~9" -> "&initial=1"
                                    else -> "&initial=${choice}"
                                }
                                this.arg = arg
                                this.refresh()
                            },
                            PublishingHouseSingleChoiceFilter { this.refresh() },
                            WordCountFilter { this.refresh() }
                        )
                    },
                    extendedParameters = "&sort=$id",
                    contentSelector = "#content > table > tbody > tr > td > div"
                )
            )
        }
        tagList.forEach { tag ->
            registerExploreExpandedPageDataSource(
                id = tag,
                expandedPageDataSource = HomeBookExpandPageDataSource(
                    baseUrl = "$host/modules/article/tags.php",
                    title = tag,
                    filtersBuilder = {
                        val choicesMap = mapOf(
                            Pair("默认", ""),
                            Pair("按更新时间排序", ""),
                            Pair("按热度排序", "&v=1"),
                            Pair("仅动画化", "&v=3")
                        )
                        listOf(
                            IsCompletedSwitchFilter { this.refresh() },
                            SingleChoiceFilter(
                                title = "排序",
                                dialogTitle = "文库筛选",
                                description = "根据小说的文库筛选",
                                choices = listOf(
                                    "默认",
                                    "按更新时间排序",
                                    "按热度排序",
                                    "仅动画化"
                                ),
                                defaultChoice = "默认"
                            ) {
                                this.arg = choicesMap[it.trim()] ?: ""
                                this.refresh()
                            },
                            PublishingHouseSingleChoiceFilter { this.refresh() },
                            WordCountFilter { this.refresh() }
                        )
                    },
                    extendedParameters = "&t=${URLEncoder.encode(tag, "gb2312")}",
                    contentSelector = "#content > table > tbody > tr:nth-child(2) > td > div"
                )
            )
        }
    }

    override fun progressBookTagClick(tag: String, navController: NavController) {
        if (tagList.contains(tag))
            navController.navigateToExploreExpandDestination(tag)
    }

    override fun getCoverUriInVolume(bookId: String, volume: Volume, volumeChapterContentMap: MutableMap<String, ChapterContent>, context: Context): Uri? {
        return volume.chapters
            .find { it.title.endsWith("插图") }
            ?.let { chapterInformation ->
                val chapterContent = volumeChapterContentMap[chapterInformation.id] ?: return null
                if (chapterContent.isEmpty()) return null
                chapterContent.content["components"]?.jsonArray
                    ?.mapNotNull { it.jsonObject }
                    ?.filter {
                        it["id"]?.jsonPrimitive?.content == ImageComponentData.ID
                    }
                    ?.forEach {
                        val uri = it["data"]?.jsonObject["uri"]?.jsonPrimitive?.content?.toUri() ?: return null
                        val options = BitmapFactory.Options()
                        options.inJustDecodeBounds = true
                        val inputStream = context.contentResolver.openInputStream(uri)
                        BitmapFactory.decodeStream(inputStream, null, options)
                        if (options.outHeight > options.outWidth) return uri
                    }
                return null
            }
    }
}