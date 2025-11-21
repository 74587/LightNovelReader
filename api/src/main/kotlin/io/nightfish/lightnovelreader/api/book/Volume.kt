package io.nightfish.lightnovelreader.api.book

data class Volume(
    val volumeId: String,
    val volumeTitle: String,
    val chapters: List<ChapterInformation>,
): CanBeEmpty {
    override fun isEmpty(): Boolean = volumeId.isEmpty()
}
