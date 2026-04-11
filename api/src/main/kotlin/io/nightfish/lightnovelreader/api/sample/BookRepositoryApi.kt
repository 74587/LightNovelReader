package io.nightfish.lightnovelreader.api.sample

import io.nightfish.lightnovelreader.api.book.BookRepositoryApi

fun updateUserReadingData(bookRepositoryApi: BookRepositoryApi) {
    bookRepositoryApi.updateUserReadingData("ciallo") {
        it.apply {
            this.readingProgress = 1f
        }
    }
}