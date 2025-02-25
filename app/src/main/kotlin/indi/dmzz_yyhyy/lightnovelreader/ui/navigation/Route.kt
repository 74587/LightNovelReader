package indi.dmzz_yyhyy.lightnovelreader.ui.navigation

import kotlinx.serialization.Serializable

object Route {
    @Serializable
    object Home {
        @Serializable
        object Reading
        @Serializable
        object Bookshelf {
            @Serializable
            object Home
            @Serializable
            data class Edit(
                val id: Int,
                val title: String
            )
            @Serializable
            data class DeleteBookshelfDialog(
                val bookshelfId: Int
            )
            @Serializable
            data class AddBookToBookshelfDialog(
                val selectedBookIds: List<Int>
            )
        }
        @Serializable
        object Exploration {
            @Serializable
            object Home
            @Serializable
            object Search
            @Serializable
            data class Expanded(
                val expandedPageDataSourceId: String
            )
        }
        @Serializable
        object Settings {
            @Serializable
            object Home
            @Serializable
            object Debug
            @Serializable
            object Translate
        }
        @Serializable
        object SourceChangeDialog
        @Serializable
        object ExportUserDataDialog
    }
    @Serializable
    object Book {
        @Serializable
        data class Detail(
            val bookId: Int
        )
        @Serializable
        data class Content (
            val bookId: Int,
            val chapterId: Int
        )
        @Serializable
        data class ExportUserDataDialog(
            val bookId: Int,
            val title: String
        )
        @Serializable
        data class ColorPickerDialog(
            val colorUserDataPath: String,
            val colors: LongArray
        ) {
            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (javaClass != other?.javaClass) return false

                other as ColorPickerDialog

                if (colorUserDataPath != other.colorUserDataPath) return false
                if (!colors.contentEquals(other.colors)) return false

                return true
            }

            override fun hashCode(): Int {
                var result = colorUserDataPath.hashCode()
                result = 31 * result + colors.contentHashCode()
                return result
            }
        }
    }
    @Serializable
    object UpdatesAvailableDialog
    @Serializable
    data class AddBookToBookshelfDialog(
        val bookId: Int
    )
}