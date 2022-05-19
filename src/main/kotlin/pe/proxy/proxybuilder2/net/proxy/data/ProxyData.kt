package pe.proxy.proxybuilder2.net.proxy.data

import kotlinx.serialization.Serializable

@Serializable
data class SupplierProxyListData(var http : MutableList<String>, var https : MutableList<String>,
                                 var socks4 : MutableList<String>, var socks5 : MutableList<String>) {
    fun isEmpty(): Boolean = http.isEmpty() && https.isEmpty() && socks4.isEmpty() && socks5.isEmpty()
}

@Serializable
data class SQLProxyListData(
    var http : MutableList<PerformanceProxyData>, var https : MutableList<PerformanceProxyData>,
    var socks4 : MutableList<PerformanceProxyData>, var socks5 : MutableList<PerformanceProxyData>
    ) {
    fun isEmpty(): Boolean = http.isEmpty() && https.isEmpty() && socks4.isEmpty() && socks5.isEmpty()
}

@Serializable
data class PerformanceProxyData(var ip : String, var port : Int, var protocol : String, var ping : EndpointPingData)

@Serializable
data class EndpointPingData(var ovh_FR : Long, var aws_NA : Long, var ora_UK : Long, var ms_HK : Long)