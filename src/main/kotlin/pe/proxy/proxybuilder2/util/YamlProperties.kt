package pe.proxy.proxybuilder2.util

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties("app-config")
data class YamlProperties(val proxyOutputPath : String, val endpointServers : List<EndpointServers>,
                          val proxySupplier : YamlProxySupplierProperties, val proxyCheckApiKey : String) {

    data class YamlProxySupplierProperties(val mainUrl: String, val customUrl: String)

    data class EndpointServers(val name : String, val remoteAddress : String)

}