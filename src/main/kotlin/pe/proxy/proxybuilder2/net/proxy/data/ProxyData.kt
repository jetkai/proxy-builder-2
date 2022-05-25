package pe.proxy.proxybuilder2.net.proxy.data

import kotlinx.serialization.Serializable

/**
 * ProxyData
 *
 * @author Kai
 * @version 1.0, 19/05/2022
 */
@Serializable
data class SupplierProxyListData(val http : MutableList<String>, val https : MutableList<String>,
                                 val socks4 : MutableList<String>, val socks5 : MutableList<String>) {
    fun empty() : Boolean = http.isEmpty() && https.isEmpty() && socks4.isEmpty() && socks5.isEmpty()
}

@Serializable
data class FinalProxyListData(val proxies : MutableList<FinalProxyDataType>) {
    fun size(type : String) = proxies.filter { it.protocol == type }.size
}

@Serializable
data class FinalProxyDataType(val protocol : String, val ip : String, val port : Int)

@Serializable
data class PerformanceConnectData(val ovh_FR : EndpointServerData?=null, val aws_NA : EndpointServerData?=null,
                                  val ora_UK : EndpointServerData?=null, val ora_JP : EndpointServerData?=null,
                                  val ms_HK : EndpointServerData?=null) {
        fun default() : PerformanceConnectData =
            PerformanceConnectData(
                EndpointServerData().default(),
                EndpointServerData().default(),
                EndpointServerData().default(),
                EndpointServerData().default(),
                EndpointServerData().default()
            )

}

@Serializable
data class EndpointServerData(var ping : Long?=null, val connections : ConnectionAttempts?=null,
                              var uptime : String?=null, var cleanSocket : Boolean?=null) {
    fun default(): EndpointServerData =
        EndpointServerData(0, ConnectionAttempts(0, 0), "0%", false)
}

@Serializable
data class ConnectionAttempts(var success : Int, var fail : Int)

@Serializable
data class ProtocolData(var protocol : MutableList<ProtocolDataType>)

@Serializable
data class ProtocolDataType(var type : String, var port : Int)

@Serializable
data class ProxyCredentials(var username : String, var password : String) {
    fun empty() : Boolean = username.isEmpty() && password.isEmpty()
}