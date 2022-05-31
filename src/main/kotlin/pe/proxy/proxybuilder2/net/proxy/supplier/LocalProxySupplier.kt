package pe.proxy.proxybuilder2.net.proxy.supplier

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import pe.proxy.proxybuilder2.net.proxy.data.FinalProxyDataType
import pe.proxy.proxybuilder2.net.proxy.data.FinalProxyListData
import pe.proxy.proxybuilder2.net.proxy.data.SupplierProxyListData
import pe.proxy.proxybuilder2.util.ProxyConfig
import java.io.File

/**
 * LocalProxySupplier
 *
 * @author Kai
 * @version 1.0, 15/05/2022
 */
class LocalProxySupplier(override val data : FinalProxyListData, appConfig : ProxyConfig) : IProxySupplier {

    private val logger = LoggerFactory.getLogger(LocalProxySupplier::class.java)

    private val endpointURL = appConfig.supplier.customUrl

    override fun request() : LocalProxySupplier {
        return this
    }

    //TODO Modify parsing for CustomProxySupplier
    override fun parse() {

        val data = Json { this.encodeDefaults = true; this.ignoreUnknownKeys = true }

        val parsed = SupplierProxyListData(mutableListOf(), mutableListOf(), mutableListOf(), mutableListOf())

        File("proxies/").listFiles()?.forEach {
            if(it.extension.contains("json")) {
                val prox = it?.readText()?.let { it1 -> data.decodeFromString<SupplierProxyListData>(it1) }
                if(prox != null) {
                    parsed.http.addAll(prox.http)
                    parsed.https.addAll(prox.https)
                    parsed.socks4.addAll(prox.socks4)
                    parsed.socks5.addAll(prox.socks5)
                }
            }
        }

        val hashMap = HashMap<String, MutableList<String>>()

        hashMap["http"] = parsed.http
        hashMap["https"] = parsed.https
        hashMap["socks4"] = parsed.socks4
        hashMap["socks5"] = parsed.socks5

        for ((key, proxyList) in hashMap) {
            for(proxy in proxyList) {
                if(!proxy.contains(":") || proxy.length > 23)
                    continue
                val ip = proxy.split(":")[0]
                val port = proxy.split(":")[1]
                this.data.proxies.add(FinalProxyDataType(key, ip, port.toInt()))
            }
        }

        logger.info(
            "Parsing complete -> " +
                    "[HTTP:${this.data.size("http")} | HTTPS:${this.data.size("https")} | " +
                    "SOCKS4:${this.data.size("socks4")} | SOCKS5:${this.data.size("socks5")}]"
        )
    }
}