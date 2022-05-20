package pe.proxy.proxybuilder2.net.proxy.data

import kotlinx.serialization.Serializable

@Serializable
data class SupplierProxyListData(val http : MutableList<String>, val https : MutableList<String>,
                                 val socks4 : MutableList<String>, val socks5 : MutableList<String>) {
    fun isEmpty(): Boolean = http.isEmpty() && https.isEmpty() && socks4.isEmpty() && socks5.isEmpty()
}

@Serializable
data class PerformanceConnectData(val ovh_FR : EndpointServerData, val aws_NA : EndpointServerData,
                                  val ora_UK : EndpointServerData, val ora_JP : EndpointServerData,
                                  val ms_HK : EndpointServerData) {

    companion object {
        fun default(): PerformanceConnectData {
            val endpointServerData = EndpointServerData(0, ConnectionAttempts(0, 0), "0%")
            return PerformanceConnectData(
                endpointServerData, endpointServerData, endpointServerData,
                endpointServerData, endpointServerData
            )
        }
    }

}

@Serializable
data class EndpointServerData(var ping : Long, val connections : ConnectionAttempts, var uptime : String)

@Serializable
data class ConnectionAttempts(var success : Int, var fail : Int)