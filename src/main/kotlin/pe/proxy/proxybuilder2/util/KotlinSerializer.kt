package pe.proxy.proxybuilder2.util

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.encodeToJsonElement

/**
 * KotlinSerializer
 *
 * @author Kai
 * @version 1.0, 19/05/2022
 */
object KotlinSerializer {

    val json = Json { this.encodeDefaults = true; this.ignoreUnknownKeys = true }

    inline fun <reified T> encode(value : T) : String {
        return json.encodeToString(value)
    }

    inline fun <reified T> encodeElement(value : T) : JsonElement {
        return json.encodeToJsonElement(value)
    }

}