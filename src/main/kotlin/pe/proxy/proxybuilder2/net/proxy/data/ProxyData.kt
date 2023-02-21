package pe.proxy.proxybuilder2.net.proxy.data

import kotlinx.serialization.Serializable
import pe.proxy.proxybuilder2.util.Utils

/**
 * ProxyData
 *
 * @author Kai
 * @version 1.0, 19/05/2022
 */
@Serializable
data class SupplierProxyListData(val http : MutableList<String>, val https : MutableList<String>,
                                 val socks4 : MutableList<String>, val socks5 : MutableList<String>) {
    fun size() : Int = http.size + https.size + socks4.size + socks5.size

    //TODO - Improve performance/readability
    fun removeAtRandom(amount : Int) {
        val lists = mutableListOf<MutableList<String>>()
        when { http.size > 0 -> lists.add(http) }
        when { https.size > 0 -> lists.add(https) }
        when { socks4.size > 0 -> lists.add(socks4) }
        when { socks5.size > 0 -> lists.add(socks5) }
        for(i in 0 until amount) {
            val theList = lists[Utils.GLOBAL_RANDOM.nextInt(lists.size)]
            if(theList.isNotEmpty())
                theList.removeAt(0)
            else
                removeAtRandom(1) //Recurse
        }
    }
}

@Serializable
data class SimpleProxyDataList(val proxies : MutableMap<String, SimpleProxyDataType>) {
    fun size(protocol : String) : Int = proxies.filter { it.value.protocol == protocol }.size

}

@Serializable
data class SimpleProxyDataType(val protocol : String, val ip : String, val port : Int)

@Serializable
data class PerformanceConnectData(val aws_NA : EndpointServerData?=null, val ora_UK : EndpointServerData?=null,
                                  val ora_JP : EndpointServerData?=null, val ms_HK : EndpointServerData?=null) {
    fun default() : PerformanceConnectData = PerformanceConnectData(
            EndpointServerData().default(), EndpointServerData().default(),
            EndpointServerData().default(), EndpointServerData().default()
        )

}

@Serializable
data class EndpointServerData(var ping : Long?=0L, val connections : ConnectionAttempts?=null,
                              var uptime : String?=null) {
    fun default() : EndpointServerData = EndpointServerData(0, ConnectionAttempts(0, 0), "0%")
}

@Serializable
data class ConnectionAttempts(var success : Int, var fail : Int)

@Serializable
data class ProtocolData(var protocol : MutableList<ProtocolDataType>)

@Serializable
data class ProtocolDataType(var type : String, var port : Int, var tls : Boolean?=false,
                            var autoRead : MutableList<Boolean>?=null)

@Serializable
data class ProxyCredentials(var username : String, var password : String) {
    fun empty() : Boolean = username.isEmpty() && password.isEmpty()
}