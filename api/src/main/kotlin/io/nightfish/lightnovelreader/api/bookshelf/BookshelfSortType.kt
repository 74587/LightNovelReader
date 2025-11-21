package io.nightfish.lightnovelreader.api.bookshelf

enum class BookshelfSortType(val key: String) {
    Default("default"),
    Latest("latest");
    companion object {
        fun map(key: String): BookshelfSortType = entries.first { it.key == key }
    }
}