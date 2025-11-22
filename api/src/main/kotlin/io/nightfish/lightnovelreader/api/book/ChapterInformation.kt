package io.nightfish.lightnovelreader.api.book

data class ChapterInformation(
    val id: String,
    val title: String
): CanBeEmpty {
    override fun isEmpty(): Boolean = id.isEmpty()
}