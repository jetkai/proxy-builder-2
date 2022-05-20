package pe.proxy.proxybuilder2.net.proxy.data

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class KotlinDeserializer {

    inline fun <reified T> decode(value : String) : T {
        val data = Json { this.encodeDefaults = true; this.ignoreUnknownKeys = true }
        return data.decodeFromString<T>(value)
    }

}