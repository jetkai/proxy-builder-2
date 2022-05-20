package pe.proxy.proxybuilder2.net.proxy.data

import kotlinx.serialization.Serializable

@Serializable
data class ProxyCredentials(var username : String, var password : String)