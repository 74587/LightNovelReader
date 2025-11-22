package io.nightfish.lightnovelreader.plugin.js.api.book

import io.nightfish.lightnovelreader.api.book.ChapterContent
import kotlinx.serialization.json.JsonObject

data class JsChapterContent(
    override val id: String,
    override val title: String,
    override val content: JsonObject,
    override val lastChapter: String,
    override val nextChapter: String
): ChapterContent