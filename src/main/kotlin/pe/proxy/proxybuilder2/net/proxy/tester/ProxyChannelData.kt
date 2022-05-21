package pe.proxy.proxybuilder2.net.proxy.tester

import pe.proxy.proxybuilder2.util.YamlProperties
import java.sql.Timestamp

data class ProxyChannelData(val ip : String, val port : Int, val type : String,
                            val username : String, val password : String, val startTime : Timestamp,
                            val endpointServer : YamlProperties.EndpointServer)