package pe.proxy.proxybuilder2.net.proxy.proxycheckio

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component
import pe.proxy.proxybuilder2.database.ProxyEntity
import pe.proxy.proxybuilder2.database.ProxyRepository
import pe.proxy.proxybuilder2.net.proxy.data.KotlinSerializer
import pe.proxy.proxybuilder2.util.YamlProperties
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

@Component
class QueryApi(private val proxyRepository : ProxyRepository, private val appConfig : YamlProperties)
    : ApplicationListener<ApplicationReadyEvent> {

    val client : HttpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build()
    val builder : HttpRequest.Builder = HttpRequest.newBuilder()

    override fun onApplicationEvent(event: ApplicationReadyEvent) {
        query()
    }

    fun query() {
        val proxies = proxyRepository.findByCountryData("")
        for(proxy in proxies) {
            val request = builder.uri(apiURI(proxy.ip!!))
                .GET()
                .timeout(Duration.ofSeconds(5))
                .build()

            val jsonResponse = client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).get().body()

            pickFromJson(proxy, jsonResponse, proxy.ip!!)


           // val country = KotlinDeserializer().decode<LocationData>(proxyInfo)
        }
        proxyRepository.saveAll(proxies)
    }

    fun pickFromJson(entity : ProxyEntity, jsonResponse : String, proxyIp: String) {
        val mapper = ObjectMapper()
        val factory: JsonFactory = mapper.factory
        val parser: JsonParser = factory.createParser(jsonResponse)
        val actualObj : JsonNode = mapper.readTree(parser)
        val proxyInfo = actualObj.get(proxyIp)

        //Change to Reflection
        val locationData = LocationData(
            proxyInfo.get("continent").textValue(),
            proxyInfo.get("country").textValue(),
            proxyInfo.get("isocode").textValue(),
            proxyInfo.get("region").textValue(),
            proxyInfo.get("regioncode").textValue(),
            proxyInfo.get("city").textValue(),
            proxyInfo.get("latitude").floatValue(),
            proxyInfo.get("longitude").floatValue(),
            proxyInfo.get("provider").textValue(),
            proxyInfo.get("organisation").textValue(),
            proxyInfo.get("asn").textValue()
        )

        val riskData = RiskData(
            proxyInfo.get("proxy").textValue() == "yes",
            proxyInfo.get("type").textValue(),
            proxyInfo.get("risk").intValue(),
        )

        val operatorInfo = proxyInfo.get("operator")
        val policiesInfo = operatorInfo.get("policies")
        val policies = Policies(
            policiesInfo.get("ad_filtering").textValue() == "yes",
            policiesInfo.get("free_access").textValue() == "yes",
            policiesInfo.get("paid_access").textValue() == "yes",
            policiesInfo.get("port_forwarding").textValue() == "yes",
            policiesInfo.get("logging").textValue() == "yes",
            policiesInfo.get("anonymous_payments").textValue() == "yes",
            policiesInfo.get("crypto_payments").textValue() == "yes",
            policiesInfo.get("traceable_ownership").textValue() == "yes",
        )
        val operatorData = OperatorData(
            operatorInfo.get("name").textValue(),
            operatorInfo.get("url").textValue(),
            operatorInfo.get("anonymity").textValue(),
            operatorInfo.get("popularity").textValue(),
            operatorInfo.get("protocols").toMutableList(),
            policies,
        )

        entity.countryData = KotlinSerializer().encode(locationData)
        entity.riskData = KotlinSerializer().encode(riskData)

    }

    fun newMapper() {
        val newMapper = ObjectMapper()
        val factory : JsonFactory = newMapper.factory
       // val node : JsonNode = newMapper.tree
        val locationMap = HashMap<String, Any>()
    }

    fun apiURI(proxyIp : String) : URI {
        return URI.create("http://proxycheck.io/v2/$proxyIp?" +
                "key=${appConfig.proxyCheckApiKey}&vpn=1&asn=1&risk=2&port=1&seen=1&tag=msg")
        //http://proxycheck.io/v2/80.90.80.54?key=7y658u-044228-737v80-64lq59"
    }

}