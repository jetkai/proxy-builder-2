package pe.proxy.proxybuilder2.net.proxy.supplier

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import pe.proxy.proxybuilder2.net.proxy.data.FinalProxyDataType
import pe.proxy.proxybuilder2.net.proxy.data.FinalProxyListData
import pe.proxy.proxybuilder2.net.proxy.data.SupplierProxyListData
import pe.proxy.proxybuilder2.util.YamlProperties
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

class CustomProxySupplier(override val finalProxyList : FinalProxyListData, appConfig : YamlProperties) : IProxySupplier {

    private val logger = LoggerFactory.getLogger(CustomProxySupplier::class.java)

    private val endpointURL = appConfig.proxySupplier.customUrl

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
        if(unparsed == null)
            return logger.error("Unable to read unparsed body data from supplier")

        val data = Json { this.encodeDefaults = true; this.ignoreUnknownKeys = true }
        val parsed = data.decodeFromString<SupplierProxyListData>(unparsed!!)

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
                finalProxyList.proxies.add(FinalProxyDataType(key, ip, port.toInt()))
            }
        }

        logger.info(
            "Parsing complete -> " +
                    "[HTTP:${finalProxyList.size("http")} | HTTPS:${finalProxyList.size("https")} | " +
                    "SOCKS4:${finalProxyList.size("socks4")} | SOCKS5:${finalProxyList.size("socks5")}]"
        )
    }
}