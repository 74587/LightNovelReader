package indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.content

import androidx.compose.runtime.Stable
import io.nightfish.lightnovelreader.api.book.ChapterContent
import io.nightfish.lightnovelreader.api.content.component.AbstractContentComponent

@Stable
interface ContentUiState {
    val bookId: String
    val readingChapterContent: ChapterContent
    val readingProgress: Float
    val loadNextChapter: () -> Unit
    val loadLastChapter: () -> Unit
    val changeChapter: (String) -> Unit
    val contentComponentsMap: Map<String, List<AbstractContentComponent<*>>>
}