package pe.proxy.proxybuilder2.net.proxy.supplier

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import pe.proxy.proxybuilder2.net.proxy.data.SupplierProxyListData
import pe.proxy.proxybuilder2.util.YamlProperties
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

class CustomProxySupplier(override val proxies : SupplierProxyListData, appConfig : YamlProperties) : IProxySupplier {

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

        proxies.http.addAll(parsed.http)
        proxies.https.addAll(parsed.https)
        proxies.socks4.addAll(parsed.socks4)
        proxies.socks5.addAll(parsed.socks5)

        logger.info(
            "Parsing complete -> " +
                "[HTTP:${proxies.http.size} | HTTPS:${proxies.https.size} | " +
                "SOCKS4:${proxies.socks4.size} | SOCKS5:${proxies.socks5.size}]"
        )
    }
}