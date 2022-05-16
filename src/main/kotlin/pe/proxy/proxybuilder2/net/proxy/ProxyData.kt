package pe.proxy.proxybuilder2.net.proxy

import kotlinx.serialization.Serializable

@Serializable
data class ProxyData(var http : MutableList<String>, var https : MutableList<String>,
                     var socks4 : MutableList<String>, var socks5 : MutableList<String>) {

    fun isEmpty(): Boolean = http.isEmpty() && https.isEmpty() && socks4.isEmpty() && socks5.isEmpty()

}