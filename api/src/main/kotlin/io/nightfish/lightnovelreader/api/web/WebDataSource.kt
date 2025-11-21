package io.nightfish.lightnovelreader.api.web

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class WebDataSource(
    val name: String,
    val provider: String
)
