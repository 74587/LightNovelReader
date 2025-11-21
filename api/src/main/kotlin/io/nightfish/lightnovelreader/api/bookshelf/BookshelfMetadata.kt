package io.nightfish.lightnovelreader.api.bookshelf

import java.time.LocalDateTime

data class BookshelfBookMetadata(
    val id: String,
    val lastUpdate: LocalDateTime,
    val bookShelfIds: List<Int>,
)

