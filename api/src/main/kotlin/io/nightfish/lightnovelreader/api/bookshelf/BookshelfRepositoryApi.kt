package io.nightfish.lightnovelreader.api.bookshelf

import io.nightfish.lightnovelreader.api.book.BookInformation
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.time.LocalDateTime

interface BookshelfRepositoryApi {
    fun getAllBookshelfIds(): List<Int>
    fun deleteBookshelf(bookshelfId: Int)
    fun addBookIntoBookShelf(bookshelfId: Int, bookInformation: BookInformation)
    fun addUpdatedBooksIntoBookShelf(bookShelfId: Int, bookId: String)
    fun getAllBookshelfBookIdsFlow(): Flow<List<String>>
    fun deleteBookFromBookshelf(bookshelfId: Int, bookId: String)
    fun deleteBookFromBookshelfUpdatedBookIds(bookshelfId: Int, bookId: String)
    fun updateBookshelfBookMetadataLastUpdateTime(bookId: String, time: LocalDateTime)
    fun clear()
    fun getAllBookshelvesFlow(): Flow<List<MutableBookshelf>>
    fun getAllBookshelves(): List<MutableBookshelf>
    fun getBookshelf(id: Int): MutableBookshelf?
    fun getBookshelfFlow(id: Int): Flow<MutableBookshelf?>
    fun createBookShelf(
        id: Int = Instant.now().epochSecond.hashCode(),
        name: String,
        sortType: BookshelfSortType,
        autoCache: Boolean,
        systemUpdateReminder: Boolean
    ): Int

    fun updateBookshelf(bookshelfId: Int, updater: (MutableBookshelf) -> Bookshelf)
    fun getAllBookshelfBooksMetadata(): List<BookshelfBookMetadata>
    fun getBookshelfBookMetadata(id: String): BookshelfBookMetadata?
    fun getBookshelfBookMetadataFlow(id: String): Flow<BookshelfBookMetadata?>
}