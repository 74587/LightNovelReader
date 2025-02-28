package indi.dmzz_yyhyy.lightnovelreader.data.text

import indi.dmzz_yyhyy.lightnovelreader.data.book.ChapterContent
import indi.dmzz_yyhyy.lightnovelreader.data.userdata.UserDataPath
import indi.dmzz_yyhyy.lightnovelreader.data.userdata.UserDataRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TextProcessingRepository @Inject constructor(
    userDataRepository: UserDataRepository,
    private val mlTranslateProcessor: MLTranslateProcessor

) {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private val processors = mutableSetOf<TextProcessor>()
    private val _updateFlow = MutableStateFlow(0)
    val updateFlow: Flow<Int> = _updateFlow

    init {
        coroutineScope.launch {
            userDataRepository.booleanUserData(UserDataPath.Reader.EnableSimplifiedTraditionalTransform.path)
                .getFlow()
                .collect { enableSimplifiedTraditionalTransform ->
                    if (enableSimplifiedTraditionalTransform == true)
                        processors.add(SimplifiedTraditionalProcessor)
                    else
                        processors.remove(SimplifiedTraditionalProcessor)
                    _updateFlow.update { it + 1 }
                }
        }
        coroutineScope.launch {
            userDataRepository.booleanUserData(UserDataPath.Reader.EnableMLTranslate.path)
                .getFlow()
                .collect { enableMLTranslate ->
                    if (enableMLTranslate == true) {
                        processors.add(mlTranslateProcessor)
                    } else {
                        processors.remove(mlTranslateProcessor)
                    }
                    _updateFlow.update { it + 1 }
                }
        }
    }


    fun processChapterContent(flow: Flow<ChapterContent>): Flow<ChapterContent> {
        return flow.map { chapterContent ->
            var text = chapterContent.content
            processors.forEach {
                text = it.processor(text)
            }
            return@map chapterContent.copy(
                content = text
            )
        }
    }
}