package indi.dmzz_yyhyy.lightnovelreader.defaultplugin.wenku8.book

import io.nightfish.lightnovelreader.api.book.BookInformation
import io.nightfish.lightnovelreader.api.book.BookVolumes
import io.nightfish.lightnovelreader.api.book.ChapterContent
import kotlinx.coroutines.flow.Flow

interface Wenku8BookDataSource {
    suspend fun getBookInformation(id: String): BookInformation
    suspend fun getBookVolumes(id: String): BookVolumes
    suspend fun getChapterContent(chapterId: String, bookId: String): ChapterContent
    fun search(searchType: String, keyword: String): Flow<BookInformation>
}