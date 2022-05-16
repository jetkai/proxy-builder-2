package pe.proxy.proxybuilder2.net.proxy

interface IProxyParser {

    val proxies : ProxyData

    fun request() : IProxyParser

    fun parse()

}