package indi.dmzz_yyhyy.lightnovelreader.data.json

import com.google.gson.annotations.SerializedName
import io.nightfish.lightnovelreader.api.bookshelf.BookshelfBookMetadata
import java.time.LocalDateTime

data class BookShelfBookMetadataData(
    @SerializedName("id")
    val id: String,
    @SerializedName("last_update")
    val lastUpdate: LocalDateTime,
    @SerializedName("book_shelf_ids")
    val bookShelfIds: List<Int>,
)

fun BookshelfBookMetadata.toJsonData() =
    BookShelfBookMetadataData(
        id = id,
        lastUpdate = lastUpdate,
        bookShelfIds = bookShelfIds,
    )
