package io.nightfish.lightnovelreader.api.book

import android.net.Uri
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.time.LocalDateTime

/**
 * 书本详情接口
 *
 * @param id 书本id
 * @param title 书本标题
 * @param subtitle 书本副标题(如果没有可以为空)
 * @param coverUri 书本封面的[Uri][Uri]
 * @param author 书本作者
 * @param description 书本简介
 * @param tags 书本的tag, 传递stringList
 * @param publishingHouse 书本出版社
 * @param wordCount 书本总字数(不一定是字数, 也可以是书本)
 * @param lastUpdated 书本id
 * @param isComplete 书本id
 *
 * @since Api 2
 */
@Stable
/**
 * BookInformation 类型。
 */
interface BookInformation: CanBeEmpty, Copyable<BookInformation> {
    val id: String
    val title: String
    val subtitle: String
    val coverUri: Uri
    val author: String
    val description: String
    val tags: List<String>
    val publishingHouse: String
    val wordCount: WordCount
    val lastUpdated: LocalDateTime
    val isComplete: Boolean

    companion object {
        /**
         * 返回一个空的书本详情, 并且将id设空
         */
        fun empty(): BookInformation = empty("")

        /**
         * 返回一个空的书本详情, 并保有书本id
         *
         * @param id 书本id
         *
         * @return 空的书本详情
         *
         * @since Api 2
         */
        fun empty(id: String): BookInformation = MutableBookInformation(
            id,
            "",
            "",
            Uri.EMPTY,
            "",
            "",
            emptyList(),
            "",
            WordCount(0),
            LocalDateTime.MIN,
            false
        )
    }

    /**
     * 判断书本详情是否为空
     * 如果id或者title任意一个为空时判断为空
     *
     * @return 书本详情是否为空
     *
     * @since Api 2
     */
    override fun isEmpty() = id.isEmpty() || title == ""

    /**
     * 深度复制一个书本详情数据对象
     *
     * @return 复制的书本详情对象
     *
     * @since Api 2
     */
    override fun copy(): BookInformation =
        MutableBookInformation(id, title, subtitle, coverUri, author, description, tags, publishingHouse, wordCount, lastUpdated, isComplete)

    /**
     * 转化为可变对象
     * 如果对象本身就是可变对象, 则直接返回自身
     * 如果对象并非可变对象, 则复制一份出一份可变对象并返回
     *
     * @return 可变对象
     *
     * @since Api 2
     */
    fun toMutable(): MutableBookInformation {
        if (this is MutableBookInformation)
            return this
        return MutableBookInformation(id, title, subtitle, coverUri, author, description, tags, publishingHouse, wordCount, lastUpdated, isComplete)
    }
}

/**
 * 可变的书本详情对象
 * 其中每一个成员都是可被UI观测的
 *
 * @param id 书本id
 * @param title 书本标题
 * @param subtitle 书本副标题(如果没有可以为空)
 * @param coverUri 书本封面的[Uri][Uri]
 * @param author 书本作者
 * @param description 书本简介
 * @param tags 书本的tag, 传递stringList
 * @param publishingHouse 书本出版社
 * @param wordCount 书本总字数(不一定是字数, 也可以是书本)
 * @param lastUpdated 书本id
 * @param isComplete 书本id
 *
 * @constructor 返回可变书本详情对象
 *
 * @since Api 2
 */
class MutableBookInformation(
    id: String,
    title: String,
    subtitle: String,
    coverUrl: Uri,
    author: String,
    description: String,
    tags: List<String>,
    publishingHouse: String,
    wordCount: WordCount,
    lastUpdated: LocalDateTime,
    isComplete: Boolean
): BookInformation {
    override var id by mutableStateOf(id)
    override var title by mutableStateOf(title)
    override var subtitle  by mutableStateOf(subtitle)
    override var coverUri by mutableStateOf(coverUrl)
    override var author by mutableStateOf(author)
    override var description by mutableStateOf(description)
    override val tags = mutableStateListOf<String>().apply { addAll(tags) }
    override var publishingHouse by mutableStateOf(publishingHouse)
    override var wordCount by mutableStateOf(wordCount)
    override var lastUpdated by mutableStateOf(lastUpdated)
    override var isComplete by mutableStateOf(isComplete)

    companion object {


        /**
         * 返回一个空的书本详情, 并且将id设空
         *
         * @since Api 2
         */
        fun empty(): MutableBookInformation = MutableBookInformation(
            "",
            "",
            "",
            Uri.EMPTY,
            "",
            "",
            emptyList(),
            "",
            WordCount(0),
            LocalDateTime.MIN,
            false
        )

        /**
         * 返回一个空的书本详情, 并保有书本id
         *
         * @param id 书本id
         *
         * @return 空的书本详情
         *
         * @since Api 2
         */
        fun empty(id: String): BookInformation = MutableBookInformation(
            id,
            "",
            "",
            Uri.EMPTY,
            "",
            "",
            emptyList(),
            "",
            WordCount(0),
            LocalDateTime.MIN,
            false
        )
    }

    /**
     * 将更新自身数据为传入的对象的数据
     *
     * @param bookInformation 传入的书本详情对象
     *
     * @since Api 2
     */
    fun update(bookInformation: BookInformation) {
        this.id = bookInformation.id
        this.title = bookInformation.title
        this.subtitle = bookInformation.subtitle
        this.coverUri = bookInformation.coverUri
        this.author = bookInformation.author
        this.description = bookInformation.description
        this.tags.clear()
        this.tags.addAll(bookInformation.tags)
        this.publishingHouse = bookInformation.publishingHouse
        this.wordCount = bookInformation.wordCount
        this.lastUpdated = bookInformation.lastUpdated
        this.isComplete = bookInformation.isComplete
    }
}
