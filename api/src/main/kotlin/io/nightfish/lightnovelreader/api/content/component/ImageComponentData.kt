package io.nightfish.lightnovelreader.api.content.component

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import org.dom4j.DocumentHelper
import org.dom4j.Element


@Serializable
data class ImageComponentData(
    @Serializable(with = UriSerializer::class)
    val uri: Uri
): AbstractContentComponentData() {
    class UriSerializer : KSerializer<Uri> {
        override val descriptor: SerialDescriptor =
            PrimitiveSerialDescriptor("Uri", PrimitiveKind.STRING)

        override fun serialize(encoder: Encoder, value: Uri) {
            encoder.encodeString(value.toString())
        }

        override fun deserialize(decoder: Decoder): Uri {
            val uriString = decoder.decodeString()
            return uriString.toUri()
        }
    }

    override val id: String = ID
    override fun toJsonElement(): JsonElement = Json.encodeToJsonElement(this)

    override fun toHtmlElement(context: Context): Element = DocumentHelper.createElement("div").apply {
        addElement("img").apply {
            addAttribute("src", uri.toString())
        }
    }

    companion object {
        const val ID = "image"
        val jsonSerializer = object: ComponentDataJsonElementSerializer<ImageComponentData> {
            override fun toJsonElement(data: ImageComponentData): JsonElement = Json.encodeToJsonElement(data)
            override fun fromJsonElement(json: JsonElement): ImageComponentData = Json.decodeFromJsonElement(json)
        }
    }
}