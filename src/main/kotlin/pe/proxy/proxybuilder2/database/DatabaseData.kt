package pe.proxy.proxybuilder2.database

import com.fasterxml.jackson.annotation.JsonProperty
import kotlinx.serialization.Serializable
import pe.proxy.proxybuilder2.net.proxy.data.PerformanceConnectData
import pe.proxy.proxybuilder2.net.proxy.data.ProtocolData
import pe.proxy.proxybuilder2.net.proxy.data.ProtocolDataType
import pe.proxy.proxybuilder2.net.proxy.data.ProxyCredentials
import pe.proxy.proxybuilder2.net.proxy.proxycheckio.LocationData
import pe.proxy.proxybuilder2.net.proxy.proxycheckio.OperatorData
import pe.proxy.proxybuilder2.net.proxy.proxycheckio.RiskData
import pe.proxy.proxybuilder2.net.proxy.tester.ProxyChannelData
import pe.proxy.proxybuilder2.util.KotlinDeserializer
import pe.proxy.proxybuilder2.util.Utils

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

    fun basic(proxy : ProxyEntity) : EntityForPublicView {
        val entity = EntityForPublicView()
        entity.ip = proxy.ip
        entity.port = proxy.port
        entity.protocols = KotlinDeserializer.decode<ProtocolData?>(proxy.protocols!!)?.protocol
        entity.ping = KotlinDeserializer.decode<PerformanceConnectData?>(proxy.connections!!)
            ?.let { Utils.lowestPing(it) }
        return entity
    }

    fun advanced(proxy : ProxyEntity) : EntityForPublicView {
        val entity = EntityForPublicView()
        entity.ip = proxy.ip
        entity.port = proxy.port
        entity.credentials = KotlinDeserializer.decode(proxy.credentials)
        entity.protocols = KotlinDeserializer.decode<ProtocolData?>(proxy.protocols!!)?.protocol
        entity.connections = KotlinDeserializer.decode(proxy.connections)
        entity.detection = KotlinDeserializer.decode(proxy.detection)
        entity.provider = KotlinDeserializer.decode(proxy.provider)
        entity.location = KotlinDeserializer.decode(proxy.location)
        entity.dateAdded = proxy.dateAdded.toString()
        entity.lastSuccess = proxy.lastSuccess.toString()
        entity.lastTested = proxy.lastTested.toString()
        return entity
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

    fun order(): Array<String> {
        return arrayOf(
            "ip", "port", "username", "password", "protocols", "ping", "detected",
            "organisation", "country", "isocode", "latitude", "longitude", "asn",
            "connections", "uptime", "dateAdded", "lastTested", "lastSuccess"
        )
    }

}