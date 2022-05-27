package pe.proxy.proxybuilder2.database

import com.fasterxml.jackson.annotation.JsonProperty
import kotlinx.serialization.Serializable
import pe.proxy.proxybuilder2.net.proxy.data.*
import pe.proxy.proxybuilder2.net.proxy.proxycheckio.LocationData
import pe.proxy.proxybuilder2.net.proxy.proxycheckio.OperatorData
import pe.proxy.proxybuilder2.net.proxy.proxycheckio.RiskData
import pe.proxy.proxybuilder2.net.proxy.tester.ProxyChannelData
import pe.proxy.proxybuilder2.util.KotlinDeserializer
import pe.proxy.proxybuilder2.util.Utils
import pe.proxy.proxybuilder2.util.writer.CustomFileWriter

/**
 * EntityChannelData
 *
 * @author Kai
 * @version 1.0, 23/05/2022
 */
data class EntityChannelData(val entity : ProxyEntity, val proxy : ProxyChannelData)

@Serializable
data class EntityForPublicView(
    var ip: String? = null,
    var port: Int? = null,
    var ping: Long? = null,
    var uptime: Int? = null,
    var protocols: List<ProtocolDataType>? = null,
    var credentials: ProxyCredentials? = null,
    var connections: PerformanceConnectData? = null,
    var detection: RiskData? = null,
    var provider: OperatorData? = null,
    var location: LocationData? = null,
    @JsonProperty("date_added")
    var dateAdded: String? = null,
    @JsonProperty("last_tested")
    var lastTested: String? = null,
    @JsonProperty("last_success")
    var lastSuccess: String? = null) {

    fun classic(proxy : ProxyEntity) : SupplierProxyListData {
        val entity = SupplierProxyListData(mutableListOf(), mutableListOf(), mutableListOf(), mutableListOf())
        if(proxy.protocols?.contains("\"http\"") == true)
            entity.http.add("${proxy.ip}:${proxy.port}")
        if(proxy.protocols?.contains("\"https\"") == true)
            entity.https.add("${proxy.ip}:${proxy.port}")
        if(proxy.protocols?.contains("\"socks4\"") == true)
            entity.socks4.add("${proxy.ip}:${proxy.port}")
        if(proxy.protocols?.contains("\"socks5\"") == true)
            entity.socks5.add("${proxy.ip}:${proxy.port}")
        return entity
    }

    //Temp deserializer (for testing) - this is currently hybrid with KTX & Jackson - Bad
    //Jackson doesn't parse KTX string decode properly, not sure how to parse KTX JsonElement to Jackson ATM
    fun basic(proxy : ProxyEntity) : EntityForPublicView {
        ip = proxy.ip
        port = proxy.port
        protocols = KotlinDeserializer.decode<ProtocolData?>(proxy.protocols!!)?.protocol
        ping = KotlinDeserializer.decode<PerformanceConnectData?>(proxy.connections!!)
            ?.let { Utils.lowestPing(it) }
        return this
    }

    //Temp deserializer (for testing) - this is currently hybrid with KTX & Jackson - Bad
    //Jackson doesn't parse KTX string decode properly, not sure how to parse KTX JsonElement to Jackson ATM
    fun advanced(proxy : ProxyEntity) : EntityForPublicView {
        ip = proxy.ip
        port = proxy.port
        credentials = KotlinDeserializer.decode(proxy.credentials)
        protocols = KotlinDeserializer.decode<ProtocolData?>(proxy.protocols!!)?.protocol
        connections = KotlinDeserializer.decode(proxy.connections)
        detection = KotlinDeserializer.decode(proxy.detection)
        provider = try {
            KotlinDeserializer.decode(proxy.provider)
        } catch (e : Exception) {
            OperatorData(proxy.provider)
        }
        location = KotlinDeserializer.decode(proxy.location)
        dateAdded = proxy.dateAdded.toString()
        lastSuccess = proxy.lastSuccess.toString()
        lastTested = proxy.lastTested.toString()
        return this
    }

}

data class EntityForPublicViewForCSV(
    var ip: String? = null,
    var port: Int? = null,
    var ping: Long? = null,
    var protocols: String? = null,
    var username: String? = null,
    var password: String? = null,
    var detected: Boolean? = null,
    var provider: String? = null,
    var organisation: String? = null,
    var country: String? = null,
    var isocode: String? = null,
    var latitude: Float? = null,
    var longitude: Float? = null,
    var asn: String? = null,
    var connections: String? = null,
    var detection: String? = null,
    var uptime: Int? = null,
    @JsonProperty("date_added")
    var dateAdded: String? = null,
    @JsonProperty("last_tested")
    var lastTested: String? = null,
    @JsonProperty("last_success")
    var lastSuccess: String? = null) {

    //TODO - Change this, creating extra EntityForPublicViewForCSV() for no reason
    fun convert(entities : List<EntityForPublicView>) : List<EntityForPublicViewForCSV> {
        val entitiesCsv = mutableListOf<EntityForPublicViewForCSV>()
        for(entity in entities) {
            val entityCsv = EntityForPublicViewForCSV()
            entityCsv.ip = entity.ip
            entityCsv.port = entity.port
            entityCsv.ping = entity.ping
            entityCsv.protocols = entity.protocols.toString()
            entityCsv.username = entity.credentials?.username
            entityCsv.password = entity.credentials?.password
            entityCsv.detected = entity.detection?.proxy
            entityCsv.organisation = entity.location?.organisation
            entityCsv.country = entity.location?.country
            entityCsv.isocode = entity.location?.isocode
            entityCsv.latitude = entity.location?.latitude
            entityCsv.longitude = entity.location?.longitude
            entityCsv.asn = entity.location?.asn
            entityCsv.connections = entity.connections.toString()
            entityCsv.uptime = entity.uptime
            entityCsv.dateAdded = entity.dateAdded
            entityCsv.lastTested = entity.lastTested
            entityCsv.dateAdded = entity.lastSuccess
            entitiesCsv.add(entityCsv)
        }
        return entitiesCsv
    }

    fun order(viewType : CustomFileWriter.ViewType): Array<String> {
        return when (viewType) {
            CustomFileWriter.ViewType.ADVANCED -> {
                arrayOf(
                    "ip", "port", "username", "password", "protocols", "ping", "detected",
                    "organisation", "country", "isocode", "latitude", "longitude", "asn",
                    "connections", "uptime", "dateAdded", "lastTested", "lastSuccess"
                )
            }
            CustomFileWriter.ViewType.BASIC -> { arrayOf("ip", "port", "protocols", "ping") }
            else -> { arrayOf("http", "https", "socks4", "socks5") }
        }
    }

}