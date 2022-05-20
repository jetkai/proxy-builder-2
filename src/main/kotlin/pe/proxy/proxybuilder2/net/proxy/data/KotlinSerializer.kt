package pe.proxy.proxybuilder2.net.proxy.data

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class KotlinSerializer {

    inline fun <reified T> encode(value : T) : String {
        val data = Json { this.encodeDefaults = true; this.ignoreUnknownKeys = true }
        return data.encodeToString(value)
    }

}