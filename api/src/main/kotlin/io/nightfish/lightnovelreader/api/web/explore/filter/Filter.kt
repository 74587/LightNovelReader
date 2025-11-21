package io.nightfish.lightnovelreader.api.web.explore.filter

abstract class Filter {
    abstract fun getType(): FilterTypes
    abstract fun getTitle(): String
}