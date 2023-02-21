package pe.proxy.proxybuilder2.net.proxy.supplier

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import pe.proxy.proxybuilder2.net.proxy.data.SimpleProxyDataList
import pe.proxy.proxybuilder2.net.proxy.data.SimpleProxyDataType
import pe.proxy.proxybuilder2.net.proxy.data.SupplierProxyListData
import pe.proxy.proxybuilder2.util.ProxyConfig
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

/**
 * CustomProxySupplier
 *
 * @author Kai
 * @version 1.0, 15/05/2022
 */
class CustomProxySupplier(override val data : SimpleProxyDataList, appConfig : ProxyConfig) : IProxySupplier {

    private val logger = LoggerFactory.getLogger(CustomProxySupplier::class.java)

    private val endpointURL = appConfig.supplier.customUrl

    private val client : HttpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build()
    private val builder : HttpRequest.Builder = HttpRequest.newBuilder()

    private var unparsed : String?= null

    override fun request() : CustomProxySupplier {
        val httpRequest = builder.uri(URI.create(endpointURL)).GET().timeout(Duration.ofSeconds(15)).build()
        this.unparsed = client.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofString()).get().body()
        return this
    }

    //TODO Modify parsing for CustomProxySupplier
    override fun parse() {
        val unparsed = this.unparsed ?: return logger.error("Unable to read unparsed body data from supplier")

        val data = Json { this.encodeDefaults = true; this.ignoreUnknownKeys = true }
        val parsed = data.decodeFromString<SupplierProxyListData>(unparsed)

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
                this.data.proxies[ip] = SimpleProxyDataType(key, ip, port.toInt())
            }
        }

        logger.info(
            "Parsing complete -> " +
                    "[HTTP:${this.data.size("http")} | HTTPS:${this.data.size("https")} | " +
                    "SOCKS4:${this.data.size("socks4")} | SOCKS5:${this.data.size("socks5")}]"
        )
    }
}