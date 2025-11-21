package indi.dmzz_yyhyy.lightnovelreader.data.json

import com.google.gson.annotations.SerializedName
import io.nightfish.lightnovelreader.api.bookshelf.Bookshelf
import io.nightfish.lightnovelreader.api.bookshelf.BookshelfSortType

data class BookshelfData(
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("sort_type")
    val sortType: BookshelfSortType,
    @SerializedName("auto_cache")
    val autoCache: Boolean,
    @SerializedName("system_update_reminder")
    val systemUpdateReminder: Boolean,
    @SerializedName("all_book_ids")
    val allBookIds: List<String>,
    @SerializedName("pinned_book_ids")
    val pinnedBookIds: List<String>,
    @SerializedName("updatedBookIds")
    val updatedBookIds: List<String>,
)

fun Bookshelf.toJsonData(): BookshelfData =
    BookshelfData(
        id = id,
        name = name,
        sortType = sortType,
        autoCache = autoCache,
        systemUpdateReminder = systemUpdateReminder,
        allBookIds = allBookIds,
        pinnedBookIds = pinnedBookIds,
        updatedBookIds = updatedBookIds,
    )