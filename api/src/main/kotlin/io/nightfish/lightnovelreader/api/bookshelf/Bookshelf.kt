package io.nightfish.lightnovelreader.api.bookshelf

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

@Stable
interface Bookshelf {
    val id: Int
    val name: String
    val sortType: BookshelfSortType
    val autoCache: Boolean
    val systemUpdateReminder: Boolean
    val allBookIds: List<String>
    val pinnedBookIds: List<String>
    val updatedBookIds: List<String>
    fun isEmpty() = this.id == -1
}

class MutableBookshelf : Bookshelf {
    override var id by mutableIntStateOf(-1)
    override var name by mutableStateOf("")
    override var sortType by mutableStateOf(BookshelfSortType.Default)
    override var autoCache by mutableStateOf(false)
    override var systemUpdateReminder by mutableStateOf(false)
    override var allBookIds by mutableStateOf<List<String>>(listOf())
    override var pinnedBookIds by mutableStateOf<List<String>>(listOf())
    override var updatedBookIds by mutableStateOf<List<String>>(listOf())
}
