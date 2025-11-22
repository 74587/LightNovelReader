package io.nightfish.lightnovelreader.api.explore

import android.net.Uri

data class ExploreDisplayBook(
    val id: String,
    val title: String,
    val author: String,
    val coverUri: Uri,
)