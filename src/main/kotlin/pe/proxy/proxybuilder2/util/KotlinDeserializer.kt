package pe.proxy.proxybuilder2.util

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement

/**
 * KotlinDeserializer
 *
 * @author Kai
 * @version 1.0, 19/05/2022
 */
object KotlinDeserializer {

    val data = Json { this.encodeDefaults = true; this.ignoreUnknownKeys = true }

    inline fun <reified T> decode(value : String?) : T? {
        if(value == null)
            return null

        return data.decodeFromString(value)
    }

    inline fun <reified T> decodeFromElement(value : JsonElement) : T {
        return data.decodeFromJsonElement(value)
    }

}