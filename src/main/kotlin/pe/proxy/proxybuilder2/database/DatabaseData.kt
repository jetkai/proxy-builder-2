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
import java.util.*

/**
 * EntityChannelData
 *
 * @author Kai
 * @version 1.0, 23/05/2022
 */
data class EntityChannelData(val entity : ProxyEntity, val proxy : ProxyChannelData)

@Serializable
data class EntityForPublicView(
    var ip : String?=null,
    var port : Int?=null,
    var ping : Long?=null,
    var uptime : Int?=null,
    var protocols : List<ProtocolDataType>?=null,
    var credentials : ProxyCredentials?=null,
    var connections : PerformanceConnectData?=null,
    var detection : RiskData?=null,
    var provider : OperatorData?=null,
    var location : LocationData?=null,
    @JsonProperty("date_added")
    var dateAdded : String?=null,
    @JsonProperty("last_tested")
    var lastTested : String?=null,
    @JsonProperty("last_success")
    var lastSuccess : String?=null) {

    fun basic(proxy : ProxyEntity) : EntityForPublicView {
        val deserializer = KotlinDeserializer()
        val entity = EntityForPublicView()
        entity.ip = proxy.ip
        entity.port = proxy.port
        entity.protocols = deserializer.decode<ProtocolData?>(proxy.protocols!!)?.protocol
        entity.ping = deserializer.decode<PerformanceConnectData?>(proxy.connections!!)?.let { Utils.lowestPing(it) }
        return entity
    }

    fun advanced(proxy : ProxyEntity) : EntityForPublicView {
        val deserializer = KotlinDeserializer()
        val entity = EntityForPublicView()
        entity.ip = proxy.ip
        entity.port = proxy.port
        entity.credentials = deserializer.decode(proxy.credentials)
        entity.protocols = deserializer.decode<ProtocolData?>(proxy.protocols!!)?.protocol
        entity.connections = deserializer.decode(proxy.connections)
        entity.detection = deserializer.decode(proxy.detection)
        entity.provider = deserializer.decode(proxy.provider)
        entity.location = deserializer.decode(proxy.location)
        entity.dateAdded = proxy.dateAdded.toString()
        entity.lastSuccess = proxy.lastSuccess.toString()
        entity.lastTested = proxy.lastTested.toString()
        return entity
    }

}

data class EntityForPublicViewForCSV(
    var ip : String?=null,
    var port : Int?=null,
    var ping : Long?=null,
    var protocols : String?=null,
    var username : String?=null,
    var password : String?=null,
    var detected : Boolean?=null,
    var provider : String?=null,
    var organisation : String?=null,
    var country : String?=null,
    var isocode : String?=null,
    var latitude : String?=null,
    var longitude : String?=null,
    var asn : String?=null,
    var connections : String?=null,
    var detection : String?=null,
    var uptime : Int?=null,
    @JsonProperty("date_added")
    var dateAdded : Date?=null,
    @JsonProperty("last_tested")
    var lastTested : Date?=null)