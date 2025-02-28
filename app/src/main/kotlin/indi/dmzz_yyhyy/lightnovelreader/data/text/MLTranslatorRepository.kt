package indi.dmzz_yyhyy.lightnovelreader.data.text

import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.TranslateRemoteModel
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import indi.dmzz_yyhyy.lightnovelreader.data.userdata.UserDataPath
import indi.dmzz_yyhyy.lightnovelreader.data.userdata.UserDataRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

@Singleton
class MLTranslatorRepository @Inject constructor(
    private val userDataRepository: UserDataRepository,
) {
    private val modelManager = RemoteModelManager.getInstance()
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private val _targetLanguage = MutableStateFlow("")
    val targetLanguage: StateFlow<String> = _targetLanguage

    fun getCurrentTargetLanguage() = _targetLanguage.value

    init {
        coroutineScope.launch {
            initializeTranslator()
            userDataRepository.stringUserData(UserDataPath.Reader.TranslateTargetLanguage.path)
                .getFlow().collect { translateTargetLanguage ->
                    _targetLanguage.value = translateTargetLanguage.toString()
                }
        }
    }

    private suspend fun initializeTranslator() {
        val srcLang = TranslateLanguage.CHINESE
        val targetLang = targetLanguage.value.takeIf { it.isNotEmpty() } ?: return

        val options = TranslatorOptions.Builder()
            .setSourceLanguage(srcLang)
            .setTargetLanguage(targetLang)
            .build()

        val translator = Translation.getClient(options)
        ensureModelAvailable(translator)
    }

    suspend fun textTranslate(text: String): String = suspendCancellableCoroutine { cont ->
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.fromLanguageTag("zh").toString())
            .setTargetLanguage(TranslateLanguage.fromLanguageTag(targetLanguage.value).toString())
            .build()
        val translator = Translation.getClient(options)

        val conditions = DownloadConditions.Builder().build()
        translator.downloadModelIfNeeded(conditions)
            .addOnSuccessListener {
                translator.translate(text)
                    .addOnSuccessListener { result ->
                        cont.resume(result)
                        translator.close()
                    }
                    .addOnFailureListener { e ->
                        cont.resumeWithException(e)
                        translator.close()
                    }
            }
            .addOnFailureListener { e ->
                cont.resumeWithException(NoSuchMethodException())
            }
    }


    suspend fun getAvailableLanguages(): List<String> = suspendCoroutine { continuation ->
        val modelManager = RemoteModelManager.getInstance()
        modelManager.getDownloadedModels(TranslateRemoteModel::class.java)
            .addOnSuccessListener {
                continuation.resume(it.toList().map { it.language })
            }
            .addOnFailureListener {
                continuation.resume(emptyList())
            }
    }

    suspend fun downloadModel(language: String) = suspendCoroutine { continuation ->
        val model = TranslateRemoteModel.Builder(
            TranslateLanguage.fromLanguageTag(language) ?: ""
        ).build()
        val conditions = DownloadConditions.Builder().build()

        modelManager.download(model, conditions)
            .addOnSuccessListener {
                continuation.resume(true)
            }
            .addOnFailureListener {
                continuation.resume(true)
            }
    }

    suspend fun deleteModel(language: String)  = suspendCoroutine { continuation ->
        val model = TranslateRemoteModel.Builder(
            TranslateLanguage.fromLanguageTag(language) ?: ""
        ).build()

        modelManager.deleteDownloadedModel(model)
            .addOnSuccessListener {
                continuation.resume(true)
            }
            .addOnFailureListener {
                continuation.resume(true)
            }
    }

    private suspend fun ensureModelAvailable(translator: Translator) = suspendCoroutine { continuation ->
        val conditions = DownloadConditions.Builder().build()
        translator.downloadModelIfNeeded(conditions)
            .addOnSuccessListener {
                continuation.resume(true)
            }
            .addOnFailureListener {
                continuation.resume(false)
            }
    }

}