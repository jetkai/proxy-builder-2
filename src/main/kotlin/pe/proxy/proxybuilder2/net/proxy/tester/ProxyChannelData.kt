package pe.proxy.proxybuilder2.net.proxy.tester

import pe.proxy.proxybuilder2.util.ProxyConfig
import java.sql.Timestamp

/**
 * ProxyChannelData
 *
 * @author Kai
 * @version 1.0, 19/05/2022
 */
data class ProxyChannelData(val ip : String, val port : Int, val type : String,
                            val username : String, val password : String, val autoRead : Boolean,
                            val endpointServer : ProxyConfig.EndpointServer?=null, val response : ProxyChannelResponseData) {
    fun remoteAddress() : String = "$ip:$port"

}

data class ProxyChannelResponseData(var connected : Boolean?=false, var tls : Boolean?=false,
                                    var readable : Boolean?=false, var remoteIp : String?=null,
                                    var startTime : Timestamp?=null, var endTime : Timestamp?=null,
                                    var autoRead : MutableList<Boolean>?=null)