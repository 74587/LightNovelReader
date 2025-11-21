package indi.dmzz_yyhyy.lightnovelreader.data.local.room.dao

import android.net.Uri
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.TypeConverters
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.converter.ListConverter.stringListToString
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.converter.LocalDateTimeConverter
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.converter.UriConverter
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.converter.WorldCountConverter
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.BookInformationEntity
import io.nightfish.lightnovelreader.api.book.BookInformation
import io.nightfish.lightnovelreader.api.book.MutableBookInformation
import io.nightfish.lightnovelreader.api.book.WorldCount
import java.time.LocalDateTime

@Dao
interface BookInformationDao {
    @TypeConverters(LocalDateTimeConverter::class, UriConverter::class, WorldCountConverter::class)
    @Query("replace into book_information (id, title, subtitle, cover_uri, author, description, tags, publishing_house, word_count, last_update, is_complete) " +
            "values (:id, :title, :subtitle, :coverUri, :author, :description, :tags, :publishingHouse, :wordCount, :lastUpdated, :isComplete) "
    )
    fun update(id: String,
               title: String,
               subtitle: String,
               coverUri: Uri,
               author: String,
               description: String,
               tags: String,
               publishingHouse: String,
               wordCount: WorldCount,
               lastUpdated: LocalDateTime,
               isComplete: Boolean)

    @Transaction
    fun update(information: BookInformation) {
        return update(
            information.id,
            information.title,
            information.subtitle,
            information.coverUri,
            information.author,
            information.description,
            stringListToString(information.tags),
            information.publishingHouse,
            information.wordCount,
            information.lastUpdated,
            information.isComplete,
        )
    }

    @Query("select * from book_information where id=:id")
    suspend fun getEntity(id: String): BookInformationEntity?

    @Transaction
    suspend fun get(id: String): BookInformation? {
        val entity = getEntity(id) ?: return null
        return MutableBookInformation(
            entity.id,
            entity.title,
            entity.subtitle,
            entity.coverUri,
            entity.author,
            entity.description,
            entity.tags,
            entity.publishingHouse,
            entity.wordCount,
            entity.lastUpdated,
            entity.isComplete,
        )
    }

    @Transaction
    suspend fun has(id: String): Boolean {
        return get(id) != null
    }

    @Query("delete from book_information")
    fun clear()
}