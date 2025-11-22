package io.nightfish.lightnovelreader.api.text

interface TextProcessingRepositoryApi {
    fun registerProcessors(processor: TextProcessor)
}