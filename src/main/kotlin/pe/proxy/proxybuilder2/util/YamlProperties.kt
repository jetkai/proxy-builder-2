package pe.proxy.proxybuilder2.util

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties("app-config")
data class YamlProperties(val endpointTestServers : List<String>, val proxySupplier : YamlProxySupplierProperties)

data class YamlProxySupplierProperties(val mainUrl : String, val customUrl : String)