package indi.dmzz_yyhyy.lightnovelreader.data.text

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.security.MessageDigest
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MLTranslateProcessor @Inject constructor(
    private val translatorRepository: MLTranslatorRepository,
) : TextProcessor {

    private val translationCache = ConcurrentHashMap<String, String>()
    private val processorScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    init {
        processorScope.launch {
            translatorRepository.targetLanguage.collect {
                translationCache.clear()
            }
        }
    }

    override fun processor(text: String): String {
        return runBlocking(Dispatchers.IO) {
            processWithCache(text)
        }
    }

    private suspend fun processWithCache(text: String): String = coroutineScope {
        val targetLanguage = translatorRepository.getCurrentTargetLanguage()

        text.split("\n")
            .map { paragraph ->
                async {
                    if (paragraph.isBlank()) return@async paragraph
                    val cacheKey = cacheGen(targetLanguage, paragraph)
                    translationCache[cacheKey] ?: translateWithCache(paragraph, cacheKey)
                }
            }
            .awaitAll()
            .joinToString("\n")
    }

    private suspend fun translateWithCache(paragraph: String, cacheKey: String): String {
        return try {
            val translated = translatorRepository.textTranslate(paragraph)
            translationCache[cacheKey] = translated
            translated
        } catch (e: Exception) {
            "#translation_error. 检查翻译设置 / Please check translation settings / 翻訳設定を確認してください / 번역 설정을 확인해 주세요."
        }
    }

    private fun cacheGen(language: String, text: String): String {
        val textHash = hash(text)
        return buildString { append(language).append("_").append(textHash)}
    }

    private fun hash(input: String): String {
        return MessageDigest
            .getInstance("SHA-1")
            .digest(input.toByteArray())
            .fold("") { str, it ->
                str + "%02x".format(it)
            }
    }
}