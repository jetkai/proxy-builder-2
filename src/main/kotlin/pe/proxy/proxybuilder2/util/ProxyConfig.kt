package pe.proxy.proxybuilder2.util

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

/**
 * ProxyConfig
 *
 * @author Kai
 * @version 1.0, 15/05/2022
 */
@ConstructorBinding
@ConfigurationProperties("proxy-config")
data class ProxyConfig(val outputPath : String, val endpointServers : List<EndpointServer>,
                       val supplier : ProxySupplier, val proxyCheckIo : ProxyCheckIo,
                       val connectAwait : Long, val timeout : Int, val githubList : List<String>,
                       val enabledThreads : EnabledThreads, val twilio : Twilio) {

    data class ProxySupplier(val mainUrl : String, val customUrl : String)

    data class EndpointServer(val name : String, val ip : String, val port : Int)

    data class ProxyCheckIo(val apiKey : String)

    data class EnabledThreads(val queryApi : Boolean, val proxyConnect : Boolean, val endpointMonitor : Boolean,
                              val sqlProxyMonitor: Boolean, val checksPerSecond : Boolean)

    data class Twilio(val sid : String, val token : String, val phoneNumberFrom : String, val phoneNumberTo : String)

}