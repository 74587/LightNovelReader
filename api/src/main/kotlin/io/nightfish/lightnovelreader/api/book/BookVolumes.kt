package io.nightfish.lightnovelreader.api.book


data class BookVolumes(
    val bookId: String,
    val volumes: List<Volume>
): CanBeEmpty {
    companion object {
        fun empty() = BookVolumes("", emptyList())
        fun empty(bookId: String) = BookVolumes(bookId, emptyList())
    }

    override fun isEmpty(): Boolean = volumes.isEmpty()
}
